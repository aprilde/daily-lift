package com.dailylift.app.widget

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll

/**
 * Bumped on every refresh to give the widget's composition something observable to react to.
 *
 * The widget's data comes off disk, and a disk read is not Compose state - reading it can't
 * invalidate anything. Glance calls [DailyLiftWidget.provideGlance] once per widget session and then
 * keeps that composition alive, so an `update()` on its own only re-runs the same composition
 * against the same already-read values. Writing a new value here is what actually tells the
 * composition "what you read is out of date, read it again".
 */
internal val REVISION_KEY = longPreferencesKey("revision")

/**
 * The single way this app refreshes the widget: bump [REVISION_KEY] on every placed instance, then
 * recompose.
 *
 * Every refresh path goes through here - phone-app edits, the widget's own checkbox taps, the
 * periodic update and the date-change broadcasts - so no path can recompose without also re-reading
 * the data underneath. Calling [updateAll] directly is the bug this function exists to prevent.
 */
suspend fun refreshDailyLiftWidgets(context: Context) {
    val revision = System.currentTimeMillis()
    GlanceAppWidgetManager(context).getGlanceIds(DailyLiftWidget::class.java).forEach { glanceId ->
        updateAppWidgetState(context, glanceId) { prefs -> prefs[REVISION_KEY] = revision }
    }
    DailyLiftWidget().updateAll(context)
}
