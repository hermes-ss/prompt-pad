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
import androidx.compose.ui.text.style.TextAlign
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
            .background(Black)
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
fun Header(text: String, back: (() -> Unit)? = null, center: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = Dim2.touch).then(if (center) Modifier.padding(top = 4.dp) else Modifier),
        horizontalArrangement = if (center) Arrangement.Center else Arrangement.Start,
        verticalAlignment = if (center) Alignment.Top else Alignment.CenterVertically,
    ) {
        if (back != null) Text("‹", Modifier.clickable { back() }.padding(end = 12.dp), style = MaterialTheme.typography.headlineSmall)
        Text(text, style = if (center) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun EdgeScreen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Black)) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = Dim2.screen)) {
            Spacer(Modifier.height(Dim2.touch))
            content()
        }
        Text(
            title,
            Modifier.fillMaxWidth().align(Alignment.TopCenter),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun Tabs(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        labels.forEachIndexed { i, l ->
            Text(
                l,
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (i == selected) Accent else Black)
                    .clickable { onSelect(i) }
                    .padding(horizontal = 2.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (i == selected) Black else Dim,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}
