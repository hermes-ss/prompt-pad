package com.hermes.promptpad

import android.content.Context
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(prefs: Prefs, nav: (Screen) -> Unit, tick: Int) {
    val ctx = LocalContext.current
    var editing by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(-1) }
    val states = remember(tick) {
        if (Usage.hasPermission(ctx)) Usage.states(ctx, prefs.distracting) else Array(24) { HourState.FUTURE }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Black)
            .pointerInput(Unit) {
                var dx = 0f; var dy = 0f
                detectDragGestures(
                    onDragStart = { dx = 0f; dy = 0f },
                    onDragEnd = {
                        when {
                            dy < -120f && kotlin.math.abs(dy) > kotlin.math.abs(dx) -> nav(Screen.Drawer)
                            dx > 120f -> nav(Screen.Hub)
                            dx < -120f -> nav(Screen.Settings)
                        }
                    }
                ) { _, d -> dx += d.x; dy += d.y }
            }
            .pointerInput(Unit) { detectTapGestures(onLongPress = { editing = !editing }) }
            .safeDrawingPadding()
            .padding(horizontal = 14.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(14.dp))
            PeakWidget(prefs, tick)
            Spacer(Modifier.height(12.dp))
            ActivityBar(states, Modifier.fillMaxWidth().clickable { nav(Screen.Settings) })
            Spacer(Modifier.height(12.dp))
            GlanceRows(nav, tick)
            Spacer(Modifier.height(16.dp))
            AppGrid(prefs, editing, nav) { picking = it }
            if (editing) {
                Text("edit mode · tap a tile to swap · long-press to exit",
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    style = MaterialTheme.typography.labelSmall, color = Accent, textAlign = TextAlign.Center)
            }
        }
    }

    if (picking >= 0) {
        AppPicker(onPick = { pkg -> prefs.setTile(picking, pkg); picking = -1; editing = false }, onDismiss = { picking = -1 })
    }
}

/** Two-line lowercase date, weather/battery caption under it. Left-padded clear of the camera cutout. */
@Composable
fun PeakWidget(prefs: Prefs, tick: Int) {
    val ctx = LocalContext.current
    val now = remember(tick) { Date() }
    fun f(p: String) = SimpleDateFormat(p, Locale.getDefault()).format(now).lowercase()
    val battery = remember(tick) {
        (ctx.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
    val align = if (prefs.peakRight) Alignment.End else Alignment.Start
    Column(Modifier.fillMaxWidth().padding(start = Dim2.cutoutLeft), horizontalAlignment = align) {
        when (prefs.peakVariant) {
            1 -> Text("${f("EEEE")}, ${f("MMMM d")}  ·  ${f("HH:mm")}", style = MaterialTheme.typography.headlineSmall)
            2 -> {
                Text(f("HH:mm"), style = MaterialTheme.typography.headlineSmall)
                Text("${f("EEEE")}, ${f("MMMM d")}", style = MaterialTheme.typography.bodyMedium, color = Dim)
            }
            else -> {
                Text("${f("EEEE")},", style = MaterialTheme.typography.headlineSmall)
                Text(f("MMMM d"), style = MaterialTheme.typography.headlineSmall)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefs.showWeather) {
                Icon(Icons.Outlined.WbSunny, null, Modifier.size(13.dp), tint = Accent)
                Spacer(Modifier.width(5.dp))
                Text("—°", style = MaterialTheme.typography.bodySmall)   // offline device: no weather feed
                if (prefs.showBattery) Text("  ·  ", style = MaterialTheme.typography.bodySmall, color = DotIdle)
            }
            if (prefs.showBattery) {
                Icon(Icons.Outlined.BatteryStd, null, Modifier.size(13.dp), tint = Accent)
                Spacer(Modifier.width(5.dp))
                Text("$battery%", style = MaterialTheme.typography.bodySmall)
            }
            if (prefs.peakVariant == 0) {
                Text("   ${f("HH:mm")}", style = MaterialTheme.typography.bodySmall, color = Dim)
            }
        }
    }
}

@Composable
fun ActivityBar(states: Array<HourState>, modifier: Modifier = Modifier) {
    Canvas(modifier.height(12.dp)) {
        val slot = size.width / 24
        val r = minOf(3.dp.toPx(), slot / 2f - 1f)
        states.forEachIndexed { i, s ->
            val c = Offset(slot * i + slot / 2f, size.height / 2f)
            when (s) {
                HourState.FUTURE -> drawCircle(DotIdle, r, c, style = Stroke(width = 1.5f))
                HourState.PRODUCTIVE -> drawCircle(White, r, c)
                HourState.UNPRODUCTIVE -> drawCircle(DotBad, r, c)
            }
        }
    }
}

/** Next calendar event + open task count, as two compact bordered rows. */
@Composable
fun GlanceRows(nav: (Screen) -> Unit, tick: Int) {
    val ctx = LocalContext.current
    val event = remember(tick) { runCatching { Agenda.upcoming(ctx, 2).firstOrNull() }.getOrNull() }
    val open = remember(tick) { Store(ctx).tasks().filter { !it.done } }
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        GlanceRow(Icons.Outlined.CalendarToday,
            event?.let { "${it.title.lowercase()} · ${Agenda.when_(it).substringAfter("· ")}" } ?: "no events today",
            null) { nav(Screen.Agenda) }
        GlanceRow(Icons.Outlined.FormatListBulleted,
            open.firstOrNull()?.text ?: "no open tasks",
            if (open.size > 1) open.size else null) { nav(Screen.Todo) }
    }
}

@Composable
private fun GlanceRow(icon: ImageVector, text: String, badge: Int?, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color0E)
            .border(1.dp, Border, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = Accent)
        Spacer(Modifier.width(10.dp))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (badge != null) {
            Box(Modifier.size(19.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                Text("$badge", style = MaterialTheme.typography.labelSmall, color = White)
            }
        }
    }
}

