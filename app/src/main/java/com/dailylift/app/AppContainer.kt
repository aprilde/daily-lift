package com.dailylift.app

import android.content.Context
import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.WorkoutDataStore
import com.dailylift.app.today.TodayViewModel
import com.dailylift.app.widget.refreshDailyLiftWidgets
import java.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/** One process-lived scope for fire-and-forget widget refreshes, rather than a fresh scope per edit. */
private val widgetRefreshScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

/**
 * Refresh requests, drained one at a time by a single consumer.
 *
 * Both properties matter. *Serialised*, because two overlapping Glance sessions cancel each other
 * ("SessionWorker attempted restart but Session is not available") and the losing one silently drops
 * its redraw - an edit then fails to appear on the widget until the next periodic tick, which looks
 * like the widget updating late or only sometimes. *Conflated*, because when several edits land in
 * quick succession only the final state is worth drawing; the intermediate ones would just be queued
 * work that redraws something already superseded.
 */
private val refreshRequests = Channel<Context>(Channel.CONFLATED).also { requests ->
    widgetRefreshScope.launch {
        for (context in requests) {
            runCatching { refreshDailyLiftWidgets(context) }
        }
    }
}

/**
 * Constructs a [TodayViewModel] wired to this [Context]'s real on-disk stores. Shared by
 * [MainActivity], the widget and its tap actions (`com.dailylift.app.widget`) so they all read and
 * write the exact same [WorkoutDataStore]/[CompletionStore] files, never divergent copies.
 *
 * Every mutation refreshes the widget via `onDataChanged`, so an edit made in the phone app
 * (toggle/rename/add/delete/weight/reps) and a checkbox tapped on the widget itself both land on the
 * same displayed state.
 *
 * [onDataChanged] overrides that refresh. The widget's own tap actions pass a no-op and then
 * refresh themselves in sequence: a Glance action callback runs inside the widget's session, and a
 * fire-and-forget refresh launched from under it lands after the session has already been torn
 * down, which drops the redraw entirely.
 */
fun Context.createTodayViewModel(
    clock: Clock = Clock.systemDefaultZone(),
    onDataChanged: (() -> Unit)? = null,
): TodayViewModel {
    val context = this
    return TodayViewModel(
        workoutDataStore = WorkoutDataStore(filesDir),
        completionStore = CompletionStore(getSharedPreferences(CompletionStore.PREFS_NAME, Context.MODE_PRIVATE)),
        clock = clock,
        onDataChanged = onDataChanged ?: { context.refreshWidget() },
    )
}

/**
 * Requests a widget refresh, for callers that aren't already in a coroutine. Returns immediately;
 * the work is picked up by [refreshRequests]' single consumer.
 *
 * Queues the application context rather than the caller's: this outlives any Activity.
 */
fun Context.refreshWidget() {
    refreshRequests.trySend(applicationContext)
}
