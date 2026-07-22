package com.dailylift.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dailylift.app.EXTRA_EXERCISE_ID
import com.dailylift.app.MainActivity
import com.dailylift.app.createTodayViewModel
import com.dailylift.app.today.DayContent
import com.dailylift.app.today.TodayUiState
import com.dailylift.app.today.displayName
import com.dailylift.app.today.weightDisplay
import com.dailylift.app.ui.theme.AppCard
import com.dailylift.app.ui.theme.AppCardLine
import com.dailylift.app.ui.theme.AppGreen
import com.dailylift.app.ui.theme.AppTextFaint
import com.dailylift.app.ui.theme.AppTextMuted
import com.dailylift.app.ui.theme.AppTextPrimary

/** Same key name as [EXTRA_EXERCISE_ID] so [actionStartActivity]'s parameter arrives as that intent extra. */
private val exerciseIdParamKey = ActionParameters.Key<String>(EXTRA_EXERCISE_ID)

/** Row-cap thresholds for [rowCapForSize]. */
private val COMPACT_HEIGHT = 150.dp
private val ROOMY_HEIGHT = 250.dp

/** Tap area around the 20dp checkbox graphic, so the whole box takes a tap rather than the glyph alone. */
private val CHECKBOX_TAP_TARGET = 32.dp

/**
 * Renders today's actual workout via the same [com.dailylift.app.today.TodayViewModel]/
 * [com.dailylift.app.createTodayViewModel] the phone app uses, so "today" and completion state are
 * never computed twice. Tapping a checkbox marks that exercise done for today; tapping the name
 * deep-links into that exercise's detail screen.
 *
 * The data read lives *inside* [provideContent] and is keyed on [REVISION_KEY]. That placement is
 * load-bearing, not style. Glance runs [provideGlance] once per widget session and then keeps the
 * resulting composition alive; later `update()` calls recompose that same composition rather than
 * re-running this function. Reading the data above [provideContent] therefore captures it once and
 * replays it forever - which froze the widget on whichever weekday its session happened to start,
 * and made checkbox taps look broken (the toggle really did save, then the widget redrew the same
 * stale snapshot over the top of it). Keying on [REVISION_KEY], which [refreshDailyLiftWidgets]
 * bumps, is what makes a recomposition actually go back to disk.
 *
 * [SizeMode.Exact] because [rowCapForSize] reads [LocalSize]: under [SizeMode.Single] that always
 * reports the provider's declared minimum height, so every widget - however large - capped at 2 rows.
 */
class DailyLiftWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Read the revision purely to subscribe: it is what makes a bump recompose this
            // content at all. The value itself is deliberately unused as a cache key - keying a
            // remember{} on it redraws pre-toggle data whenever the write hasn't propagated to the
            // session's view of state by the time it recomposes.
            currentState(REVISION_KEY)
            // Read synchronously, on every composition. Not produceState/LaunchedEffect: after a
            // checkbox tap the session is torn down within a few hundred milliseconds, and an
            // asynchronous read does not reliably finish inside that window - the composition gets
            // cancelled before it can emit, so the save lands but the widget keeps its old picture.
            // Glance composes off the main thread, so the blocking read is safe here.
            val uiState = context.createTodayViewModel().uiState
            WidgetContent(uiState)
        }
    }
}

/** Short widgets show a couple of rows, tall ones enough that most days need no "+N more" at all. */
@Composable
private fun rowCapForSize(): Int {
    val height = LocalSize.current.height
    return when {
        height < COMPACT_HEIGHT -> 2
        height < ROOMY_HEIGHT -> 4
        else -> 7
    }
}

@Composable
private fun WidgetContent(uiState: TodayUiState) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(AppCard))
            .cornerRadius(16.dp)
            .padding(12.dp),
    ) {
        Text(
            text = uiState.viewedWeekday.displayName(),
            style = TextStyle(color = ColorProvider(AppTextPrimary), fontWeight = FontWeight.Bold),
        )
        when (val content = uiState.content) {
            is DayContent.Workout -> {
                Text(text = content.focus, style = TextStyle(color = ColorProvider(AppTextMuted)))
                val capped = capRows(content.rows, max = rowCapForSize())
                // LazyColumn, not a plain forEach'd Column: a plain loop gave every row's
                // checkbox/name actions the same underlying Android PendingIntent identity
                // (extras like the exercise id don't factor into PendingIntent equality), so
                // tapping any row actually fired whichever row's action was built last. Glance's
                // LazyColumn + a stable itemId per row is the mechanism built to keep per-item
                // actions distinct.
                LazyColumn(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    items(capped.shown, itemId = { it.exercise.id.hashCode().toLong() }) { row ->
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            // The tap target is this outer box, not the checkmark: an unchecked row
                            // draws an empty string, which collapses to zero size and takes no taps.
                            Box(
                                modifier = GlanceModifier
                                    .size(CHECKBOX_TAP_TARGET)
                                    .clickable(
                                        actionRunCallback<ToggleExerciseAction>(
                                            actionParametersOf(toggleExerciseIdKey to row.exercise.id),
                                        ),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(20.dp)
                                        .cornerRadius(6.dp)
                                        .background(ColorProvider(if (row.checked) AppGreen else AppCardLine)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (row.checked) "✓" else "",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                            }
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = row.exercise.name,
                                modifier = GlanceModifier.defaultWeight().clickable(
                                    actionStartActivity<MainActivity>(
                                        actionParametersOf(exerciseIdParamKey to row.exercise.id),
                                    ),
                                ),
                                style = TextStyle(
                                    color = ColorProvider(if (row.checked) AppTextFaint else AppTextPrimary),
                                    textDecoration = if (row.checked) TextDecoration.LineThrough else TextDecoration.None,
                                ),
                            )
                            Text(
                                text = weightDisplay(row.exercise.weight),
                                style = TextStyle(color = ColorProvider(AppTextFaint)),
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = row.exercise.reps,
                                style = TextStyle(color = ColorProvider(AppTextFaint)),
                            )
                        }
                    }
                }
                if (capped.overflowCount > 0) {
                    Text(
                        text = "+${capped.overflowCount} more",
                        style = TextStyle(color = ColorProvider(AppTextFaint)),
                    )
                } else {
                    Text(
                        text = "Tap to open the app →",
                        style = TextStyle(color = ColorProvider(AppTextFaint)),
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                    )
                }
            }
            is DayContent.Rest -> {
                Text(
                    text = "Rest & recover",
                    style = TextStyle(color = ColorProvider(AppTextPrimary), fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "No workout today.",
                    style = TextStyle(color = ColorProvider(AppTextMuted)),
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Next up · ${content.nextUpFocus}",
                    style = TextStyle(color = ColorProvider(AppTextFaint)),
                )
                val preview = content.nextUpExercises.joinToString(", ") + if (content.hasMore) " + more" else ""
                Text(
                    text = preview,
                    style = TextStyle(color = ColorProvider(AppTextMuted)),
                    modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>()),
                )
            }
        }
    }
}
