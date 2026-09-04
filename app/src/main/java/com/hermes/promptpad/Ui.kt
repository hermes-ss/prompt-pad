package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Titan 2 Elite is 4.04in / 4:3 — compact paddings everywhere. */
object Dim2 {
    val screen = 10.dp
    val gap = 8.dp
    val touch = 48.dp
    val cutoutLeft = 32.dp
}

@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Charcoal)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        content = content
    )
}

@Composable
fun Row48(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = Dim2.touch).clickable { onClick() }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun Header(text: String, back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = Dim2.touch), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) {
            Text("‹", Modifier.clickable { back() }.padding(end = 12.dp), style = MaterialTheme.typography.headlineSmall)
        }
        Text(text, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun Tabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { i, l ->
            Text(
                l,
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (i == selected) Accent else Charcoal)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (i == selected) Black else Dim
            )
        }
    }
}

/** Left-handed mode pulls interactive content toward the left margin. */
fun Modifier.hand(prefs: Prefs): Modifier =
    if (prefs.leftHanded) this.padding(end = 24.dp) else this.padding(start = 12.dp)
