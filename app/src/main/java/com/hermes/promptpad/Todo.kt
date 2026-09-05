package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TodoScreen(back: () -> Unit, autoFocusAdd: Boolean = false) {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    var tasks by remember { mutableStateOf(store.tasks()) }
    var draft by remember { mutableStateOf("") }
    var vanishing by remember { mutableStateOf<Long?>(null) }
    val fr = remember { FocusRequester() }
    if (autoFocusAdd) LaunchedEffect(Unit) { fr.requestFocus() }

    // Checked task shows a mark for a beat, then disappears from view entirely.
    LaunchedEffect(vanishing) {
        val id = vanishing ?: return@LaunchedEffect
        delay(250)
        val next = tasks.map { if (it.id == id) it.copy(done = true) else it }.toMutableList()
        store.saveTasks(next); tasks = next; vanishing = null
    }

    Column(Modifier.fillMaxSize().background(Black).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).padding(horizontal = Dim2.screen)) {
        Header("to do", center = true)
        TextField(draft, { draft = it },
            Modifier.fillMaxWidth().focusRequester(fr),
            placeholder = { Text("Add a task...", color = DotIdle) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (draft.isNotBlank()) {
                    val next = (tasks + Task(System.currentTimeMillis(), draft.trim(), false)).toMutableList()
                    store.saveTasks(next); tasks = next; draft = ""
                }
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Charcoal, unfocusedContainerColor = Charcoal,
                cursorColor = Accent, focusedIndicatorColor = Accent, unfocusedIndicatorColor = Charcoal))
        Spacer(Modifier.height(Dim2.gap))
        LazyColumn {
            items(tasks.filter { !it.done }, key = { it.id }) { t ->
                Row(Modifier.fillMaxWidth().heightIn(min = Dim2.touch)
                    .clickable { vanishing = t.id }, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (vanishing == t.id) "☑" else "☐", Modifier.padding(end = 10.dp),
                        style = MaterialTheme.typography.bodyMedium, color = if (vanishing == t.id) Accent else White)
                    Text(t.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
