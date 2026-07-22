# PRD: Daily Lift

## Overview

### Problem Statement

Beginners working out at home - specifically women new to exercise - don't stick with a routine because of two compounding gaps: the routine isn't visible enough to stay top of mind without notifications, and exercise names/instructions alone are meaningless to someone who's never done them. A netnography study of four fitness communities (`docs/DISCOVERY.md`) confirmed both are real and load-bearing, not assumptions: the knowledge-action gap ("I know what to do but can't start") and gendered gym intimidation specifically push this audience toward home workouts, where guidance has to replace what a trainer or gym environment would otherwise provide.

### Solution Summary

A native Android app with a different bodyweight/dumbbell workout for each weekday (rest days on weekends), fully editable by the user, plus a home screen widget that keeps the day's workout glanceable without opening the app. Every exercise carries a plain-language form tip. No accounts, no cloud, no tracking beyond "did I do today's workout" - all data lives on-device.

### Target Users

Women new to exercise, working out at home, who find typical fitness apps intimidating or assume too much prior knowledge (see `docs/DISCOVERY.md` for the research this is grounded in).

## Goals

This is a solo, AI-directed demonstration project (see root `README.md` - "the focus of this repo is the *process*"), not a shipped product with live usage data. Success here is qualitative, not metric-driven:

1. A beginner can look at any exercise and understand what to do without outside help (the form-tip requirement).
2. The routine stays visible without the user having to remember to open the app (the widget requirement).
3. Editing the routine (weights, reps, exercises) is fast enough that it doesn't get abandoned week to week.

### Non-Goals

- Any measure of physical progress (weight loss, strength gains). The discovery research specifically flagged calorie/weight tracking as something to avoid for this audience (compulsive tracking risk) - this is a deliberate absence, not a gap.

## Scope

### Phase 1 - Phone app (complete)

- A different workout for each weekday (Monday-Friday); Saturday/Sunday show a rest-day state.
- Editable weight and reps per exercise. Reps are a single short value (e.g. `"12"`, or `"60 sec"` for timed holds) rather than a range - narrowed during a post-install design pass so the field could be shortened; see DECISION-LOG Addendum 12.
- Add, rename, and delete exercises on any day.
- Check off exercises for **today only**; completion resets automatically at the next calendar day (no exact-alarm, just recomputed whenever the app is opened/resumed).
- Exercise detail screen: name, weight, start/end demo images (currently placeholders - see Future Considerations), plain-language form tip.
- All data local: a JSON file for the workout data, SharedPreferences for today's completion state. No account, no backend, no network calls at all.
- Stable UUID exercise IDs from day one - a deliberate Phase 1 decision specifically so Phase 2 could reuse the data model without a migration (DECISION-LOG, Decision 3).
- Accessibility floor: 48dp tap targets, WCAG-contrast-checked text colors, and a row layout that stays single-line at 100% font scale but relaxes to two lines above that (up to ~130%) rather than clipping.

### Phase 2 - Home screen widget (complete; original scope restored)

- Built with Jetpack Glance. Shows today's real workout - day, focus, exercise list with weight/reps.
- **Tapping a checkbox marks that exercise done for the day, in place, without opening the app.**
- Tapping an exercise's name deep-links into the app, directly to that exercise's detail screen.
- Tapping anything else on the widget opens the app to the Today screen.
- Long exercise lists scroll inside the widget, with a "+N more" line when a day has more exercises than the widget's height allows.
- Widget and app stay in sync in both directions - a checkbox ticked on the widget shows in the app, and an edit made in the app shows on the widget.
- **Previously reversed, now restored:** the tappable checkbox was built, reversed mid-build as unreliable, then restored once the actual cause was found. The original diagnosis - a Glance/RemoteViews rendering limitation on the test device - was wrong. The widget was reading its data *outside* the Glance composition, so the value was captured once when the widget's session started and replayed on every redraw. Saves landed correctly and were then painted over with stale data, which is why inspecting the persisted data mid-debugging showed it working. Full account: DECISION-LOG Addendum 14.
- **Resizing restored:** widget sizes adjust row count to fit again (`SizeMode.Exact`). The fixed single size was collateral from the same misdiagnosis - and under `SizeMode.Single`, `LocalSize` always reported the declared minimum, silently capping every widget at 2 rows regardless of how large it was on screen.
- **Not yet confirmed:** the midnight weekday rollover. The stale-data bug had frozen the widget on one weekday; the fix is in and the mechanism is understood, but a live rollover hasn't been observed yet at time of writing.

