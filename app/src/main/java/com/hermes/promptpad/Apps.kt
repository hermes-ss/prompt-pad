package com.hermes.promptpad

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserManager

data class AppEntry(
    val label: String,
    val pkg: String,
    val activity: String,
    val userSerial: Long,
    val icon: Drawable?,
    val bundledIconRes: Int? = null,
) {
    val spec: String get() = listOf(pkg, activity, userSerial.toString()).joinToString("\t")
}

object Apps {
    private val KATAPULT_ICONS = mapOf(
        "org.thoughtcrime.securesms" to R.drawable.signal,
        "org.telegram.messenger" to R.drawable.telegram,
        "com.viber.voip" to R.drawable.viber,
        "com.whatsapp" to R.drawable.whatsapp,
        "com.beeper.android" to R.drawable.beeper,
        "com.aurora.store" to R.drawable.aurora,
        "org.fdroid.fdroid" to R.drawable.f_droid,
        "com.spotify.music" to R.drawable.spotify,
        "com.android.gallery3d" to R.drawable.gallery,
        "com.google.android.apps.photos" to R.drawable.gallery,
        "com.android.deskclock" to R.drawable.clock,
        "com.google.android.deskclock" to R.drawable.clock,
        "com.android.fmradio" to R.drawable.radio,
        "com.android.stk" to R.drawable.sim,
        "org.mozilla.firefox" to R.drawable.firefox,
        "org.mozilla.firefox_beta" to R.drawable.firefox,
        "org.mozilla.focus" to R.drawable.firefox,
        "com.fsck.k9" to R.drawable.mail,
        "net.thunderbird.android" to R.drawable.mail,
        "com.google.android.gm" to R.drawable.mail,
        "com.android.documentsui" to R.drawable.files,
        "de.danoeh.antennapod" to R.drawable.ap,
        "dev.octoshrimpy.quik" to R.drawable.sms,
        "com.message.ink" to R.drawable.sms,
        "com.discord" to R.drawable.discord,
        "fm.libro.librofm" to R.drawable.librofm,
        "org.schabi.newpipe" to R.drawable.newpipe,
        "org.fossify.musicplayer" to R.drawable.music,
        "org.oxycblt.auxio" to R.drawable.music,
        "com.foobar2000.foobar2000" to R.drawable.music,
        "org.videolan.vlc" to R.drawable.music,
        "com.android.settings" to R.drawable.settings,
        "com.android.vending" to R.drawable.google,
        "com.paypal.android.p2pmobile" to R.drawable.money,
        "it.palsoftware.pastiera" to R.drawable.keyboard,
        "it.palsoftware.pastiera.nightly" to R.drawable.keyboard,
        "org.chromium.webview_shell" to R.drawable.chromium,
        "com.brave.browser" to R.drawable.brave,
        "com.zsemberi.killapps" to R.drawable.killapps,
        "org.koreader.launcher" to R.drawable.koreader,
        "org.koreader.launcher.fdroid" to R.drawable.koreader,
        "com.reddit.frontpage" to R.drawable.reddit,
        "info.plateaukao.einkbro" to R.drawable.einkbro,
        "ws.xsoh.etar" to R.drawable.calendar,
        "org.onekash.kashcal" to R.drawable.calendar,
        "com.android.dialer" to R.drawable.phone,
        "com.google.android.dialer" to R.drawable.phone,
        "com.android.camera2" to R.drawable.camera,
        "com.google.android.GoogleCamera" to R.drawable.camera,
    )

    fun bundledIconForPackage(packageName: String): Int? = KATAPULT_ICONS[packageName]

    fun all(ctx: Context): List<AppEntry> {
        val launcher = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val users = ctx.getSystemService(Context.USER_SERVICE) as UserManager
        return launcher.profiles.flatMap { user ->
            val serial = users.getSerialNumberForUser(user)
            launcher.getActivityList(null, user).map { info ->
                val packageName = info.componentName.packageName
                val bundled = bundledIconForPackage(packageName)
                val managedIcon = if (user != Process.myUserHandle() && bundled != null) runCatching {
                    ctx.getDrawable(bundled)!!.mutate().apply { setTint(Color.WHITE) }
                        .let { ctx.packageManager.getUserBadgedIcon(it, user) }
                }.getOrNull() else null
                AppEntry(
                    info.label.toString(),
                    packageName,
                    info.componentName.className,
                    serial,
                    managedIcon ?: runCatching { info.getBadgedIcon(0) }.getOrNull(),
                    bundled.takeIf { user == Process.myUserHandle() },
                )
            }
        }.distinctBy { Triple(it.pkg, it.activity, it.userSerial) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    fun fromSpec(ctx: Context, spec: String): AppEntry? {
        val parts = spec.split('\t')
        if (parts.size == 3) {
            val serial = parts[2].toLongOrNull() ?: return null
            return all(ctx).firstOrNull { it.pkg == parts[0] && it.activity == parts[1] && it.userSerial == serial }
        }
        return all(ctx).firstOrNull { it.pkg == spec }
    }

    fun label(ctx: Context, spec: String): String = fromSpec(ctx, spec)?.label ?: spec.substringBefore('\t')

    fun launch(ctx: Context, spec: String) {
        val app = fromSpec(ctx, spec)
        if (app != null) launch(ctx, app)
        else ctx.packageManager.getLaunchIntentForPackage(spec)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { ctx.startActivity(it) }
        }
    }

    fun launch(ctx: Context, app: AppEntry) {
        val launcher = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val users = ctx.getSystemService(Context.USER_SERVICE) as UserManager
        val user = users.getUserForSerialNumber(app.userSerial) ?: Process.myUserHandle()
        runCatching {
            launcher.startMainActivity(ComponentName(app.pkg, app.activity), user, null, null)
        }
    }

    fun launchAction(ctx: Context, spec: String) {
        val parts = spec.split("|")
        val intent = Intent(parts[0]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (parts.size > 1) intent.addCategory(parts[1])
        runCatching { ctx.startActivity(intent) }
    }

    fun search(list: List<AppEntry>, query: String): List<AppEntry> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        val prefix = list.filter { it.label.lowercase().startsWith(q) }
        return prefix + list.filter { !it.label.lowercase().startsWith(q) && it.label.lowercase().contains(q) }
    }
}
