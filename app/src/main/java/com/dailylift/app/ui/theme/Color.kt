package com.dailylift.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Dark palette ported from `workout-widget-prototype.html`'s CSS variables, used directly by
 * [com.dailylift.app.today.TodayScreen] for its bespoke card UI.
 *
 * [AppTextFaint] replaces the prototype's `--faint` (rgba(255,255,255,0.35)) for column headers
 * and the rest-day "Next up" label, which only reached ~3.2:1 contrast against [AppCard]
 * (Decision 6). This value hits >=4.5:1 — see `ContrastTest`.
 */
val AppBackground = Color(0xFF0F1424)
val AppCard = Color(0xFF1A1A2E)
val AppCardLine = Color(0x1AFFFFFF)
val AppTextPrimary = Color(0xFFFFFFFF)
val AppTextMuted = Color(0x8CFFFFFF)
val AppTextFaint = Color(0xFFA8ACB8)
val AppGreen = Color(0xFF34A853)
val AppAccent = Color(0xFF6C8CFF)

/**
 * WCAG relative-luminance contrast ratio between [foreground] (composited over [background] if
 * translucent) and [background], in the range [1, 21].
 */
fun contrastRatio(foreground: Color, background: Color): Double {
    val resolved = if (foreground.alpha < 1f) foreground.compositeOver(background) else foreground
    val l1 = resolved.luminance().toDouble()
    val l2 = background.luminance().toDouble()
    val lighter = max(l1, l2)
    val darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}
