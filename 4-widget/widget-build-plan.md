# Daily Lift — Home Screen Widget (Phase 2) — Build Plan

**Status note (read this first):** this is the plan exactly as written and approved before implementation began, kept as the historical record. Two things were cut during the build and both have since been restored — the cut is documented in `../2-team-review/DECISION-LOG.md`, Addendum 13, and the restoration in Addendum 14:

- The checkbox **is tappable-to-toggle**, as originally planned in Step 4 below. It was briefly made read-only after on-device testing appeared to trace the failure to Glance's list-rendering reliability. That diagnosis was wrong: the widget was reading its data outside the Glance composition, so it captured the data once when its session started and redrew that same snapshot forever. Taps saved correctly and were painted over — which is why reading the persisted data off the device showed everything working, and why the platform took the blame.
- Resizing **is enabled** (`SizeMode.Exact`), covering the row-count breakpoints in Step 8. It was briefly pinned to `SizeMode.Single` as collateral from the same misdiagnosis — which had its own cost, since under `SizeMode.Single` the size query always returns the declared minimum, silently capping every widget at 2 rows however large it was.

One item from the plan below is fixed but not yet confirmed in the wild: the lazy midnight rollover. The stale-data bug had frozen the widget on a single weekday, and while the fix is in and understood, a live rollover hasn't been observed yet.

Everything else below was built as planned.

---

## Context

The phone app (v1) is built, installed, and tested on-device. The project's own `2-team-review/DECISION-LOG.md` deliberately deferred the home-screen widget to "once the widget is actually being built" (Decision 1, Decision 7) — the data model (stable UUID exercise IDs, synchronous JSON + SharedPreferences storage constructed from a raw `File`/`SharedPreferences` rather than a `Context`-bound database) was built specifically so the widget could reuse it later without a rewrite. That time is now. The widget is the part of the product vision the README calls "central to the concept" (why this is a native app and not a wrapped web app at all).

This plan adds a Jetpack Glance-based home-screen widget that shows today's workout, lets the user check off exercises directly from the widget, and deep-links into the phone app for anything it can't do itself (renaming, adding/deleting exercises, editing weight/reps, viewing exercise detail).

