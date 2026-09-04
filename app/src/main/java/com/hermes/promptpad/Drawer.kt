package com.hermes.promptpad

import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun DrawerScreen(prefs: Prefs, initialQuery: String, back: () -> Unit) {
    val ctx = LocalContext.current
    val apps = remember { Apps.all(ctx) }
    var q by remember { mutableStateOf(initialQuery) }
    val results = remember(q) { Apps.search(apps, q) }
    val fr = remember { FocusRequester() }
    LaunchedEffect(Unit) { fr.requestFocus() }

    Column(Modifier.fillMaxSize().background(Black).safeDrawingPadding().padding(horizontal = Dim2.screen)) {
        LazyColumn(Modifier.weight(1f), reverseLayout = true) {
            items(results) { a ->
                Row48({ Apps.launch(ctx, a.pkg); back() }) {
                    Text(a.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        TextField(
            value = q,
            onValueChange = { q = it },
            placeholder = { Text("search apps", style = MaterialTheme.typography.bodyMedium, color = DotIdle) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(fr).padding(bottom = 6.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                results.firstOrNull()?.let { Apps.launch(ctx, it.pkg); back() }
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Charcoal, unfocusedContainerColor = Charcoal,
                cursorColor = Accent, focusedIndicatorColor = Accent, unfocusedIndicatorColor = Charcoal
            )
        )
    }
}
