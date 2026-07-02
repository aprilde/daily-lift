package com.dailylift.app.ui.theme

import androidx.compose.ui.graphics.compositeOver
import org.junit.Assert.assertTrue
import org.junit.Test

/** C7: column-header and rest-day label colors hit >=4.5:1 contrast against the card background (Decision 6). */
class ContrastTest {

    private val minContrast = 4.5

    @Test
    fun faintLabelTextMeetsContrastFloorAgainstCard() {
        val ratio = contrastRatio(AppTextFaint, AppCard)

        assertTrue("Expected >= $minContrast:1, got $ratio:1", ratio >= minContrast)
    }

    @Test
    fun mutedTextMeetsContrastFloorAgainstCard() {
        val ratio = contrastRatio(AppTextMuted, AppCard)

        assertTrue("Expected >= $minContrast:1, got $ratio:1", ratio >= minContrast)
    }

    @Test
    fun primaryTextMeetsContrastFloorAgainstCard() {
        val ratio = contrastRatio(AppTextPrimary, AppCard)

        assertTrue("Expected >= $minContrast:1, got $ratio:1", ratio >= minContrast)
    }

    /** Step D.1/D.2, observation #9: the "Today" badge's text/background combo. */
    @Test
    fun todayBadgeTextMeetsContrastFloorAgainstBadgeBackground() {
        val badgeBackground = AppAccent.copy(alpha = 0.35f).compositeOver(AppCard)
        val ratio = contrastRatio(AppTextPrimary, badgeBackground)

        assertTrue("Expected >= $minContrast:1, got $ratio:1", ratio >= minContrast)
    }
}
