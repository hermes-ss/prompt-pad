package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(prefs: Prefs, back: () -> Unit, requestUsage: () -> Unit) {
    val ctx = LocalContext.current
    var v by remember { mutableStateOf(0) }         // bump to recompose after pref writes
    var confirmSeal by remember { mutableStateOf(false) }
    val locked = prefs.sealed
    fun set(block: () -> Unit) { if (!locked) { block(); v++ } }

    Column(Modifier.fillMaxSize().background(Black).safeDrawingPadding()
        .padding(horizontal = Dim2.screen).verticalScroll(rememberScrollState())) {
        Header("settings", back)
        key(v) {
            if (locked) Text("sealed — changes require a factory reset",
                Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.bodySmall, color = DotBad)

            Section("appearance")
            Toggle("battery in peak widget", prefs.showBattery) { set { prefs.showBattery = it } }
            Toggle("weather in peak widget", prefs.showWeather) { set { prefs.showWeather = it } }
            Toggle("peak widget right-aligned", prefs.peakRight) { set { prefs.peakRight = it } }
            Choice("peak variant", listOf("time+date", "one line", "stacked"), prefs.peakVariant) { set { prefs.peakVariant = it } }
            Choice("font", listOf("sans", "serif", "easy read"), prefs.fontProfile) { set { prefs.fontProfile = it } }
            Choice("text size", listOf("90%", "100%", "115%", "130%"), listOf(90, 100, 115, 130).indexOf(prefs.textScale).coerceAtLeast(1)) {
                set { prefs.textScale = listOf(90, 100, 115, 130)[it] }
            }
            Toggle("left-handed layout", prefs.leftHanded) { set { prefs.leftHanded = it } }
            Toggle("reduce motion", prefs.reduceMotion) { set { prefs.reduceMotion = it } }

            Section("activity tracking")
            if (!Usage.hasPermission(ctx)) {
                Card(Modifier.fillMaxWidth(), onClick = requestUsage) {
                    Text("Grant usage access", style = MaterialTheme.typography.bodyMedium, color = Accent)
                }
                Spacer(Modifier.height(6.dp))
            }
            DistractingApps(prefs) { set { } }

            Section("restriction")
            Choice("mode", listOf("unrestricted", "focus", "monk"), prefs.restriction) { set { prefs.restriction = it } }
            if (prefs.restriction == 1) {
                Choice("daily limit", listOf("30m", "45m", "1h"), listOf(30, 45, 60).indexOf(prefs.dailyLimitMin).coerceAtLeast(0)) {
                    set { prefs.dailyLimitMin = listOf(30, 45, 60)[it] }
                }
                Toggle("grayscale", prefs.grayscale) { set { prefs.grayscale = it; BlockService.applyGrayscale(ctx, it) } }
            }
            if (prefs.restriction == 2 && !prefs.grayscale) {
                LaunchedEffect(Unit) { prefs.grayscale = true; BlockService.applyGrayscale(ctx, true) }
            }
            if (prefs.restriction > 0 && !BlockService.isEnabled(ctx)) {
                Card(Modifier.fillMaxWidth(), onClick = { BlockService.openSettings(ctx) }) {
                    Text("Enable Prompt-Pad Focus accessibility service", style = MaterialTheme.typography.bodyMedium, color = Accent)
                    Text("Required to block restricted apps.", style = MaterialTheme.typography.bodySmall, color = Dim)
                }
            }
            if (!locked && prefs.restriction > 0) {
                Spacer(Modifier.height(Dim2.gap))
                Text("SEAL SETTINGS", Modifier.fillMaxWidth().heightIn(min = Dim2.touch)
                    .background(DotBad).clickable { confirmSeal = true }.padding(12.dp),
                    style = MaterialTheme.typography.titleMedium, color = Black)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmSeal) {
        Box(Modifier.fillMaxSize().background(Black.copy(alpha = 0.96f)), contentAlignment = Alignment.Center) {
            Column(Modifier.padding(24.dp)) {
                Text("Seal these limits?", style = MaterialTheme.typography.headlineSmall, color = DotBad)
                Spacer(Modifier.height(8.dp))
                Text("Once sealed, restriction settings cannot be changed, bypassed or removed except by a full device factory reset.",
                    style = MaterialTheme.typography.bodyMedium, color = Dim)
                Spacer(Modifier.height(16.dp))
                Row {
                    Text("cancel", Modifier.clickable { confirmSeal = false }.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(24.dp))
                    Text("SEAL", Modifier.clickable { prefs.sealed = true; confirmSeal = false; v++ }.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium, color = DotBad)
                }
            }
        }
    }
}

@Composable
private fun Section(t: String) {
    Text(t, Modifier.padding(top = 14.dp, bottom = 4.dp), style = MaterialTheme.typography.labelSmall, color = Accent)
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

@Composable
private fun DistractingApps(prefs: Prefs, onChange: () -> Unit) {
    val ctx = LocalContext.current
    var open by remember { mutableStateOf(false) }
    var set by remember { mutableStateOf(prefs.distracting) }
    Row48({ open = !open }) {
        Text("distracting apps", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("${set.size}", style = MaterialTheme.typography.bodySmall, color = Accent)
    }
    if (open) {
        val apps = remember { Apps.all(ctx) }
        Column {
            apps.forEach { a ->
                Row48({
                    set = if (a.pkg in set) set - a.pkg else set + a.pkg
                    prefs.distracting = set; onChange()
                }) {
                    Text(if (a.pkg in set) "☑" else "☐", Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium, color = if (a.pkg in set) Accent else DotIdle)
                    Text(a.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
