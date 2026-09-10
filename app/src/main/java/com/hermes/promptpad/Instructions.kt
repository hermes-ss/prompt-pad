package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun InstructionsScreen(done: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Black).safeDrawingPadding().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth()) {
            Text("prompt-pad", Modifier.align(Alignment.Center), style = MaterialTheme.typography.headlineMedium)
            IconButton(done, Modifier.align(Alignment.CenterEnd)) {
                Icon(Icons.Outlined.Close, "Dismiss instructions", tint = Accent)
            }
        }
        Spacer(Modifier.height(28.dp))
        Instruction("Edit Home", "Long-press a blank area, then tap the date, clock, weather or an icon to replace its shortcut. Tap weather to choose an app the first time. Long-press again to leave edit mode.")
        Instruction("Launcher settings", "Swipe left on the Home screen, or tap the settings symbol in app search.")
        Instruction("Notifier", "On by default; toggle notifier in Settings. Swipe right on Home to open it, then left on a blank area to return. Grant notification access when prompted.")
        Instruction("Apps", "Swipe up or start typing on the physical keyboard to search.")
        Spacer(Modifier.weight(1f))
        Text(
            "start",
            Modifier.fillMaxWidth().heightIn(min = Dim2.touch).clickable { done() }.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Accent,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Instruction(title: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Accent)
        Text(text, Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = Dim)
    }
}
