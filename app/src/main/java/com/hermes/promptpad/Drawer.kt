package com.hermes.promptpad

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppIcon(icon: Drawable?, bundledIconRes: Int? = null, size: Int = 44) {
    if (bundledIconRes != null) {
        Icon(
            painterResource(bundledIconRes),
            contentDescription = null,
            modifier = Modifier.size((size * 0.64f).dp),
            tint = White,
        )
        return
    }
    val bitmap = remember(icon, size) { icon?.toBitmap(size, size)?.asImageBitmap() }
    if (bitmap != null) {
        Image(bitmap, null, Modifier.size(size.dp).clip(RoundedCornerShape((size * 19 / 72).dp)))
    }
}

fun initialSearchValue(query: String) = TextFieldValue(query, selection = TextRange(query.length))

@Composable
fun DrawerScreen(prefs: Prefs, initialQuery: String, back: () -> Unit) {
    val ctx = LocalContext.current
    val apps = remember { Apps.all(ctx) }
    var query by remember { mutableStateOf(initialSearchValue(initialQuery)) }
    val results = remember(query.text, apps) { Apps.search(apps, query.text) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Column(Modifier.fillMaxSize().background(Black).safeDrawingPadding().padding(horizontal = Dim2.screen)) {
        LazyColumn(Modifier.weight(1f), reverseLayout = true) {
            items(results, key = { it.spec }) { app ->
                Row48({ Apps.launch(ctx, app); back() }) {
                    AppIcon(app.icon, app.bundledIconRes)
                    Spacer(Modifier.width(12.dp))
                    Text(app.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("search apps", style = MaterialTheme.typography.bodyMedium, color = DotIdle) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focus).padding(bottom = 6.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                results.firstOrNull()?.let { Apps.launch(ctx, it); back() }
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Black, unfocusedContainerColor = Black,
                cursorColor = Accent, focusedIndicatorColor = Accent, unfocusedIndicatorColor = White,
            ),
        )
    }
}
