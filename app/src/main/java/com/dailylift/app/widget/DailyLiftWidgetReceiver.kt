package com.dailylift.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DailyLiftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyLiftWidget()

    /**
     * Makes the clock-related broadcasts re-read the data rather than recompose already-loaded
     * values (see [DailyLiftWidget]), so the weekday rolls over at midnight.
     *
     * Deliberately *not* handling [AppWidgetManager.ACTION_APPWIDGET_UPDATE] here: the superclass
     * already starts a Glance session for it, and starting a second one alongside had the two
     * cancelling each other's `SessionWorker`, which left the widget on its loading spinner
     * indefinitely. [onUpdate] handles the periodic tick instead, in one path.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in CLOCK_ACTIONS) {
            refreshInBackground(context)
        }
        super.onReceive(context, intent)
    }

    /**
     * Replaces the superclass's update rather than adding to it. The revision bump has to land
     * before the recomposition that reads it, and [refreshDailyLiftWidgets] does both in that order
     * in one coroutine; calling `super.onUpdate` as well would start a competing Glance session,
     * which is what left the widget spinning. The superclass's own `onUpdate` does nothing more than
     * trigger that update, so there is nothing else to preserve here.
     *
     * This periodic `updatePeriodMillis` tick is the dependable path, and is what caps how long a
     * wrong weekday can survive - roughly half an hour past midnight, worst case. The [CLOCK_ACTIONS]
     * broadcasts only make the rollover immediate when the system delivers them.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        refreshInBackground(context)
    }

    private fun refreshInBackground(context: Context) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                refreshDailyLiftWidgets(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val CLOCK_ACTIONS = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