**Decisions locked before this plan** (from `DECISION-LOG.md` Decision 1/7, plus PM choices made when this plan was approved — not re-litigated here):
- No day-navigation on the widget; it always shows today only.
- Weight/reps are read-only on the widget; all editing stays app-only.
- Tapping an exercise's name deep-links into the app, directly to that exercise's `ExerciseDetailScreen` (not just the Today list, not an in-widget overlay — RemoteViews/Glance can't do overlays).
- Checkbox toggle works directly on the widget (no need to open the app). *(Reversed during the build — see status note above.)*
- Long lists get a capped row count with "+N more" overflow text.
- Rollover is "lazy" — the widget recomputes "today" whenever it naturally redraws (periodic ~30 min system update, or any tap), matching the phone app's existing `refreshToday()` pattern. No exact-alarm/AlarmManager.
- One responsive/resizable widget (not a fixed single size): small (~2 rows + overflow), medium (~4 rows + overflow), large (~7 rows, no overflow). *(Temporarily reverted to one fixed size — see status note above.)*

## Approach

### 1. Gradle — add Glance

`gradle/libs.versions.toml`: add `glance = "1.1.1"` (confirmed current stable) plus library entries for `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3`. Add both as `implementation` in `app/build.gradle.kts`.

**Flag, not a blocker:** Glance 1.1.1 was last released mid-2024; this project's Compose BOM (`2026.05.01`) and Kotlin (`2.3.0`) are substantially newer. Gradle's resolver should pick the higher first-party Compose versions transitively, but this isn't guaranteed. **First build step is a bare compile check before writing any widget code** (see Build Order, step 1).

### 2. Shared helper — avoid duplicating construction logic

`TodayViewModel` (`app/src/main/java/com/dailylift/app/today/TodayViewModel.kt`) is a plain Kotlin class (confirmed by reading it — not an `androidx.lifecycle.ViewModel`), so it can be constructed directly from a Glance action callback the same way `MainActivity` does today. To avoid duplicating `TodayViewModel(WorkoutDataStore(...), CompletionStore(...))` construction in three places:

- Move `Context.createTodayViewModel()` out of `MainActivity.kt`'s private scope into a new small file, e.g. `app/src/main/java/com/dailylift/app/AppContainer.kt`.
- Move `PREFS_NAME` (currently a private constant in `MainActivity.kt`) into `CompletionStore`'s companion object as a public `const val`, so the widget package references the same single source of truth.

### 3. Widget-refresh-on-app-edit, without breaking testability

`TodayViewModelTest.kt` constructs `TodayViewModel` directly in plain JUnit today — nothing here should force a real `Context`/Glance call into the constructor. Add one optional constructor parameter:

```kotlin
class TodayViewModel(
    private val workoutDataStore: WorkoutDataStore,
    private val completionStore: CompletionStore,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val onDataChanged: () -> Unit = {},
)
```

Call `onDataChanged()` at the three existing save points: `toggleExerciseChecked`, `updateViewedDay` (covers weight/reps/rename/add/delete), and the stale-completion-reset branch in `loadOrResetCompletion()`. Every existing test keeps passing unmodified (default no-op). `AppContainer.createTodayViewModel()` wires the real one: `onDataChanged = { MainScope().launch { DailyLiftWidget().updateAll(this@createTodayViewModel) } }`.

### 4. New widget package: `app/src/main/java/com/dailylift/app/widget/`

- **`DailyLiftWidgetReceiver.kt`** — `GlanceAppWidgetReceiver` subclass, registered in `AndroidManifest.xml` with the standard `APPWIDGET_UPDATE` intent-filter and `android.appwidget.provider` meta-data pointing at a new `res/xml/daily_lift_widget_info.xml` (sizing, `updatePeriodMillis="1800000"` for the ~30 min lazy refresh, `resizeMode="horizontal|vertical"`).
- **`DailyLiftWidget.kt`** — the `GlanceAppWidget` subclass. `sizeMode = SizeMode.Responsive(...)` with three `DpSize` breakpoints (small/medium/large). `provideGlance(context, id)`: constructs `WorkoutDataStore(context.filesDir)` / `CompletionStore(context.getSharedPreferences(CompletionStore.PREFS_NAME, MODE_PRIVATE))`, recomputes today (mirrors `refreshToday()`'s lazy-rollover logic), builds `TodayUiState` via the existing `buildTodayUiState(...)`, caps the row list based on `LocalSize.current`, and calls `provideContent { }`.
- **Row cap ("+N more") as a pure, unit-tested function** — extract into `today/TodayUiState.kt` or a new `widget/WidgetRowCap.kt` (e.g. `fun capRows(rows: List<ExerciseRow>, max: Int): CappedRows`). This is the one piece of widget logic worth testing in JUnit like the rest of the app's boundary-condition tests.
- **`ToggleExerciseAction.kt`** — a Glance `ActionCallback` that reuses `context.createTodayViewModel()`, calls `refreshToday()` then `toggleExerciseChecked(exerciseId)` (the exact same already-tested logic the phone app uses — no reimplementation), then `DailyLiftWidget().updateAll(context)`. Runs in Glance's own coroutine scope, safe for the stores' blocking I/O.
- **`OpenExerciseDetailAction`** — simplest as `actionStartActivity<MainActivity>(actionParametersOf(EXTRA_EXERCISE_ID to exerciseId))`, no custom callback needed since no state mutation happens.
- Widget UI mirrors the prototype's widget-mode 4-column grid (checkbox, name, weight, reps) from `1-prototype/workout-widget-prototype.html`: header (day + focus), capped exercise rows, footer ("+N more" or "Tap to open the app →"), and the rest-day state reusing `DayContent.Rest`.

### 5. `MainActivity.kt` — deep link to exercise detail

- New constant `EXTRA_EXERCISE_ID`. On `onCreate`, resolve `intent.getStringExtra(EXTRA_EXERCISE_ID)` to an `Exercise` (add `TodayViewModel.findExercise(exerciseId): Exercise?` — small, colocated with the data it already holds, trivially unit-testable) and seed `selectedExercise`'s initial value from it instead of always starting `null`.
- Known, acceptable v1 gap: if `MainActivity` is already in the foreground when a widget tap fires, the app just comes to front without changing `selectedExercise` (no `onNewIntent` handling in the first pass — default launch mode is fine, this is a minor polish item for later, not a blocker).
- Deleted-exercise-tapped-from-a-stale-widget-render resolves to `null` safely (shows `TodayScreen`, not a crash).

### 6. Visual approach

Reuse `ui/theme/Color.kt` constants directly (`AppBackground`, `AppCard`, `AppCardLine`, `AppTextPrimary/Muted/Faint`, `AppGreen`, `AppAccent`) — Glance's color APIs accept plain `androidx.compose.ui.graphics.Color` values as-is.

**Real, accepted constraint:** Glance/RemoteViews has no shadow/elevation support at all — the phone app's recent card shadow (Addendum 12) cannot be replicated on the widget. Fallback: background color + rounded corners + a 1px `AppCardLine` border only (via a small `res/drawable` shape-drawable if Glance's own border modifier proves too limited). This is a deliberate, documented visual gap versus the phone app, not an oversight.

## Build order (incremental, on-device-checkable milestones)

1. Add the Glance dependency only; run `./gradlew assembleDebug` to confirm it resolves cleanly against this project's newer Compose/Kotlin toolchain before writing any widget code.
2. Static shell: receiver + widget + XML + manifest entry, hardcoded `Text("Daily Lift")`. Place it on a home screen, confirm it appears.
3. Wire real read-only data (today's actual workout, no cap, no checkbox action yet).
4. Add the checkbox toggle action; verify a tap persists and is reflected on reopening the phone app.
5. Add the `onDataChanged` refresh hook; verify editing in-app (toggle/rename/add/delete/weight/reps) updates the widget without waiting for the 30-min periodic tick.
6. Add the exercise-detail deep link; verify tapping a name opens the app straight to that exercise's detail screen.
7. Add and unit-test the row-cap function; verify "+N more" appears for a long day.
8. Switch to `SizeMode.Responsive` with the three breakpoints; verify resizing on-device adjusts row count correctly at each size.
9. Visual polish against the prototype's widget-mode mock (colors, border/corner-radius fallback, footer copy, preview image).
10. Rest-day state, empty-day state, stale-tap edge case — run the full manual checklist end to end.

## Verification

- **Unit tests (JUnit, extend existing `app/src/test/` patterns):** `TodayViewModel.findExercise`, the row-cap function, and `onDataChanged` firing on every mutation path (spy/counter lambda, same fixture style as `TodayViewModelTest.kt`).
- **Not unit-testable — manual, on-device, at the milestones above:** actual widget placement/resize on a real home screen, the periodic update timing, tap-to-toggle and tap-to-deep-link end-to-end, visual check against the prototype, and cross-process edit-collision behavior. This gap is real (Robolectric can't simulate true `AppWidgetHost` resize/render behavior) — each build-order milestone above doubles as the manual check for that step, using the same phone you've already got set up (USB debugging on, `adb`/`installDebug` flow already working from the design-fix session).
- Existing unit + instrumented suites (`testDebugUnitTest`, `connectedDebugAndroidTest`) must keep passing unmodified after each milestone, same as the design-fix work just completed.

### Critical files
- `app/src/main/java/com/dailylift/app/today/TodayViewModel.kt`, `TodayUiState.kt`
- `app/src/main/java/com/dailylift/app/MainActivity.kt`
- `app/src/main/java/com/dailylift/app/data/CompletionStore.kt` (PREFS_NAME move)
- `app/build.gradle.kts`, `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`
- New: `app/src/main/java/com/dailylift/app/widget/*`, `app/src/main/java/com/dailylift/app/AppContainer.kt`, `res/xml/daily_lift_widget_info.xml`
