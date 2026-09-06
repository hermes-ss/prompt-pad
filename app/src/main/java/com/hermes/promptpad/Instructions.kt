package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
        Text("prompt-pad", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(28.dp))
        Instruction("Edit Home", "Long-press a blank area, then tap the date or an icon to replace it. Long-press again to leave edit mode.")
        Instruction("Launcher settings", "Swipe left on the Home screen, or tap the settings symbol in app search.")
        Instruction("Hub", "Swipe right on the Home screen. Grant notification access when prompted.")
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
