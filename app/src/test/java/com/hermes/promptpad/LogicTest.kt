package com.hermes.promptpad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {

    private val day = 0L
    private val h = 3_600_000L

    @Test fun spreadSplitsAcrossHourBuckets() {
        val out = LongArray(24)
        Usage.spread(out, day + h * 2 + 30 * 60_000L, day + h * 4, day)   // 02:30 -> 04:00
        assertEquals(30 * 60_000L, out[2])
        assertEquals(h, out[3])
        assertEquals(0L, out[4])
    }

    @Test fun spreadIgnoresEmptyAndInvertedSpans() {
        val out = LongArray(24)
        Usage.spread(out, day + h, day + h, day)
        Usage.spread(out, day + h * 5, day + h * 2, day)
        assertEquals(0L, out.sum())
    }

    @Test fun spreadClampsBeforeMidnight() {
        val out = LongArray(24)
        Usage.spread(out, day - h, day + 30 * 60_000L, day)               // started yesterday
        assertEquals(30 * 60_000L, out[0])
    }

    @Test fun searchPrefersPrefixMatches() {
        val apps = listOf("Telegram", "Settings", "Tetris").map { AppEntry(it, it, null) }
        assertEquals(listOf("Telegram", "Tetris"), Apps.search(apps, "te").map { it.label })
        assertEquals(listOf("Settings", "Tetris"), Apps.search(apps, "t").map { it.label }.filter { it != "Telegram" }.sorted())
        assertEquals(3, Apps.search(apps, "").size)
    }

    @Test fun monkBlocksBrowsersRegardlessOfTime() {
        assertTrue(BlockService.shouldBlock(2, 30, emptySet(), "com.android.chrome", 0))
        assertFalse(BlockService.shouldBlock(2, 30, emptySet(), "com.hermes.notes", 0))
    }

    @Test fun focusBlocksOnlyPastTheDailyLimit() {
        val d = setOf("com.google.android.youtube")
        assertFalse(BlockService.shouldBlock(1, 30, d, "com.google.android.youtube", 29 * 60_000L))
        assertTrue(BlockService.shouldBlock(1, 30, d, "com.google.android.youtube", 30 * 60_000L))
        assertFalse(BlockService.shouldBlock(1, 30, d, "com.hermes.notes", 99 * 60_000L))
    }

    @Test fun unrestrictedBlocksNothing() {
        assertFalse(BlockService.shouldBlock(0, 1, setOf("x"), "x", Long.MAX_VALUE))
    }

    @Test fun inlineMarkersRenderAsSpans() {
        val a = inline("plain **bold** and _it_")
        assertEquals("plain bold and it", a.text)
        assertTrue(a.spanStyles.any { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold })
        assertTrue(a.spanStyles.any { it.item.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic })
    }
}
