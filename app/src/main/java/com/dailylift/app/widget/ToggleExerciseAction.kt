package com.dailylift.app.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.dailylift.app.createTodayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Carries which exercise a checkbox tap belongs to. */
internal val toggleExerciseIdKey = ActionParameters.Key<String>("toggleExerciseId")

private const val TAG = "DailyLiftWidget"

/**
 * Marks an exercise done/not-done straight from the widget, through the same
 * [com.dailylift.app.today.TodayViewModel] the phone app uses, so both write the one completion
 * record rather than two that can disagree.
 *
 * Save and redraw are both awaited here, in order, and the view model's own refresh is suppressed
 * with a no-op `onDataChanged`. That sequencing is the whole point: this callback runs inside the
 * widget's Glance session, so a refresh launched into a background scope instead of awaited lands
 * after the session has been torn down and is dropped ("SessionWorker attempted restart but Session
 * is not available"). The save still succeeds, which makes the failure look like a rendering flake -
 * a checkbox that ticks in the data but never on screen.
 *
 * Updating only [glanceId] rather than every instance for the same reason: this is the session that
 * is definitely alive right now.
 */
class ToggleExerciseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val exerciseId = parameters[toggleExerciseIdKey] ?: return
        withContext(Dispatchers.IO) {
            val viewModel = context.createTodayViewModel(onDataChanged = {})
            val before = viewModel.checkedState(exerciseId)
            viewModel.toggleExerciseChecked(exerciseId)
            // Diagnostic while checkbox behaviour is being confirmed on-device: says whether a tap
            // reached the toggle at all and which direction it went. Safe to delete once settled.
            Log.i(TAG, "toggle $exerciseId: $before -> ${viewModel.checkedState(exerciseId)}")
        }
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[REVISION_KEY] = System.currentTimeMillis()
        }
        DailyLiftWidget().update(context, glanceId)
    }
}
