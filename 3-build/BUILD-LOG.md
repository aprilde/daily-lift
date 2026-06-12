# Build Log

_Last updated by: Claude Sonnet 4.6 (`claude-sonnet-4-6`) — tokens for this
update (measured): ~503,700 (covers this update and the new `CLAUDE.md`,
produced in the same response)_

A running journal of building Daily Lift through an AI-assisted workflow - the decisions, dead ends, and course-corrections along the way. Newest entries are added at the bottom as the project continues.

The point of this log isn't a tidy success story; it's an honest record of the *process* - including what didn't work and why I changed direction.

---

## Step 1 - The original idea

Started narrow: display a Google Sheet of workouts in an Android home screen widget, switching to a different tab based on the day of the week. The appeal was keeping a workout visible and top of mind without notifications.

Explored free web-widget apps to render the sheet on the home screen.

## Step 2 - Hitting a wall

The web-widget approach couldn't deliver a readable, well-controlled layout - fonts rendered tiny, margins were cut off, and there was no real control over how the page displayed inside the widget. Tried multiple apps and scaling workarounds.

**Decision:** rather than keep forcing a path that wasn't working, treat the repeated friction as a signal to step back and rethink the approach. This turned out to be the most important judgment call of the project.

## Step 3 - Evaluating alternatives

Considered prebuilt workout apps as a shortcut. They were polished, but none matched the specific need: a day-based, glanceable home screen widget paired with genuinely beginner-friendly guidance for someone who doesn't know the exercises. Ruled them out for concrete reasons rather than settling.

## Step 4 - Prototyping the real concept

Shifted to building a dedicated concept and used AI to vibe-code a self-contained, interactive prototype. Iterated on layout, features, and behavior - adding inline editing, completion tracking, form tips, add/remove exercises, and a widget-vs-app view split - until it matched the vision and was testable in a browser.

## Step 5 - Scoping the real build

Turned the finished prototype into a phased, reviewable specification to hand to an AI coding tool, deliberately isolating the hardest part (the home screen widget) into its own phase so it wouldn't sink the whole build.

---

## Lessons so far

- **Knowing when to abandon an approach mattered more than any single prompt.** The biggest time saver was recognizing the web-widget path was a dead end early enough to pivot.
- **Prebuilt isn't always faster.** Evaluating and rejecting off-the-shelf options with clear reasons was worth the time.
- **Scope the hard part separately.** Isolating the widget into its own phase keeps the rest of the build from being held hostage to the trickiest piece.
- **A structured, multi-role review caught real gaps before any code existed.** Having Claude argue from distinct (and sometimes conflicting) roles - engineer, accessibility specialist, a beginner-user stand-in, etc. - surfaced tradeoffs a single straight-through pass would have missed (stable IDs, mandatory exercise images, accessibility minimums), and recording every decision plus its rationale as it happened made the resulting plan traceable back to *why*, not just *what*.
- **Having Claude test its own work first cuts down what's left for me to check.** Building an automated-test layer into the plan up front - JUnit/Compose UI tests that Claude writes, runs, and fixes before pausing - means most of the test plan (34 of 46 cases) never lands on my plate at all.

---

<!-- Add new entries below as the build progresses -->
## Step 6 - Team review and plan hardening (Stage 2 -> Stage 3)

Before handing anything to an AI coding tool to actually build, ran a
structured "team review" of the prototype and the draft plan
(`stage3-build-prompt-DRAFT.md`). Had Claude play six distinct roles - Senior
Staff Engineer, Android Platform Specialist, QA Engineer, Product Designer,
Accessibility Specialist, and "Maya" (a stand-in for the actual end user: a
beginner working out at home) - each reviewing the prototype and draft plan
from its own priorities, deliberately not allowed to just agree with each
other.

