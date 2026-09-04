package com.hermes.promptpad

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserManager

data class AppEntry(
    val label: String,
    val pkg: String,
    val activity: String,
    val userSerial: Long,
    val icon: Drawable?,
) {
    val spec: String get() = listOf(pkg, activity, userSerial.toString()).joinToString("\t")
}

object Apps {
    fun all(ctx: Context): List<AppEntry> {
        val launcher = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val users = ctx.getSystemService(Context.USER_SERVICE) as UserManager
        return launcher.profiles.flatMap { user ->
            val serial = users.getSerialNumberForUser(user)
            launcher.getActivityList(null, user).map { info ->
                AppEntry(
                    info.label.toString(),
                    info.componentName.packageName,
                    info.componentName.className,
                    serial,
                    runCatching { info.getBadgedIcon(0) }.getOrNull(),
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
        if (query.isBlank()) return list
        val q = query.lowercase()
        val prefix = list.filter { it.label.lowercase().startsWith(q) }
        return prefix + list.filter { !it.label.lowercase().startsWith(q) && it.label.lowercase().contains(q) }
    }
}
