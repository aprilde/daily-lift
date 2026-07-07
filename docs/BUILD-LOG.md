# Build Log

_Last updated by: Claude Sonnet 4.6 (`claude-sonnet-4-6`) — tokens for this
update (measured): ~20,753,650 (covers this update together with the
`BUILD-DIARY.md`, `DECISION-LOG.md`, and `test-plan.md` edits made in the
same response)_

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
other. The full role-by-role findings are written up in
`team-review-summary.md`.

That review surfaced **8 real conflicts** that the draft plan hadn't
resolved - scope (is the widget actually in v1?), the data model (stable IDs
vs. index-based completion), content (how to communicate "3 sets"), visuals
(images vs. text-only), a batch of edge cases (empty workout days,
"Bodyweight" input, name length, custom-exercise images), accessibility
minimums vs. layout density, and how much beginner hand-holding to build in.
Each was worked through one at a time as a PM decision point. **The full
options considered, my responses and rationale, and the final call for all 8
are in `DECISION-LOG.md`** - this log won't restate them.

Headline outcome: the widget moved to v2 (v1 = phone app, built on a
widget-ready data model with stable per-exercise IDs); per-exercise
start/end images became a **hard requirement** for v1, overriding the
engineer's "defer to v2" recommendation; and an accessibility floor
(contrast, tap targets, font-scale) got locked in. The result is
`build-plan-hardened.md` - the plan that wins wherever it disagrees with the
prototype, since every disagreement traces back to one of those 8 decisions,
not an oversight.

**Process note:** also set up token-usage tracking for this project at this
point - going forward, significant deliverables report how many tokens
(measured from the session transcript, not estimated) they took to produce.
Token/cost totals for this review are in `DECISION-LOG.md` and
`team-review-summary.md`.

## Step 7 - Native build (in progress)

Before writing any app code, did a round of pre-build prep on top of
`build-plan-hardened.md`:

- **Clarified what's actually needed from me (the PM)** beyond environment
  setup - mainly: confirming my dev environment works, approving the visual
  style of exercise images (one sample image before the full set is
  generated), and a handful of checks Claude can't do remotely (a 130%
  font-scale change in my phone's Settings, a real-device day-rollover check,
  a screen-reader spot-check).
- **Resolved the "how do the images get made" open question** (left TBD in
  Decision 5 / edge case #9): AI-generated illustrations, calm/friendly
  style, every figure depicted as a woman, with one sample image approved
  before the remaining ~70 are bulk-generated. Recorded as **Addendum 9** in
  `DECISION-LOG.md`.
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

## Step 8 - Steps A-D built, design review loop closed

Steps A-D (environment, data model, the "today" screen, and full editing)
are built and automated-tested. PM manual testing after Step D flagged two
issues - the Bodyweight toggle and the 130%-font-scale layout - which
became Steps D.1 (designer review) and D.2 (implementation), inserted
before Step E.

The headline call from that loop: rather than fix the toggle's labeling as
the designer initially suggested, the PM **removed it entirely** for the
MVP - a reversal of Decision 5/edge case #2, edited directly into
`DECISION-LOG.md` rather than layered on as an addendum. Removing the
toggle also simplified the 130%-layout fix, since the row had one fewer
control to account for. Full reasoning for both is in `DECISION-LOG.md` and
`design-review.md`; the build-level detail (what changed in code, snags,
test results) is in `BUILD-DIARY.md`'s Step D.1/D.2 entries - this log
won't restate either.

Next: Step E (detail view, exercise images, all-done message), pending PM
visual sign-off on D.2.

## Step 9 - Step E built, image generation deferred

D.2's three bugs (scroll, column alignment, default reps text) got PM
sign-off, clearing the way for Step E: the exercise detail view, tap-to-open
navigation, and the all-done celebration message - all built and
automated-tested.

One real gap surfaced going in: Addendum 9 calls for Claude to generate AI
illustrations (a sample image, PM approval, then bulk-generation), but no
image-generation tool is available to Claude in this environment. Rather
than block the rest of Step E on that, the PM chose to build everything
else now - detail view, navigation, all-done message, full data wiring for
all 33 exercises' image refs - with placeholder visuals standing in for the
artwork, and defer actual image generation to a follow-up. Recorded as
**Addendum 10** in `DECISION-LOG.md`; build-level detail in
`BUILD-DIARY.md`'s Step E entry.

# Next: sort out an image-generation approach (likely PM-generated externally,
wired in by Claude), then close out the deferred ME1/ME2 manual tests.
