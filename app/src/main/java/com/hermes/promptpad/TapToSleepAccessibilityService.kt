package com.hermes.promptpad

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class TapToSleepAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { active = this }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
    override fun onUnbind(intent: android.content.Intent?): Boolean {
        if (active === this) active = null
        return super.onUnbind(intent)
    }

    companion object {
        @Volatile private var active: TapToSleepAccessibilityService? = null
        fun lockScreen(): Boolean =
            active?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) == true
    }
}
