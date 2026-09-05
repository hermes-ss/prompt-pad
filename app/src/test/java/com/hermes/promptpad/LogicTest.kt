package com.hermes.promptpad

import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test fun initialSearchCursorFollowsForwardedKey() {
        val value = initialSearchValue("p")
        assertEquals("p", value.text)
        assertEquals(TextRange(1), value.selection)
    }

    @Test fun notesDoNotOfferJournalsFolder() {
        assertFalse(Store.FOLDERS.contains("Journals"))
        assertEquals("Personal", visibleNoteFolder("Journals"))
    }

    @Test fun katapultIconMappingCoversKnownApps() {
        assertEquals(R.drawable.whatsapp, Apps.bundledIconForPackage("com.whatsapp"))
        assertEquals(R.drawable.phone, Apps.bundledIconForPackage("com.android.dialer"))
        assertEquals(R.drawable.google, Apps.bundledIconForPackage("com.android.vending"))
        assertEquals(R.drawable.money, Apps.bundledIconForPackage("com.paypal.android.p2pmobile"))
        assertEquals(R.drawable.keyboard, Apps.bundledIconForPackage("it.palsoftware.pastiera"))
    }

    @Test fun inlineMarkersRenderAsSpans() {
        val text = inline("plain **bold** and _it_")
        assertEquals("plain bold and it", text.text)
        assertTrue(text.spanStyles.any { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold })
        assertTrue(text.spanStyles.any { it.item.fontStyle == androidx.compose.ui.text.font.FontStyle.Italic })
    }

    @Test fun listMarkerIsInsertedAtCursorOnANewLine() {
        val result = insertListMarker(androidx.compose.ui.text.input.TextFieldValue("beforeafter", TextRange(6)), "-")
        assertEquals("before\n- after", result.text)
        assertEquals(TextRange(9), result.selection)
    }

    @Test fun listMarkerReplacesSelectionAndLeavesCursorAfterMarker() {
        val result = insertListMarker(androidx.compose.ui.text.input.TextFieldValue("beforeafter", TextRange(6, 11)), "[]")
        assertEquals("before\n[] ", result.text)
        assertEquals(TextRange(10), result.selection)
    }

    @Test fun noteShareTextIncludesTitleWhenPresent() {
        assertEquals("Title\n\nBody", noteShareText(Note(1, "Personal", "Title", "Body")))
        assertEquals("Body", noteShareText(Note(1, "Personal", "", "Body")))
    }
}
