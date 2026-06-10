# Build Log

A running journal of building Daily Lift through an AI-assisted workflow — the decisions, dead ends, and course-corrections along the way. Newest entries are added at the bottom as the project continues.

The point of this log isn't a tidy success story; it's an honest record of the *process* — including what didn't work and why I changed direction.

---

## Stage 1: The original idea

Started narrow: display a Google Sheet of workouts in an Android home screen widget, switching to a different tab based on the day of the week. The appeal was keeping a workout visible and top of mind without notifications.

Explored free web-widget apps to render the sheet on the home screen.

## Stage 2: Hitting a wall

The web-widget approach couldn't deliver a readable, well-controlled layout — fonts rendered tiny, margins were cut off, and there was no real control over how the page displayed inside the widget. Tried multiple apps and scaling workarounds.

**Decision:** rather than keep forcing a path that wasn't working, treat the repeated friction as a signal to step back and rethink the approach. This turned out to be the most important judgment call of the project.

## Stage 3: Evaluating alternatives

Considered prebuilt workout apps as a shortcut. They were polished, but none matched the specific need: a day-based, glanceable home screen widget paired with genuinely beginner-friendly guidance for someone who doesn't know the exercises. Ruled them out for concrete reasons rather than settling.

## Stage 4: Prototyping the real concept

Shifted to building a dedicated concept and used AI to vibe-code a self-contained, interactive prototype. Iterated on layout, features, and behavior — adding inline editing, completion tracking, form tips, add/remove exercises, and a widget-vs-app view split — until it matched the vision and was testable in a browser.

## Stage 5: Scoping the real build

Turned the finished prototype into a phased, reviewable specification to hand to an AI coding tool, deliberately isolating the hardest part (the home screen widget) into its own phase so it wouldn't sink the whole build.

---

## Lessons so far

- **Knowing when to abandon an approach mattered more than any single prompt.** The biggest time saver was recognizing the web-widget path was a dead end early enough to pivot.
- **Prebuilt isn't always faster.** Evaluating and rejecting off-the-shelf options with clear reasons was worth the time.
- **Scope the hard part separately.** Isolating the widget into its own phase keeps the rest of the build from being held hostage to the trickiest piece.

---

<!-- Add new entries below as the build progresses -->
## Stage 6: Native build (in progress)

_To be updated as the native Android build proceeds: environment setup, what the AI coding tool got right and wrong, errors hit and how they were resolved, and decisions made along the way._
