package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val FILTERS = listOf("All", "Messages", "Calls", "Emails", "Apps", "Flagged")

@Composable
fun HubScreen(back: () -> Unit) {
    val ctx = LocalContext.current
    var filter by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf<String?>(null) }
    val all = HubListener.items
    val shown = all.filter {
        when (filter) {
            1 -> it.kind == HubKind.MESSAGE
            2 -> it.kind == HubKind.CALL
            3 -> it.kind == HubKind.EMAIL
            4 -> it.kind == HubKind.APP
            5 -> it.flagged
            else -> true
        }
    }

    Column(Modifier.fillMaxSize().background(Black).safeDrawingPadding().padding(horizontal = Dim2.screen)) {
        Header("hub", center = true)
        Tabs(FILTERS, filter) { filter = it }
        Spacer(Modifier.height(Dim2.gap))
        if (!HubListener.isEnabled(ctx)) {
            Card(Modifier.fillMaxWidth(), onClick = { HubListener.openSettings(ctx) }) {
                Text("Grant notification access", style = MaterialTheme.typography.bodyMedium, color = Accent)
                Text("Prompt-Pad needs it to collect messages, calls and alerts here.",
                    style = MaterialTheme.typography.bodySmall, color = Dim)
            }
            Spacer(Modifier.height(Dim2.gap))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(shown, key = { it.key }) { item ->
                Card(Modifier.fillMaxWidth(), onClick = { expanded = if (expanded == item.key) null else item.key }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                        Text(if (item.flagged) "★" else "☆",
                            Modifier.clickable { item.flagged = !item.flagged
                                val i = all.indexOfFirst { it.key == item.key }
                                if (i >= 0) all[i] = item.copy() }.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium, color = Accent)
                    }
                    if (item.text.isNotBlank()) Text(item.text, style = MaterialTheme.typography.bodySmall, color = Dim)
                    if (expanded == item.key) {
                        if (item.reply != null) {
                            var draft by remember(item.key) { mutableStateOf("") }
                            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextField(draft, { draft = it }, Modifier.weight(1f),
                                    placeholder = { Text("reply", style = MaterialTheme.typography.bodySmall, color = DotIdle) },
                                    singleLine = true, textStyle = MaterialTheme.typography.bodySmall,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Black, unfocusedContainerColor = Black,
                                        cursorColor = Accent, focusedIndicatorColor = Accent, unfocusedIndicatorColor = DotIdle))
                                Text("send", Modifier.clickable {
                                    if (draft.isNotBlank()) { HubListener.sendReply(ctx, item, draft); expanded = null }
                                }.padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium, color = Accent)
                            }
                        } else {
                            Text("no inline reply available", style = MaterialTheme.typography.labelSmall, color = DotIdle)
                        }
                    }
                }
            }
        }
    }
}
