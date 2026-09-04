package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    prefs: Prefs,
    back: () -> Unit,
    openAccessibility: () -> Unit,
    applyStatusBar: () -> Unit,
) {
    var version by remember { mutableIntStateOf(0) }
    fun update(block: () -> Unit) { block(); version++ }

    Column(
        Modifier.fillMaxSize().background(Black).safeDrawingPadding()
            .padding(horizontal = Dim2.screen).verticalScroll(rememberScrollState()),
    ) {
        Header("settings", back)
        key(version) {
            Section("appearance")
            Toggle("battery in peak widget", prefs.showBattery) { update { prefs.showBattery = it } }
            Toggle("weather in peak widget", prefs.showWeather) { update { prefs.showWeather = it } }
            Toggle("peak widget right-aligned", prefs.peakRight) { update { prefs.peakRight = it } }
            Choice("peak variant", listOf("time+date", "one line", "stacked"), prefs.peakVariant) { update { prefs.peakVariant = it } }
            Choice(
                "text size",
                listOf("90%", "100%", "115%", "130%"),
                listOf(90, 100, 115, 130).indexOf(prefs.textScale).coerceAtLeast(1),
            ) { update { prefs.textScale = listOf(90, 100, 115, 130)[it] } }
            Toggle("left-handed layout", prefs.leftHanded) { update { prefs.leftHanded = it } }
            Toggle("reduce motion", prefs.reduceMotion) { update { prefs.reduceMotion = it } }

            Section("gestures and system bars")
            Toggle("tap blank area twice to sleep", prefs.tapToSleep) { enabled ->
                update { prefs.tapToSleep = enabled }
                if (enabled) openAccessibility()
            }
            Text(
                "Enable the Prompt-Pad tap-to-sleep service in Accessibility.",
                Modifier.padding(bottom = 6.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Dim,
            )
            Toggle("hide status bar", prefs.hideStatusBar) { hidden ->
                update { prefs.hideStatusBar = hidden }
                applyStatusBar()
            }
            Text(
                "Swipe from the top edge to reveal it temporarily.",
                Modifier.padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Dim,
            )
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, Modifier.padding(top = 14.dp, bottom = 4.dp), style = MaterialTheme.typography.labelSmall, color = Accent)
}

@Composable
private fun Toggle(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row48({ onChange(!value) }) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(if (value) "on" else "off", style = MaterialTheme.typography.bodySmall, color = if (value) Accent else DotIdle)
    }
}

@Composable
private fun Choice(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Dim)
        Spacer(Modifier.height(4.dp))
        Tabs(options, selected, onSelect)
    }
}
