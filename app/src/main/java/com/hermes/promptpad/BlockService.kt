package com.hermes.promptpad

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

/**
 * Focus/Monk enforcement: when a restricted package comes to the foreground, bounce back Home.
 * ponytail: AccessibilityService only. DevicePolicyManager needs device-owner provisioning
 * (factory-reset + adb), which the Seal flow already documents as the only way out.
 */
class BlockService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName) return
        val p = Prefs(this)
        if (!shouldBlock(p, pkg, Usage.totalForegroundMsToday(this))) return
        Toast.makeText(this, "Blocked by Prompt-Pad", Toast.LENGTH_SHORT).show()
        performGlobalAction(GLOBAL_ACTION_HOME)
    }

    override fun onInterrupt() {}

    companion object {
        fun shouldBlock(p: Prefs, pkg: String, foregroundMsToday: Long): Boolean =
            shouldBlock(p.restriction, p.dailyLimitMin, p.distracting, pkg, foregroundMsToday)

        /** Pure rule, unit-tested. restriction: 0 none, 1 focus, 2 monk. */
        fun shouldBlock(restriction: Int, limitMin: Int, distracting: Set<String>, pkg: String, foregroundMsToday: Long): Boolean =
            when (restriction) {
                2 -> pkg in Prefs.BROWSERS_AND_SOCIAL
                1 -> foregroundMsToday >= limitMin * 60_000L && pkg in distracting
                else -> false
            }

        fun isEnabled(ctx: Context): Boolean =
            (Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
                .contains("${ctx.packageName}/.BlockService")

        fun openSettings(ctx: Context) {
            ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        /** System-wide grayscale via the accessibility display daltonizer (needs WRITE_SECURE_SETTINGS or adb). */
        fun applyGrayscale(ctx: Context, on: Boolean): Boolean = runCatching {
            Settings.Secure.putInt(ctx.contentResolver, "accessibility_display_daltonizer_enabled", if (on) 1 else 0)
            Settings.Secure.putInt(ctx.contentResolver, "accessibility_display_daltonizer", if (on) 0 else -1)
            true
        }.getOrDefault(false)
    }
}
