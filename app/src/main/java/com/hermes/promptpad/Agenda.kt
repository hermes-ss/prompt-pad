package com.hermes.promptpad

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Event(val id: Long, val title: String, val begin: Long, val location: String?)

object Agenda {
    fun upcoming(ctx: Context, days: Int = 7): List<Event> {
        val now = System.currentTimeMillis()
        val end = now + days * 86_400_000L
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(now.toString()).appendPath(end.toString()).build()
        val cols = arrayOf(CalendarContract.Instances.EVENT_ID, CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN, CalendarContract.Instances.EVENT_LOCATION)
        val out = mutableListOf<Event>()
        runCatching {
            ctx.contentResolver.query(uri, cols, null, null, CalendarContract.Instances.BEGIN + " ASC")?.use { c ->
                while (c.moveToNext()) {
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
