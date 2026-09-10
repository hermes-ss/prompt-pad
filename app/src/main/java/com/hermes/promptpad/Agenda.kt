package com.hermes.promptpad

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

data class Event(val id: Long, val title: String, val begin: Long, val location: String?)

internal fun nextLocalMidnight(now: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
    Instant.ofEpochMilli(now).atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

private fun utcDayStart(now: Long, zone: ZoneId): Long =
    Instant.ofEpochMilli(now).atZone(zone).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun eventIsToday(begin: Long, end: Long, allDay: Boolean, now: Long, zone: ZoneId = ZoneId.systemDefault()): Boolean {
    val start = if (allDay) utcDayStart(now, zone) else now
    val limit = if (allDay) start + 86_400_000L else nextLocalMidnight(now, zone)
    return end > start && begin < limit
}

object Agenda {
    fun today(ctx: Context, now: Long = System.currentTimeMillis()): List<Event> =
        query(ctx, now, nextLocalMidnight(now), remainingOnly = true)

    fun upcoming(ctx: Context, days: Int = 7): List<Event> {
        val now = System.currentTimeMillis()
        return query(ctx, now, now + days * 86_400_000L)
    }

    private fun query(ctx: Context, now: Long, end: Long, remainingOnly: Boolean = false): List<Event> {
        // CalendarProvider stores all-day dates at UTC midnight, not local midnight.
        val utcDay = utcDayStart(now, ZoneId.systemDefault())
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath((if (remainingOnly) minOf(now, utcDay) else now).toString())
            .appendPath((if (remainingOnly) maxOf(end, utcDay + 86_400_000L) else end).toString()).build()
        val cols = arrayOf(CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.END, CalendarContract.Instances.ALL_DAY)
        val out = mutableListOf<Event>()
        runCatching {
            ctx.contentResolver.query(uri, cols, null, null,
                CalendarContract.Instances.BEGIN + " ASC")?.use { c ->
                while (c.moveToNext()) {
                    if (remainingOnly && !eventIsToday(c.getLong(2), c.getLong(4), c.getInt(5) != 0, now)) continue
                    out.add(Event(c.getLong(0), c.getString(1) ?: "(no title)", c.getLong(2), c.getString(3)))
                }
            }
        }
        return out
    }

    fun when_(e: Event): String = SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault()).format(Date(e.begin))

    /** Standard geo: intent — whatever map app the user has handles it. */
    fun navigate(ctx: Context, location: String) {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(location)))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { ctx.startActivity(i) }
    }
}
