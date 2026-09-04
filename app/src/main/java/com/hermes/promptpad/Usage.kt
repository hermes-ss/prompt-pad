package com.hermes.promptpad

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

enum class HourState { FUTURE, PRODUCTIVE, UNPRODUCTIVE }

/**
 * Hourly foreground usage from UsageStatsManager, bucketed into the 24 hours of today.
 * ponytail: recompute on resume instead of a background service; the bar is only visible on Home.
 */
object Usage {

    fun hasPermission(ctx: Context): Boolean = try {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 86_400_000L, now).isNotEmpty()
    } catch (e: Exception) { false }

    /** Milliseconds spent in distracting packages, per hour of today. */
    fun distractingMsPerHour(ctx: Context, distracting: Set<String>): LongArray {
        val out = LongArray(24)
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return out
        val start = startOfToday()
        val now = System.currentTimeMillis()
        val events = try { usm.queryEvents(start, now) } catch (e: Exception) { return out }
        val e = UsageEvents.Event()
        var openPkg: String? = null
        var openAt = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            when (e.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> { openPkg = e.packageName; openAt = e.timeStamp }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (openPkg != null && openPkg in distracting) spread(out, openAt, e.timeStamp, start)
                    openPkg = null
                }
            }
        }
        if (openPkg != null && openPkg in distracting) spread(out, openAt, now, start)
        return out
    }

    fun states(ctx: Context, distracting: Set<String>, thresholdMs: Long = 10 * 60_000L): Array<HourState> {
        val per = distractingMsPerHour(ctx, distracting)
        val hourNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return Array(24) { h ->
            when {
                h > hourNow -> HourState.FUTURE
                per[h] >= thresholdMs -> HourState.UNPRODUCTIVE
                else -> HourState.PRODUCTIVE
            }
        }
    }

    /** Splits a [from,to] foreground span across the hour buckets it covers. */
    internal fun spread(out: LongArray, from: Long, to: Long, dayStart: Long) {
        if (to <= from) return
        var cur = maxOf(from, dayStart)
        while (cur < to) {
            val hour = (((cur - dayStart) / 3_600_000L).toInt()).coerceIn(0, 23)
            val hourEnd = dayStart + (hour + 1) * 3_600_000L
            val slice = minOf(to, hourEnd)
            out[hour] += slice - cur
            cur = slice
        }
    }

    fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    /** Total foreground time today across all apps, used for the Focus daily limit. */
    fun totalForegroundMsToday(ctx: Context): Long {
        val usm = ctx.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0
        val start = startOfToday()
        return try {
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, System.currentTimeMillis())
                .filter { it.packageName != ctx.packageName }
                .sumOf { it.totalTimeInForeground }
        } catch (e: Exception) { 0 }
    }
}
