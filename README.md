# Daily Lift

**→ See [`PRD.md`](./PRD.md) for the product's full scope by phase - what's built, what's out, what's deferred.**

A learning project demonstrating an AI-assisted product workflow - taking an idea from concept to a working prototype, through a multi-perspective team review, to a scoped build, using AI tools throughout.

The product itself is a home-workout app for beginners (the vehicle). The focus of this repo is the *process*: how a PM can drive an idea to execution by directing AI tools - including the engineering work - rather than writing the code by hand.

![Prototype demo](images/demo.gif)

---

## The process, in four stages

This repo is organized around the lifecycle, so the process is visible at a glance:

| Stage | Folder | What's in it |
|-------|--------|--------------|
| **1 - Prototype** | [`1-prototype/`](./1-prototype/) | The live, interactive HTML prototype - a working mockup of the idea, built iteratively with AI. Open it in any browser. |
| **2 - Team review** | [`2-team-review/`](./2-team-review/) | A structured, multi-role review (engineer, QA, designer, accessibility, a beginner-user voice, and more) that stress-tested the prototype and produced the hardened build plan and a recorded decision log before any code was written. |
| **3 - Build** | [`3-build/`](./3-build/) | The build process itself: the plan it followed, the build journal, the test plan, and a design review of the finished screens. The resulting native Android app (Kotlin + Jetpack Compose) lives at the repo root, alongside this README. |
| **4 - Widget** | [`4-widget/`](./4-widget/) | The home screen widget build (Jetpack Glance), including the plan it followed and a status note on the one scope reversal (interactive checkbox → read-only) discovered through on-device testing. |

Supporting context lives in [`docs/`](./docs/): a brief [discovery note](./docs/DISCOVERY.md) and a running [build log](./docs/BUILD-LOG.md). For what's in/out of scope across both phases, see [`PRD.md`](./PRD.md); for the full rationale behind every decision (including the widget debugging trail), see [`2-team-review/DECISION-LOG.md`](./2-team-review/DECISION-LOG.md).

## What this project demonstrates

- Driving an AI-assisted workflow from idea → prototype → review → build
- Recognizing when an approach isn't working and redirecting quickly
- Orchestrating multiple perspectives (a simulated cross-functional team) to pressure-test a plan
- Making deliberate scope and tradeoff decisions, and recording them
- Directing AI tools through the engineering, rather than writing code by hand

## The product (the vehicle)

A calm, supportive weekly workout routine for beginners - women working out at home who are new to exercise - that stays **visible at a glance** as a home screen widget, with **plain-language form guidance** so each exercise is actually understandable.

Core features (all demonstrated in the prototype):

- A different workout for each weekday, with rest days on weekends
- Editable weights and reps (they change week to week)
- Add, rename, and remove exercises
- Check off completed exercises, with a daily reset
- Plain-language form tips for each exercise (visuals planned for a later version)
- All data saved locally on the device - no account, no cloud

## Two views: widget and app

A key design decision is the split between what lives on the home screen and what lives in the app. The prototype demonstrates both via a toggle - the built widget ended up fully read-only rather than partially interactive, for reasons below:

- **Widget view** - what sits on the home screen. Fully glanceable, fully read-only: today's workout, weight, reps, and completion state, all view-only. Tapping an exercise name opens the app to that exercise's detail screen; tapping anything else opens the app to today's list.
- **App view** - opens from the widget or the app icon. Full editing: check off exercises, tap a value to type, rename exercises, add or remove them.

This split matches the platform (home screen widgets can't take text input, so all editing belongs in the app) but goes a step further than originally planned: the prototype's widget mode let you check off exercises directly, and that was built and tested, but reversed after on-device testing showed the widget's own display couldn't reliably reflect it correctly on the test device - see [`PRD.md`](./PRD.md) and `2-team-review/DECISION-LOG.md` Addendum 13 for the full account.

## Try the prototype

[`1-prototype/workout-widget-prototype.html`](./1-prototype/workout-widget-prototype.html) is a fully working, self-contained prototype - open it in any browser (desktop or mobile). It's a single HTML file with no dependencies, built iteratively with AI.

## Run the app

The native Android app lives at the repo root (`app/`, standard Gradle project layout). Open this repo in Android Studio, or run `./gradlew installDebug` with a connected device or emulator.

The home screen widget installs as part of the same app - after installing, add it to a home screen the normal Android way (long-press an empty area → Widgets → Daily Lift).

## Key product decisions (and why)

- **Native, not a wrapped web app** - because the home screen widget is central to the concept.
- **Widget is fully read-only** - originally planned as read-mostly (view + check off), but the tappable checkbox was reversed after on-device testing showed the widget couldn't reliably display its own state correctly. All editing, including checking off exercises, lives in the app.
- **Text form tips for v1, visuals for v2** - keeps the first version shippable.
- **Local storage only** - no accounts, no servers, no ongoing cost.
- **Resizable widget sizes, temporarily fixed to one** - built and working for the read-only widget, but paused at a single size pending re-verification after the checkbox change.

## Repository structure

```
daily-lift/
├── README.md
├── PRD.md                                      # Product requirements: scope by phase, out of scope, nice-to-haves
├── app/                                         # Android app source (Kotlin + Jetpack Compose)
├── gradle/, gradlew, build.gradle.kts, settings.gradle.kts, gradle.properties
├── 1-prototype/
│   ├── workout-widget-prototype.html           # The live prototype - open in a browser
│   ├── prototype-testing-edits.html            # Earlier version (edit-in-widget approach)
│   └── README.md
├── 2-team-review/
│   ├── stage2-team-review-prompt.md            # Multi-role review prompt
│   ├── team-review-summary.md                  # Summary of the review and conflicts found
│   ├── DECISION-LOG.md                         # Each decision and its rationale
│   ├── build-plan-hardened.md                  # The hardened plan the build follows
│   └── README.md
├── 3-build/
│   ├── stage3-build-prompt-DRAFT.md            # Draft build plan (pre-review input)
│   ├── BUILD-DIARY.md                          # Running build journal
│   ├── test-plan.md                            # Full v1 test plan (46 cases)
│   ├── design-review.md                        # Post-build review of the finished screens
│   ├── design-review-prototype.html            # Reference used during the design review
│   ├── screenshots/                            # Screens captured during the design review
│   └── README.md
├── 4-widget/
│   ├── widget-build-plan.md                    # Home screen widget plan (Phase 2), with a status note on what changed during the build
│   └── README.md
├── docs/
│   ├── DISCOVERY.md                            # Brief discovery note
│   ├── BUILD-LOG.md                            # Running process journal
│   └── Netnography_Fitness-Beginner-Challenges_20260611.txt   # Netnographic study of real beginner fitness discussions
└── images/
    ├── demo.gif                                # Screen recording of the prototype
    ├── edit-in-widget-demo.gif                 # Earlier edit-in-widget approach
    └── final-app-widget-demo.gif               # Final widget-view/app-view split
```

## Status

Prototype complete and tested. Team review complete, with the hardened plan and decision log recorded. Native build complete and installable. Home screen widget complete (checkbox is read-only; see `4-widget/README.md`).
