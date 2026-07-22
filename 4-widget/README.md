# 4 - Widget

The home screen widget: a Jetpack Glance implementation of the "glanceable" view the prototype demonstrated, built once the phone app (v1) was stable on-device.

**Status: complete, original scope restored.** The widget installs, shows today's real workout, stays in sync with the app in both directions, resizes, and its checkboxes toggle completion in place.

It got there by a detour worth recording. The checkbox was cut mid-build as unreliable, on the conclusion that Glance/RemoteViews couldn't render its own state correctly on the test device. That conclusion was wrong, and the widget later froze on a single weekday for the same underlying reason: it read its data *outside* the Glance composition, so the value was captured once when the widget's session started and replayed on every redraw afterwards. Taps saved correctly and were then painted over with stale data - which is exactly why inspecting the persisted data mid-debugging showed everything working, and why the platform got the blame. Addendum 13 in `../2-team-review/DECISION-LOG.md` records the original reversal; Addendum 14 records the correction.

## What's here

- `widget-build-plan.md` - the plan approved before this build started, kept as the historical record, with a status note at the top flagging what changed during implementation.

The widget's source code lives at the repo root (`../app/src/main/java/com/dailylift/app/widget/`, plus `../app/src/main/java/com/dailylift/app/AppContainer.kt` and the `res/xml/daily_lift_widget_info.xml` provider config), not in this folder - same convention as `3-build/` for the phone app.

## Why a new numbered stage, not part of `3-build/`

`3-build/` is specifically the v1 phone app's build record. The widget is a distinct phase with its own plan, its own build order, and its own set of on-device-only failure modes (RemoteViews rendering, `SizeMode` behavior, launcher-specific quirks) that don't overlap with the app build. Numbering it `4-widget` keeps the stage-by-stage structure (`1-prototype` -> `2-team-review` -> `3-build` -> `4-widget`) readable at a glance.

## What this stage demonstrates

- Directing an AI tool through a technology (Jetpack Glance / RemoteViews) with real, hard-to-predict platform behaviour, not just a straightforward feature build.
- Evidence-based debugging: pulling the app's actual persisted data off the device mid-session, rather than guessing at fixes, was what turned "the checkbox doesn't work" into a precise, answerable question.
- Reversing a "locked" earlier decision once on-device evidence contradicted it, and reducing scope deliberately rather than shipping something unreliable.
- **And the limit of that:** the same evidence was read one step too confidently. "Data saves correctly, display is wrong" was taken to mean the renderer was at fault, when it equally fits the renderer being handed stale data - the explanation that turned out to be true. The evidence was sound; the inference skipped a candidate. Cutting scope on a diagnosis that hadn't ruled out the app's own code cost the feature for a while, and left a second bug (the frozen weekday) alive and unexplained.
