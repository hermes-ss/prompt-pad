package com.hermes.promptpad

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process

data class AppEntry(val label: String, val pkg: String, val icon: Drawable?)

object Apps {
    fun all(ctx: Context): List<AppEntry> {
        val la = ctx.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        return la.getActivityList(null, Process.myUserHandle())
            .map { AppEntry(it.label.toString(), it.applicationInfo.packageName, runCatching { it.getIcon(0) }.getOrNull()) }
            .distinctBy { it.pkg }
            .sortedBy { it.label.lowercase() }
    }

    fun label(ctx: Context, pkg: String): String = runCatching {
        val pm = ctx.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    }.getOrDefault(pkg)

    fun launch(ctx: Context, pkg: String) {
        val i = ctx.packageManager.getLaunchIntentForPackage(pkg) ?: return
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }
    }

    /** Prefix matches first, then substring — matches how people type on a physical keyboard. */
    fun launchAction(ctx: Context, spec: String) {
        val parts = spec.split("|")
        val i = Intent(parts[0]).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (parts.size > 1) i.addCategory(parts[1])
        runCatching { ctx.startActivity(i) }
    }

    fun search(list: List<AppEntry>, q: String): List<AppEntry> {
        if (q.isBlank()) return list
        val n = q.lowercase()
        val pre = list.filter { it.label.lowercase().startsWith(n) }
        val sub = list.filter { !it.label.lowercase().startsWith(n) && it.label.lowercase().contains(n) }
        return pre + sub
    }
}
