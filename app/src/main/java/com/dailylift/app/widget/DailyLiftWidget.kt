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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Same key name as [EXTRA_EXERCISE_ID] so [actionStartActivity]'s parameter arrives as that intent extra. */
private val exerciseIdParamKey = ActionParameters.Key<String>(EXTRA_EXERCISE_ID)

/** Row-cap thresholds for [rowCapForSize]. */
private val COMPACT_HEIGHT = 150.dp
private val ROOMY_HEIGHT = 250.dp

/**
 * Renders today's actual workout via the same [com.dailylift.app.today.TodayViewModel]/
 * [com.dailylift.app.createTodayViewModel] the phone app uses, so "today" and completion state are
 * never computed twice. The checkbox is read-only (view state only); tapping anywhere on a row
 * opens the app - the name specifically deep-links into that exercise's detail screen.
 *
 * Checkbox tap-to-toggle was tried and pulled after extensive on-device testing: the toggle/save
 * logic itself worked correctly (confirmed by reading the persisted completion data directly off
 * the device), but the widget's own list rendering repeatedly showed checkmarks that didn't match
 * that real data - a Glance/RemoteViews collection-widget rendering reliability issue on this
 * project's test device/launcher, not an app logic bug. Revisit if a future Glance version proves
 * more reliable here.
 *
 * [SizeMode.Single] (fixed size, no resizing) for now, established as the reliable baseline while
 * removing the checkbox interactivity that was implicated in the rendering issue above; resizing
 * can be revisited separately once this baseline is confirmed solid.
 */
class DailyLiftWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val uiState = withContext(Dispatchers.IO) {
            context.createTodayViewModel().uiState
        }
        provideContent {
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
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Box(
                                modifier = GlanceModifier
                                    .size(20.dp)
                                    .cornerRadius(6.dp)
                                    .background(ColorProvider(if (row.checked) AppGreen else AppCardLine)),
                                contentAlignment = Alignment.Center,
                            ) {
                                // Read-only display only - see the class doc for why this isn't
                                // tappable-to-toggle. Tapping still opens the app, consistent with
                                // every other non-editable element on the widget.
                                Text(
                                    text = if (row.checked) "✓" else "",
                                    style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold),
                                    modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
                                )
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
