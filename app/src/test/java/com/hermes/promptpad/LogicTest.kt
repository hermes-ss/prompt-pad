package com.hermes.promptpad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test fun searchPrefersPrefixMatches() {
        val apps = listOf("Telegram", "Settings", "Tetris").map { AppEntry(it, it, it, 0, null) }
        assertEquals(listOf("Telegram", "Tetris"), Apps.search(apps, "te").map { it.label })
        assertEquals(3, Apps.search(apps, "").size)
    }

    @Test fun appSpecPreservesProfileIdentity() {
        val app = AppEntry("Mail", "mail.pkg", "mail.Activity", 12, null)
        assertEquals("mail.pkg\tmail.Activity\t12", app.spec)
    }

    @Test fun inlineMarkersRenderAsSpans() {
        val text = inline("plain **bold** and _it_")
        assertEquals("plain bold and it", text.text)
        assertTrue(text.spanStyles.any { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold })
        assertTrue(text.spanStyles.any { it.item.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic })
    }
}
