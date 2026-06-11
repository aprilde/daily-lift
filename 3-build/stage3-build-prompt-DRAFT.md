# Stage 3: Build Plan (DRAFT - pre-review input)

*A prompt to paste into Claude Code.*

> **Status: DRAFT.** This is the raw build plan that goes *into* the Stage 2 team
> review (`stage2-team-review-prompt.md`). The review will harden it
> into the final build spec. Don't build from this version directly - run the
> review first, then build from the hardened plan it produces.

---

## The build prompt

> Paste everything below the line into Claude Code. It is written so Claude Code
> builds the app in reviewable steps and checks in with you between them.
> A working prototype HTML file is attached separately - share it with Claude Code
> as the source of truth for layout, behavior, and the default workout data.

---

I am not a developer. I'm building a native Android app called **Daily Lift** and I'll need you to guide me through everything, including environment setup, in plain language. Explain what you're doing as you go, and stop to let me test after each phase rather than building everything at once.

## What the app is

A simple home-workout app for beginners (the target users are women working out at home who are new to exercise). It shows a different workout for each weekday, lets the user track and edit their workouts, and saves everything locally on the device. **There is no login, no cloud, no backend, no analytics.** All data stays on the device.

A home screen widget is planned for a later phase - DO NOT build the widget yet - but design the data storage so a widget could read the same saved data later (see "Data storage" below).

## Build environment first

Before any app code, walk me through setting up everything I need to build and run an Android app, since I have none of it: Android Studio, the SDK, and running the app either on an emulator or my physical phone. Confirm my environment can build and launch a blank app before we go further.

## Tech choices (keep it simple)

- Language: **Kotlin**
- UI: **Jetpack Compose**
- Storage: local only - use **DataStore** (or a simple JSON file in app storage) so the data is easy to read later from a widget. No Room/SQLite unless you think it's clearly better for widget access; if so, explain why in one sentence.
- Minimum SDK: pick a sensible modern default and tell me what you chose and why.
- No third-party services, no network permission needed.

## Data model

Match the attached prototype. The data is a set of weekdays, each with a "focus" label and a list of exercises. Each exercise has: name, weight (a string, in pounds, may be empty or "bodyweight"), reps (a string), and a form tip (a string). Seed the app with the default workout data from the prototype on first launch. Saturday and Sunday are rest days (no exercise list).

Store the user's data in a single local structure that persists across app restarts, and is straightforward for a separate widget process to read later.

## Screens & behavior (mirror the prototype exactly)

1. **Main screen = today's workout.** On open, show the workout for the current weekday automatically.
2. **Day navigation.** Left/right controls to flip to other days. Each day shows its focus label (e.g. "Lower body").
3. **Rest day screen** for Sat/Sun: a calm "Rest & recover" screen with a short message and a preview of Monday's first few exercises.
4. **Per exercise, the user can:** check it off as done (completion resets daily); edit the weight; edit the reps; rename it; delete it (with a confirm); and tap it to open a detail view showing its form tip (the visuals/images come in a later version - for now the detail view shows the name, weight, reps, and the text form tip).
5. **Add exercise** button per day that adds a new row the user can fill in.
6. When all of today's exercises are checked off, show a brief encouraging message.

Match the visual style of the prototype: dark card aesthetic, clean and friendly, large readable text. The tone is supportive and beginner-friendly, never intimidating.

## How to proceed

Work in clear steps and pause after each so I can run it on my phone and confirm before continuing:

- **Step A:** Environment setup + a blank app that launches.
- **Step B:** Data model + seed data + local persistence (show me it saves and reloads).
- **Step C:** The main "today" screen and day navigation (read-only first).
- **Step D:** Editing - check off, edit weight/reps, rename, add, delete.
- **Step E:** Exercise detail view with the form tip, plus the rest-day screen and the all-done message.

At each step, tell me exactly what to tap/run to verify it works, and keep the code organized and commented so it's easy to extend with a widget later.

(Attach the prototype HTML file to this Claude Code session so it can use the exact layout, behavior, and default workout data.)