private data class Native(val screen: Screen, val label: String, val icon: ImageVector)

private val NATIVE = mapOf(
    "promptpad:notes" to Native(Screen.Notes, "Note", Icons.Outlined.Description),
    "promptpad:agenda" to Native(Screen.Agenda, "Event", Icons.Outlined.CalendarToday),
    "promptpad:todo" to Native(Screen.Todo, "To Do", Icons.Outlined.FormatListBulleted),
    "promptpad:hub" to Native(Screen.Hub, "Hub", Icons.Outlined.Inbox),
    "promptpad:settings" to Native(Screen.Settings, "Settings", Icons.Outlined.Tune)
)

private val DEFAULT_TILES = listOf(
    "promptpad:notes", "promptpad:agenda", "promptpad:clock", "promptpad:todo",
    "promptpad:phone", "promptpad:sms", "promptpad:camera", "promptpad:hub"
)

/** Well-known system apps get a line icon and a short label, resolved by intent category. */
private val SYSTEM = mapOf(
    "promptpad:clock" to Triple("Clock", Icons.Outlined.Timer, "android.intent.action.SHOW_ALARMS"),
    "promptpad:phone" to Triple("Call", Icons.Outlined.Call, "android.intent.action.DIAL"),
    "promptpad:sms" to Triple("Message", Icons.Outlined.ChatBubbleOutline, "android.intent.action.MAIN|android.intent.category.APP_MESSAGING"),
    "promptpad:camera" to Triple("Camera", Icons.Outlined.PhotoCamera, "android.media.action.STILL_IMAGE_CAMERA")
)

@Composable
fun AppGrid(prefs: Prefs, editing: Boolean, nav: (Screen) -> Unit, onEdit: (Int) -> Unit) {
    val ctx = LocalContext.current
    val tiles = prefs.tiles.ifEmpty { DEFAULT_TILES }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        tiles.chunked(4).forEachIndexed { row, quad ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                quad.forEachIndexed { col, key ->
                    val index = row * 4 + col
                    val native = NATIVE[key]
                    val sys = SYSTEM[key]
                    val label = native?.label ?: sys?.first ?: Apps.label(ctx, key)
                    val icon = native?.icon ?: sys?.second ?: Icons.Outlined.Apps
                    Tile(label, icon, editing, Modifier.weight(1f)) {
                        if (editing) onEdit(index)
                        else if (native != null) nav(native.screen)
                        else if (sys != null) Apps.launchAction(ctx, sys.third)
                        else Apps.launch(ctx, key)
                    }
                }
                repeat(4 - quad.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Tile(label: String, icon: ImageVector, editing: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(11.dp))
            .background(if (editing) Accent.copy(alpha = 0.22f) else Color0E)
            .border(1.dp, if (editing) Accent else Border, RoundedCornerShape(11.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(21.dp), tint = White)
        Spacer(Modifier.height(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
fun AppPicker(onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val apps = remember { Apps.all(ctx) }
    Box(Modifier.fillMaxSize().background(Black.copy(alpha = 0.97f)).clickable { onDismiss() }) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(Dim2.screen)) {
            Header("swap tile", back = onDismiss)
            LazyColumn(Modifier.weight(1f)) {
                items((NATIVE.keys + SYSTEM.keys).toList()) { k ->
                    val label = NATIVE[k]?.label ?: SYSTEM[k]!!.first
                    Row48({ onPick(k) }) { Text("$label (built-in)", style = MaterialTheme.typography.bodyMedium, color = Accent) }
                }
                items(apps) { a -> Row48({ onPick(a.pkg) }) { Text(a.label, style = MaterialTheme.typography.bodyMedium) } }
            }
        }
    }
}