That review surfaced **8 real conflicts** that the draft plan hadn't
resolved: whether the home-screen widget is actually in v1, how to
communicate "3 sets" to the user, index-based vs. stable-ID completion
tracking, visual richness vs. build cost, a batch of edge cases (empty
workout day, "Bodyweight" input, long exercise names, missing images for
custom exercises), accessibility minimums vs. layout density, and how much
beginner hand-holding to build in. Each was worked through one at a time as a
PM decision point and recorded - with the options considered and the
rationale - in `DECISION-LOG.md`.

**Key decisions that came out of this:**
- **Widget moved to v2.** v1 is the phone app only, but the data model is
  built widget-ready (stable per-exercise UUIDs, a JSON file +
  SharedPreferences split) so v2 doesn't require a rewrite.
- **"3 sets" gets a single static header line** ("Do 3 sets of each exercise
  below") instead of a new data-model field - solves the "how many rounds?"
  confusion Maya raised at zero structural cost.
- **Every exercise gets a stable `id`**; completion tracking keys off `id`,
  not array position - replaces the prototype's fragile index-reshifting
  delete logic.
- **Overrode the engineer's "defer visuals to v2" recommendation.** Per-
  exercise images became a hard requirement for v1 - both a start-position
  and an end-position image - because a beginner needs to see correct form
  and may not recognize an exercise from its name alone.
- **Resolved a batch of edge cases:** an emptied-out workout day shows "Rest
  Day" (with "Add exercise" still available, not a dead end); a dedicated
  "Bodyweight" toggle replaces guessing at magic strings; exercise names are
  capped at ~30 characters; user-added exercises get a generic placeholder
  image pair.
- **Locked an accessibility floor:** 4.5:1 minimum contrast, 48x48dp tap
  targets, and a 130% font-scale row-wrap fallback (instead of clipping).
- **No first-launch onboarding in v1** - the two specific anxieties Maya
  raised are already addressed by the "3 sets" line and the new images;
  a general "welcome" message needs its own discovery work to get the tone
  right, so it's parked for v2.

The result is `build-plan-hardened.md` - this is the plan that wins wherever
it disagrees with the prototype, since every disagreement traces back to one
of the 8 decisions above, not an oversight.

**Process note:** also set up token-usage tracking for this project at this
point - going forward, significant deliverables report how many tokens
(measured from the session transcript, not estimated) they took to produce.

## Step 7 - Native build (in progress)

Before writing any app code, did a round of pre-build prep on top of
`build-plan-hardened.md`:

- **Clarified what's actually needed from me (the PM)** beyond environment
  setup - mainly: confirming my dev environment works, approving the visual
  style of exercise images (one sample image before the full set is
  generated), and a handful of checks Claude can't do remotely (a 130%
  font-scale change in my phone's Settings, a real-device day-rollover check,
  a screen-reader spot-check).
- **Decided on the image approach:** AI-generated illustrations (not
  photos), calm/friendly style matching the app's tone, every figure depicted
  as a woman to match the target audience. One sample image gets generated
  and approved before the remaining ~70 are bulk-generated.
- **Added an automated-testing layer to `build-plan-hardened.md`** - at each
  build step, Claude now writes and runs JUnit unit tests and Compose UI
  tests via Gradle, and fixes failures itself, *before* asking me to test
  anything.
- **Created `test-plan.md`** - every test case (46 total) derived from the
  build plan and decision log, each marked Automated (Claude) or Manual (PM).
  34 of 46 are automated; the remaining manual items are mostly quick
  "does this feel right" passes, with only ~4 being hard requirements
  (dev environment setup, the image-style approval, and the font-scale
  check).
- **Established a documentation convention:** new project documents open
  with the Claude model version used and a measured token count, and
  `test-plan.md` includes a recommendation on which Claude model to use for
  which part of the build (Sonnet 4.6 by default, Opus as a fallback for the
  foundational data-model step or if Claude gets stuck debugging).

_To be updated as the native Android build proceeds: environment setup, what
the AI coding tool got right and wrong, errors hit and how they were
resolved, and decisions made along the way._
