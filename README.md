# Daily Lift

**A learning project demonstrating an AI-assisted product workflow — taking an idea from rough concept to a working, tested prototype and a scoped build plan, using AI tools throughout.**

The product itself is a home-workout app for beginners (the vehicle); the focus of this repo is the *process* — how the idea was explored, where the approach was redirected, and the product decisions made along the way.

![Prototype demo](images/demo.gif)

---

## What this project demonstrates

Driving an AI-assisted workflow from idea → prototype → build spec
Recognizing when an approach isn't working and redirecting quickly
Evaluating off-the-shelf options and articulating why they don't fit
Making deliberate scope and tradeoff decisions, not just generating output
Translating a working prototype into a clear specification for a build

My role here is product-side: scoping, evaluating, deciding, and steering the AI — not hand-writing the code. The native build is in progress; this repo captures the work through a complete, tested prototype.

## How this was built

This project was built through an AI-assisted workflow — exploring an idea, hitting a wall with the first approach, redirecting, prototyping, and scoping the real build. Rather than detail every step here, the full story (including dead ends and decisions) lives in a running build log that grows as the project continues:

**→ See [`BUILD-LOG.md`](./BUILD-LOG.md) for the full process.**

---

## The problem

Most workout apps assume you already know what you're doing. For a true beginner, a list of exercise names ("renegade row," "RDL") is meaningless, and gym-oriented apps can feel intimidating rather than welcoming. Staying consistent also usually depends on nagging push notifications.

The goal: a calm, supportive weekly routine that stays **visible at a glance** as a home screen widget — so it stays top of mind without notifications — with **plain-language form guidance** so beginners actually understand each move.

## The approach

This began as a simpler idea — display a Google Sheet of workouts in an Android widget — and evolved, through prototyping, into a dedicated app concept. The journey itself surfaced the real requirements:

- A different workout for each weekday, with rest days on weekends
- Editable weights and reps (they change week to week)
- Add, rename, and remove exercises
- Check off completed exercises, with a daily reset
- Plain-language form tips for each exercise (visuals planned for a later version)
- All data saved locally on the device — no account, no cloud

## Two views: widget and app

A key design decision is the split between what lives on the home screen and what lives in the app. The prototype demonstrates both via a toggle:

- **Widget view** — what sits on the home screen. Glanceable and read-only, but you can still check off exercises (the one daily action worth having on a widget). Tapping it opens the app.
- **App view** — opens when you tap the widget. Full editing: tap a value to type, rename exercises, add or remove them.

This split matches the platform: home screen widgets can't take text input, so all editing naturally belongs in the app, while the widget stays fast and glanceable.

## Try the prototype

[`prototype/workout-widget-prototype.html`](./prototype/workout-widget-prototype.html) is a fully working, self-contained prototype — open it in any browser (desktop or mobile):

- Auto-switches to today's workout
- Toggle between widget view and app view
- Flip between days with the ‹ › controls
- App view: tap any weight or reps value to edit; tap to rename; add or remove exercises
- Tap an exercise name for a detail popup with a form tip
- Check off exercises (resets each new day); see an encouraging message when all are done
- Rest-day screen on weekends with a preview of Monday
- Edits persist locally between sessions

It's built as a single HTML file with no dependencies — easy to share, iterate on, and hand to a build tool as a spec.

## From prototype to product — the build plan

The prototype is the design source of truth, and the Claude Code prompt that kicks off the real build lives alongside it in `prototype/`. The actual native app work — and the installable result — will live in `build/`.

The real product is planned as a **native Android app** (Kotlin + Jetpack Compose) in two deliberate phases:

**Phase 1 — the app.** All viewing, editing, form tips, and local storage. See [`prototype/claude-code-prompt-phase1-app.md`](./prototype/claude-code-prompt-phase1-app.md).

**Phase 2 — the home screen widget.** A standard, resizable widget showing today's workout with tap-to-check and tap-to-open, reading the same locally-saved data. (Planned — prompt to follow.)

### Key product decisions (and why)

- **Native, not a wrapped web app** — because the home screen widget is central to the concept, and a widget is where native is clearly worth the extra cost.
- **Widget is read-mostly** — viewing and checking off live on the widget; all typing-based editing lives in the app, matching the platform rather than fighting it.
- **Text form tips for v1, visuals for v2** — keeps the first version shippable; richer visuals (illustrations or short clips) are a deliberate next step.
- **Local storage only** — no accounts, no servers, no ongoing cost.
- **Standard resizable widget sizes** — the user picks what fits their home screen.

## Repository structure

```
daily-lift/
├── README.md
├── prototype/                                  # The vibe-coding stage
│   ├── workout-widget-prototype.html           # The working prototype — open in a browser
│   └── claude-code-prompt-phase1-app.md        # Build plan / spec handed to Claude Code
├── build/                                      # The native app work + installable result (in progress)
└── images/
    └── prototype-demo.gif                      # Screen recording of the prototype
```

## Status

Prototype complete and demonstrated. Native build not yet started. Phase 2 (widget) prompt to be added.
