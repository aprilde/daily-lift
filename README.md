# Daily Lift

A home-workout app concept for beginners — designed for women working out at home who are new to exercise and want a clear, encouraging, glanceable routine without the intimidation of typical fitness apps.

This repository documents the product thinking and the working prototype, from problem through to a phased build plan.

---

## The problem

Most workout apps assume you already know what you're doing. For a true beginner, a list of exercise names ("renegade row," "RDL") is meaningless, and gym-oriented apps can feel intimidating rather than welcoming. On top of that, staying consistent usually relies on nagging push notifications.

The goal: a calm, supportive weekly routine that's **visible at a glance** (eventually as a home screen widget, so it stays top of mind without notifications), with **plain-language form guidance** so beginners actually understand each move.

## The approach

This started as a simpler idea — display a Google Sheet of workouts in an Android widget — and evolved, through prototyping, into a dedicated app concept. The journey itself surfaced the real requirements:

- Different workout for each weekday, with rest days on weekends
- Editable weights and reps (they change week to week)
- Add, rename, and remove exercises
- Check off completed exercises, with daily reset
- Plain-language form tips for each exercise (visuals planned for a later version)
- All data saved locally on the device — no account, no cloud

## The prototype

[`prototype.html`](./prototype.html) is a fully working, self-contained prototype. Open it in any browser (desktop or mobile) to try it:

- Auto-switches to today's workout
- Flip between days with the ‹ › controls
- Tap any weight or reps value to edit; tap ✏️ to rename an exercise
- Tap an exercise name for a detail popup with a form tip
- Check off exercises (resets each new day); see an encouraging message when all are done
- Add or remove exercises per day
- Rest-day screen on weekends with a preview of Monday
- Edits persist locally between sessions

It's built as a single HTML file with no dependencies, intentionally, so it's easy to share, easy to iterate on, and easy to hand to a build tool as a spec.

## From prototype to product — the build plan

The prototype is the design source of truth. The real product is planned as a **native Android app** (Kotlin + Jetpack Compose) in two deliberate phases:

**Phase 1 — the app.** All viewing, editing, form tips, and local storage. See [`claude-code-prompt-phase1-app.md`](./claude-code-prompt-phase1-app.md).

**Phase 2 — the home screen widget.** A standard, resizable widget showing today's workout with tap-to-check and tap-to-open, reading the same locally-saved data. (Planned — prompt to follow.)

### Key product decisions (and why)

- **Native, not a wrapped web app** — because the home screen widget is central to the concept, and a widget is where native is clearly worth the extra cost.
- **Widget is read-mostly** — viewing and checking off live on the widget; all typing-based editing lives in the app. Widgets can't take text input, so this split matches the platform rather than fighting it.
- **Text form tips for v1, visuals for v2** — keeps the first version shippable; richer visuals (illustrations or short clips) are a deliberate next step.
- **Local storage only** — no accounts, no servers, no ongoing cost.
- **Standard resizable widget sizes** — the user picks what fits their home screen.

## Repository contents

```
daily-lift/
├── prototype.html                      # The working prototype — open in a browser
├── claude-code-prompt-phase1-app.md    # Build plan / spec for the native app (Phase 1)
└── README.md
```

## Status

Prototype complete. Native build not yet started. Phase 2 (widget) prompt to be added.
