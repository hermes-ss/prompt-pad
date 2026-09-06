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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun TodoScreen() {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    var tasks by remember { mutableStateOf(store.tasks()) }
    var draft by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }

    EdgeScreen("to do") {
        TextField(draft, { draft = it },
            Modifier.fillMaxWidth().focusRequester(focus),
            placeholder = { Text("Add a task...", color = DotIdle) }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (draft.isNotBlank()) {
                    val next = tasks + Task(System.currentTimeMillis(), draft.trim(), false)
                    store.saveTasks(next); tasks = next; draft = ""
                }
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Black, unfocusedContainerColor = Black,
                cursorColor = Accent, focusedIndicatorColor = Accent, unfocusedIndicatorColor = Black))
        Spacer(Modifier.height(Dim2.gap))
        LazyColumn(Modifier.weight(1f)) {
            items(tasks, key = { it.id }) { t ->
                Row(Modifier.fillMaxWidth().heightIn(min = Dim2.touch)
                    .clickable {
                        tasks = toggleTask(tasks, t.id)
                        store.saveTasks(tasks)
                    }, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (t.done) "☑" else "☐", Modifier.padding(end = 10.dp),
                        style = MaterialTheme.typography.bodyMedium, color = if (t.done) Accent else White)
                    Text(t.text, style = MaterialTheme.typography.bodyMedium,
                        textDecoration = if (t.done) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (t.done) DotIdle else White)
                }
            }
        }
        Text("Clear", Modifier.align(Alignment.End).heightIn(min = Dim2.touch).clickable {
            tasks = clearDone(tasks); store.saveTasks(tasks)
        }.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium, color = Accent)
    }
}

fun toggleTask(tasks: List<Task>, id: Long) = tasks.map { if (it.id == id) it.copy(done = !it.done) else it }
fun clearDone(tasks: List<Task>) = tasks.filterNot { it.done }