### Out of Scope (both phases, deliberate)

- Any account system, cloud sync, or multi-device support.
- Real photography or stock exercise images - all images are AI-generated-style placeholders (see Future Considerations).
- A first-launch onboarding/tutorial flow - explicitly deferred as needing its own discovery work to get the tone right, rather than being bolted on (DECISION-LOG, Decision 8).
- Font scale support above ~130% (Decision 6 covers up to that point only).
- Progress/streak tracking of any kind - see Non-Goals above.

### Future Considerations

- **Real exercise images.** A sample-then-bulk-generate approach (AI-generated, calm tone, every figure a woman) was already decided (DECISION-LOG Addendum 9) but blocked purely on image-generation tooling not being available in the build environment (Addendum 10). Nothing about the design intent needs revisiting - just needs the tooling.
- **Onboarding for first-time users.** Deferred pending dedicated discovery work on tone (per Decision 8) - the two specific anxieties this audience raised in research (sets/reps confusion, unfamiliar exercises) are already addressed by the detail screen and form tips, so this would be additive, not a gap-filler.
- **Re-enable widget resizing.** Already built and working for read-only content (`SizeMode.Responsive`, three breakpoints) - just needs re-verification on-device now that the checkbox is view-only, since the earlier instability was checkbox-specific.
- **Revisit widget checkbox interactivity.** Worth retrying if a future Glance/RemoteViews release proves more reliable, or on a different test device/launcher. The blocker was platform rendering reliability, not the app's approach.
- **200%+ font scale support**, if a future accessibility pass extends past the current ~130% floor.

## Technical Considerations

### Constraints

- Native Android (Kotlin + Jetpack Compose) by deliberate choice, not a wrapped web app - the widget is central to the product concept, and web-wrapper widgets were tried and abandoned during discovery (`docs/DISCOVERY.md`).
- No backend of any kind. `WorkoutDataStore` (JSON file) and `CompletionStore` (SharedPreferences) are both constructed from raw `File`/`SharedPreferences` handles rather than a `Context`-bound database, specifically so the widget process could read/write the same files without a rewrite.
- The widget's own repaint is **eventually consistent**, not synchronous: Glance's background session needs to be "warm" (recently interacted with) to refresh promptly; if the widget has been idle and an edit happens purely in the app, the widget can lag by a few seconds to the next natural redraw. Acceptable given the widget is read-only.
- Glance/RemoteViews has no shadow/elevation support - the phone app's card shadow (Addendum 12) isn't replicated on the widget; background color + rounded corners only.

### Data

Two files, both on-device only: a JSON workout file (days, exercises, weights, reps, tips) and a SharedPreferences entry for today's completion map, keyed by exercise UUID (not list position, so reordering/renaming never corrupts completion state).

## Known Risks / Limitations

- **Widget rendering reliability is device/launcher-dependent.** The checkbox reversal (see Phase 2 above) was specific to the one test device available (Nothing Phone 2a). Behavior on other Android versions/launchers is unverified.
- **This is a single-tester project.** All manual verification happened on one physical device; there's no device-lab or beta-cohort coverage.

## Appendix

### Related Documents

- `2-team-review/DECISION-LOG.md` - every Phase 1 decision and its rationale, plus Addenda 9-13 covering post-build changes (including the full widget debugging trail)
- `2-team-review/build-plan-hardened.md` - the Phase 1 build's hardened plan
- `4-widget/widget-build-plan.md` - the Phase 2 (widget) build plan, with a status note on what changed during implementation
- `docs/DISCOVERY.md` - the research this product's assumptions are grounded in
- `3-build/test-plan.md` - the Phase 1 test plan (46 cases)

