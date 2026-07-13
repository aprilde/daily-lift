# 4 - Widget

The home screen widget: a Jetpack Glance implementation of the same read-only "glanceable" view the prototype demonstrated, built once the phone app (v1) was stable on-device.

**Status: complete, with one scope reversal.** The widget installs, shows today's real workout, and stays in sync with the app. The checkbox turned out not to be reliably tappable-to-toggle on the test device (a Glance/RemoteViews rendering limitation, not an app bug - confirmed by inspecting the app's persisted data mid-debugging) and was made read-only instead; tapping it opens the app. Resizing is temporarily off pending re-verification. Full account of what was tried and why in `../2-team-review/DECISION-LOG.md`, Addendum 13.

## What's here

- `widget-build-plan.md` - the plan approved before this build started, kept as the historical record, with a status note at the top flagging what changed during implementation.

The widget's source code lives at the repo root (`../app/src/main/java/com/dailylift/app/widget/`, plus `../app/src/main/java/com/dailylift/app/AppContainer.kt` and the `res/xml/daily_lift_widget_info.xml` provider config), not in this folder - same convention as `3-build/` for the phone app.

## Why a new numbered stage, not part of `3-build/`

`3-build/` is specifically the v1 phone app's build record. The widget is a distinct phase with its own plan, its own build order, and its own set of on-device-only failure modes (RemoteViews rendering, `SizeMode` behavior, launcher-specific quirks) that don't overlap with the app build. Numbering it `4-widget` keeps the stage-by-stage structure (`1-prototype` -> `2-team-review` -> `3-build` -> `4-widget`) readable at a glance.

## What this stage demonstrates

- Directing an AI tool through a technology (Jetpack Glance / RemoteViews) with real, hard-to-predict platform limitations, not just a straightforward feature build.
- Evidence-based debugging: when tap behavior looked broken, the decisive step was pulling the app's actual persisted data off the device mid-session to prove the failure was in rendering, not logic - rather than continuing to guess at fixes.
- Reversing a "locked" earlier decision (interactive checkbox) once real on-device evidence contradicted it, and reducing scope deliberately rather than shipping something unreliable.
