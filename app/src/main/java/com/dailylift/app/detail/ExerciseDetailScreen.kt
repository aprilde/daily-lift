package com.dailylift.app.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailylift.app.data.Exercise
import com.dailylift.app.today.weightAccessibilityLabel
import com.dailylift.app.today.weightDisplay
import com.dailylift.app.ui.theme.AppBackground
import com.dailylift.app.ui.theme.AppCard
import com.dailylift.app.ui.theme.AppTextFaint
import com.dailylift.app.ui.theme.AppTextMuted
import com.dailylift.app.ui.theme.AppTextPrimary

private const val BACK_TAP_TARGET_DP = 48
private const val IMAGE_PLACEHOLDER_HEIGHT_DP = 160

/** Ported from `workout-widget-prototype.html`'s `.popup .pic` (`linear-gradient(135deg, #2a2f52, #1a1d33)`). */
private val ImagePlaceholderGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2A2F52), Color(0xFF1A1D33)),
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)

/**
 * Exercise detail view: name, weight, start/end images, and the form tip. Opened by tapping an
 * exercise's name on the Today screen. Originally specified as a "weight/reps line"
 * (build-plan-hardened.md, "Screens & behavior" point 6) - reps was dropped per PM direction
 * (DECISION-LOG.md Addendum 11): reps already lives on the Today screen's editable row, and
 * showing it again here, read-only, next to a picture of the exercise didn't make sense.
 *
 * The start/end images are placeholders for now, not real artwork - generating AI illustrations
 * is deferred (see DECISION-LOG.md Addendum 10: no image-generation tool is available to Claude
 * in this environment). [exercise.imageStartRef]/[exercise.imageEndRef] are still threaded through
 * so wiring real drawables later only touches [ExerciseImagePlaceholder].
 */
@Composable
fun ExerciseDetailScreen(exercise: Exercise, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(BACK_TAP_TARGET_DP.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .clearAndSetSemantics { contentDescription = "Back" },
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = AppTextPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = exercise.name, color = AppTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = weightDisplay(exercise.weight),
            color = AppTextMuted,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(start = BACK_TAP_TARGET_DP.dp + 4.dp)
                .semantics { contentDescription = weightAccessibilityLabel(exercise.weight) },
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ExerciseImagePlaceholder(
                label = "Start",
                exerciseName = exercise.name,
                modifier = Modifier.weight(1f),
            )
            ExerciseImagePlaceholder(
                label = "End",
                exerciseName = exercise.name,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AppCard)
                .padding(14.dp),
        ) {
            Text(text = "FORM TIP", color = AppTextFaint, fontSize = 9.5.sp, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = exercise.tip, color = AppTextPrimary.copy(alpha = 0.9f), fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

/**
 * Stand-in for the AI-generated start/end illustration that will replace it later. The box's
 * gradient background and "Demo image of: {name}" copy pattern are ported directly from
 * `workout-widget-prototype.html`'s `.popup .pic` placeholder; the "Start"/"End" label above it
 * is this app's own addition, per Decision 4's requirement that the two images be labeled.
 */
@Composable
private fun ExerciseImagePlaceholder(label: String, exerciseName: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = AppTextFaint, fontSize = 11.sp, letterSpacing = 0.5.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IMAGE_PLACEHOLDER_HEIGHT_DP.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ImagePlaceholderGradient)
                .padding(12.dp)
                .semantics {
                    contentDescription = "$label position demo image of $exerciseName"
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Demo image of:\n$exerciseName",
                color = AppTextFaint,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
            )
        }
    }
}
