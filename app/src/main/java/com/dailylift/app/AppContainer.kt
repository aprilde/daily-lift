package com.dailylift.app

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.dailylift.app.data.CompletionStore
import com.dailylift.app.data.WorkoutDataStore
import com.dailylift.app.today.TodayViewModel
import com.dailylift.app.widget.DailyLiftWidget
import java.time.Clock
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Constructs a [TodayViewModel] wired to this [Context]'s real on-disk stores. Shared by
 * [MainActivity] and the widget package (`com.dailylift.app.widget`) so both read/write the exact
 * same [WorkoutDataStore]/[CompletionStore] files, never two divergent copies.
 *
 * Every mutation triggers a fire-and-forget widget refresh via `onDataChanged`, so editing in the
 * phone app (toggle/rename/add/delete/weight/reps) keeps the read-only widget display in sync.
 */
fun Context.createTodayViewModel(clock: Clock = Clock.systemDefaultZone()): TodayViewModel {
    val context = this
    return TodayViewModel(
        workoutDataStore = WorkoutDataStore(filesDir),
        completionStore = CompletionStore(getSharedPreferences(CompletionStore.PREFS_NAME, Context.MODE_PRIVATE)),
        clock = clock,
        onDataChanged = { MainScope().launch { DailyLiftWidget().updateAll(context) } },
    )
}
