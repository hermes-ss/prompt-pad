package com.hermes.promptpad

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

enum class Screen { Home, Drawer, Hub, Settings, Notes, Todo, Agenda }

class HomeActivity : ComponentActivity() {

    private lateinit var prefs: Prefs
    private var pendingKey: String? by mutableStateOf(null)
    private var screen: Screen by mutableStateOf(Screen.Home)

    private val calendarPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MinimalTheme(prefs) {
                var tick by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) { while (true) { delay(20_000); tick++ } }
                val back = { screen = Screen.Home }
                when (screen) {
                    Screen.Home -> HomeScreen(prefs, { screen = it }, tick)
                    Screen.Drawer -> DrawerScreen(prefs, pendingKey.orEmpty()) { pendingKey = null; back() }
                    Screen.Hub -> HubScreen(back)
                    Screen.Settings -> SettingsScreen(prefs, back) { openUsageAccess() }
                    Screen.Notes -> NotesScreen(back)
                    Screen.Todo -> TodoScreen(back)
                    Screen.Agenda -> AgendaScreen(back) { calendarPermission.launch(Manifest.permission.READ_CALENDAR) }
                }
            }
        }
    }

    /** Home is the root: back never leaves the launcher, it only unwinds to Home. */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (screen != Screen.Home) { pendingKey = null; screen = Screen.Home } // ponytail: one-level stack, no nav lib
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingKey = null
        screen = Screen.Home
    }

    override fun onResume() {
        super.onResume()
        if (prefs.grayscale) BlockService.applyGrayscale(this, true)
    }

    /** Physical keyboard: long-press a mapped letter launches its app, a normal press opens search. */
    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (screen != Screen.Home) return super.onKeyLongPress(keyCode, event)
        val ch = event.displayLabel.lowercaseChar().toString()
        prefs.keyMap[ch]?.let { Apps.launch(this, it); return true }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (screen == Screen.Home && prefs.keyMap.isNotEmpty()) event.startTracking()
        if (screen == Screen.Home) {
            val c = event.unicodeChar.toChar()
            if (c.isLetterOrDigit()) {
                pendingKey = c.toString()
                screen = Screen.Drawer
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun openUsageAccess() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}
