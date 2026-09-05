package com.hermes.promptpad

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun AgendaScreen(back: () -> Unit, requestCalendar: () -> Unit) {
    val ctx = LocalContext.current
    val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    val events = remember(granted) { if (granted) Agenda.upcoming(ctx) else emptyList() }

    Column(Modifier.fillMaxSize().background(Black).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)).padding(horizontal = Dim2.screen)) {
        Header("agenda", center = true)
        if (!granted) {
            Card(Modifier.fillMaxWidth(), onClick = requestCalendar) {
                Text("Allow calendar access", style = MaterialTheme.typography.bodyMedium, color = Accent)
            }
        } else if (events.isEmpty()) {
            Text("nothing in the next 7 days", style = MaterialTheme.typography.bodySmall, color = Dim)
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(events, key = { it.begin.toString() + it.id }) { e ->
                Card(Modifier.fillMaxWidth()) {
                    Text(e.title, style = MaterialTheme.typography.titleMedium)
                    Text(Agenda.when_(e), style = MaterialTheme.typography.bodySmall, color = Dim)
                    if (!e.location.isNullOrBlank()) {
                        Text(e.location, Modifier.padding(top = 4.dp).heightIn(min = 32.dp)
                            .clickable { Agenda.navigate(ctx, e.location) },
                            style = MaterialTheme.typography.bodySmall, color = Accent)
                    }
                }
            }
        }
    }
}
