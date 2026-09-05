package com.hermes.promptpad

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotesScreen(back: () -> Unit) {
    val ctx = LocalContext.current
    val store = remember { Store(ctx) }
    var notes by remember { mutableStateOf(store.notes()) }
    var folder by remember { mutableStateOf(0) }
    var open by remember { mutableStateOf<Long?>(null) }

    fun persist(list: MutableList<Note>) { notes = list; store.saveNotes(list) }

    val current = notes.firstOrNull { it.id == open }
    if (current != null) {
        var confirmDelete by remember { mutableStateOf(false) }
        NoteEditor(current, onChange = { persist(notes.toMutableList()) }, back = { open = null }, onDelete = { confirmDelete = true })
        if (confirmDelete) AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete note?") },
            text = { Text("This cannot be undone.") },
            confirmButton = { TextButton(onClick = { persist(notes.filterNot { it.id == current.id }.toMutableList()); open = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Black).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).padding(horizontal = Dim2.screen)) {
        Header("notes", center = true)
        Tabs(Store.FOLDERS, folder) { folder = it }
        Spacer(Modifier.height(Dim2.gap))
        Text("+ new note", Modifier.fillMaxWidth().heightIn(min = Dim2.touch).clickable {
            val n = Note(System.currentTimeMillis(), Store.FOLDERS[folder], "", "")
            persist((notes + n).toMutableList()); open = n.id
        }.padding(vertical = 12.dp), style = MaterialTheme.typography.bodyMedium, color = Accent)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(notes.filter { it.folder == Store.FOLDERS[folder] }, key = { it.id }) { n ->
                Card(Modifier.fillMaxWidth(), onClick = { open = n.id }) {
                    Text(n.title.ifBlank { "(untitled)" }, style = MaterialTheme.typography.titleMedium)
                    if (n.body.isNotBlank()) Text(n.body.lineSequence().first().take(60),
                        style = MaterialTheme.typography.bodySmall, color = Dim)
                }
            }
        }
    }
}

/**
 * Rich text is stored as lightweight markdown markers (**b**, _i_, __u__, # h, - bullet, [] checkbox)
 * ponytail: markers over a span model — round-trips through plain-text storage for free.
 */
@Composable
fun NoteEditor(note: Note, onChange: () -> Unit, back: () -> Unit, onDelete: () -> Unit) {
    var title by remember(note.id) { mutableStateOf(note.title) }
    var body by remember(note.id) { mutableStateOf(note.body) }
    var preview by remember { mutableStateOf(false) }

    fun wrap(marker: String) { body += marker; note.body = body; onChange() }

    Column(Modifier.fillMaxSize().background(Black).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).padding(horizontal = Dim2.screen)) {
        Header("note", center = true)
        TextField(title, { title = it; note.title = it; onChange() }, Modifier.fillMaxWidth(),
            placeholder = { Text("title", color = DotIdle) }, singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium,
            colors = noteFieldColors())
        if (preview) {
            Column(Modifier.weight(1f).padding(top = 8.dp)) {
                body.lines().forEach { line -> RichLine(line) { done ->
                    body = body.lines().joinToString("\n") { if (it == line) toggleCheck(it, done) else it }
                    note.body = body; onChange()
                } }
            }
        } else {
            TextField(body, { body = it; note.body = it; onChange() },
                Modifier.fillMaxWidth().weight(1f), textStyle = MaterialTheme.typography.bodyMedium,
                placeholder = { Text("write…", color = DotIdle) }, colors = noteFieldColors())
        }
        Row(Modifier.fillMaxWidth().heightIn(min = Dim2.touch), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("H" to "\n# ", "B" to "**b**", "I" to "_i_", "U" to "__u__", "•" to "\n- ", "☐" to "\n[] ").forEach { (l, m) ->
                Text(l, Modifier.clickable { wrap(m) }.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium, color = if (preview) DotIdle else White)
            }
            Text(if (preview) "edit" else "view", Modifier.clickable { preview = !preview }.padding(8.dp),
                style = MaterialTheme.typography.bodySmall, color = Accent)
            Text("delete", Modifier.clickable { onDelete() }.padding(8.dp),
                style = MaterialTheme.typography.bodySmall, color = Accent)
        }
    }
}

private fun toggleCheck(line: String, done: Boolean) = when {
    done -> line.replaceFirst("[] ", "[x] ")
    else -> line.replaceFirst("[x] ", "[] ")
}

@Composable
private fun RichLine(line: String, onCheck: (Boolean) -> Unit) {
    val checked = line.startsWith("[x] ")
    val isCheck = checked || line.startsWith("[] ")
    val text = line.removePrefix("[x] ").removePrefix("[] ").removePrefix("- ").removePrefix("# ")
    Row(Modifier.fillMaxWidth().heightIn(min = if (isCheck) Dim2.touch else 0.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (isCheck) Text(if (checked) "☑" else "☐", Modifier.clickable { onCheck(!checked) }.padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium, color = Accent)
        else if (line.startsWith("- ")) Text("•  ", style = MaterialTheme.typography.bodyMedium)
        Text(inline(text, strike = checked),
            style = if (line.startsWith("# ")) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium)
    }
}

/** Renders **bold**, _italic_, __underline__ inside one line. */
fun inline(s: String, strike: Boolean = false) = buildAnnotatedString {
    val re = Regex("\\*\\*(.+?)\\*\\*|__(.+?)__|_(.+?)_")
    var i = 0
    val base = if (strike) SpanStyle(textDecoration = TextDecoration.LineThrough) else SpanStyle()
    re.findAll(s).forEach { m ->
        withStyle(base) { append(s.substring(i, m.range.first)) }
        val style = when {
            m.groupValues[1].isNotEmpty() -> base.copy(fontWeight = FontWeight.Bold)
            m.groupValues[2].isNotEmpty() -> base.copy(textDecoration = TextDecoration.Underline)
            else -> base.copy(fontStyle = FontStyle.Italic)
        }
        withStyle(style) { append(m.groupValues.drop(1).first { it.isNotEmpty() }) }
        i = m.range.last + 1
    }
    withStyle(base) { append(s.substring(i)) }
}

@Composable
private fun noteFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Black, unfocusedContainerColor = Black,
    cursorColor = Accent, focusedIndicatorColor = Charcoal, unfocusedIndicatorColor = Black
)
