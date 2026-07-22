# Daily Lift — v1 Build Plan: Decision Log

_Last updated by: Claude Sonnet 4.6 (`claude-sonnet-4-6`)_

**Status: FINALIZED (Stage 2 review).** All 8 conflicts from the Stage 2 team review resolved. This log is the complete record of every PM call and the rationale behind it. The resulting plan is `build-plan-hardened.md`. **Six addenda** (#9-14) were added after this log's original "FINALIZED" status, during Stage 3 build, a post-install design pass, the Phase 2 widget build, and a later widget debugging session that corrected one of the earlier addenda's conclusions — see the Addendum section below.

Conflicts resolved:
1. Is the widget actually in v1?
2. Missing "sets" in the data model
3. Stable exercise IDs vs. index-based completion
4. Visual richness vs. build cost
5. Edge-case triage
6. Accessibility minimums vs. layout density
7. Widget interactivity — lock the v1 scope
8. Beginner hand-holding vs. simplicity

---

## 1. Is the home-screen widget actually in v1?

**Options on the table:**
- A. Phone app first — v1 = working phone app + widget-ready data model (stable IDs, JSON/SharedPrefs split). Widget becomes v2.
- B. Widget ships in v1 as a Step F, with scope locked per Android Specialist constraints.

**Recommended:** A.

**PM response:** Agreed with the recommendation — no pushback.

**Decision:** A — phone app is v1. Widget is v2.

**PM's stated rationale:** "Best for testing the app product 1st before the larger effort of a widget."

**Additional rationale:** Validate the core app product first before taking on the widget's platform-specific complexity (PendingIntents, RemoteViews, midnight-rollover handling). The data model will be built widget-ready now so v2 is additive, not a rebuild.

---

## 2. Communicating "sets" (static 3 sets, not per-exercise/customizable)

**Original framing — two options for whether/how sets get a data-model home:**
- A. Content-only fix — bake "sets" into the existing `reps` string via a seed-data rewrite, no schema change.
- B. Add a real, editable `sets` field to the data model (default 3) with its own small UI.

**PM response:** Pushed back on the premise before either option was chosen — rejected the idea that sets would ever need to be per-exercise or user-editable, and reframed the question to "how do we communicate one static '3 sets' fact."

**PM's stated rationale:** "The sets will [have] no change and don't need to be customized."

**Revised options (after reframe):**
- A. One-time static header note per workout day ("Do 3 sets of each exercise below"). No data model change, no per-row content rewrite.
- B. Bake "3x" into every reps string ("3x 8-12 reps", etc.) — no schema change, but lengthens every row and tightens column width.

**Recommended:** A.

**PM response (on revised options):** Agreed with the recommendation — no further pushback.

**Decision:** A — single static header line, said once per day view.

**Additional rationale:** Solves Maya's "how many rounds?" confusion at zero structural cost, and avoids cramming "3x" into the already-tight reps column (relevant later for widget/accessibility column width). AMRAP → "as many as you can" content fix proceeds as a separate small seed-data tweak regardless.

---

## 3. Stable exercise IDs vs. index-based completion

**Options on the table:**
- A. Index-based, matches the prototype exactly. No `id` field; `deleteExercise` keeps index-reshifting logic. Cost shifts to v2 (data migration when widget needs stable IDs).
- B. Add a stable `id` field now (UUID at creation). Completion map keys off `id`. Simplifies v1's own delete logic; zero migration for v2.

**Recommended:** B.

**PM response:** Agreed with the recommendation — no pushback, no additional rationale offered beyond accepting B.

**Decision:** B — add stable `id` field now.

**Additional rationale:** Tiny implementation delta (one field, generated once) that actually simplifies v1's delete logic and avoids migrating real, customized user data when the widget arrives in v2.

---

## 4. Visual richness vs. build cost (image placeholder + per-exercise visuals)

**Round 1 — options on the table:**
- A. Drop the image area entirely for v1 — text-only detail view, all visuals deferred to v2 (Engineer's original recommendation).
- B. Hard requirement: per-exercise visuals, with a sub-decision on static images vs. animated GIF/video.

**Recommended:** A.

**PM response (Round 1):** Pushed back hard — rejected A entirely and overrode it with a hard requirement that per-exercise visuals (images or GIFs) ship in v1, not v2.

**PM's stated rationale:** "Images or gifs for each exercise [are] a hard requirement. It is important a beginner like Maya gets her posture and form correct, plus she might not even know what an RDL exercise is. These are 2 frictions that will be removed for a beginner like Maya with visuals specific to each exercise."

**Round 2 — given the hard requirement, sub-decision on format:**
- A. Static images per exercise (start position + reference) — bundled drawables, trivial app-size impact, simple Compose `Image`.
- B. Animated GIFs/short video per exercise — better for showing movement/tempo, but real app-size cost, a new playback dependency, and a much bigger asset-production lift.

**Recommended:** A (static).

**PM response (Round 2):** Agreed with the recommended format (static, not GIF/video) — but extended it: a single static image per exercise wasn't enough; required **both a start-position image and an end-position image**.

**PM's stated rationale:** "Static images for v1 is good as long as there is a start and end visual."

**Decision:** Hard requirement overrides Round 1's Position A. Static images for v1 (not GIF/video), with **both a start-position image and an end-position image per exercise** (~35 exercises x 2 = ~70 image assets).

**Additional rationale:** Directly removes two concrete Maya frictions — not recognizing an unfamiliar exercise (e.g., "what does an RDL look like") and uncertainty about correct posture/form — at a fraction of the asset-production and engineering cost of GIFs/video. Bundled as local drawables; no network permission; trivial app-size impact.

**Build implications:**
- Step B (data model): each exercise gets `imageStartRef` + `imageEndRef` fields pointing to bundled drawables.
- Step E (detail view): renders both images as a labeled start/end pair.
- New task: source/create ~70 simple static images alongside Step E.

---

## 5. Edge-case triage

**Already resolved/N/A (no decision needed):** widget/app sync (N/A v1, per Decision 1), deleting last exercise/index-reshifting (resolved by Decision 3), reordering exercises (not a v1 feature), completion resets daily + non-today days always show unchecked (confirmed intentional), rest-day "Next up" hardcoded to Monday (acceptable given fixed Mon-Fri schedule).

**Items needing a call:**

- **#1 Empty workout day** (all exercises deleted)
  - *Recommended:* an empty-state message ("No exercises yet — add one below") in place of the list, with "Add exercise" still visible.
  - *PM response:* Modified the recommendation — kept the underlying concept (empty-state message + retained "Add exercise" button) but changed the wording.
  - *PM's stated direction:* "message should read 'Rest Day'."
  - *Decision:* show **"Rest Day"** in place of the exercise list, with the "Add exercise" button still available below it so the user isn't stuck on a dead-end screen.

- **#2 "Bodyweight" weight input**
  - *Decision:* free-text entry — the user types "bodyweight" directly into the existing weight field (case-insensitive match, already handled by `weightDisplay`/`weightAccessibilityLabel`). No dedicated toggle in v1.

- **#3 Long exercise names**
  - *Recommended:* cap rename input at ~30 characters.
  - *PM response:* Agreed — no pushback.
  - *Decision:* cap rename input at **~30 characters**. Confirmed.

- **#9 Missing images for user-added custom exercises** (new edge case introduced by Decision 4)
  - *Recommended:* one generic placeholder start/end image pair as fallback for exercises without dedicated art.
  - *PM response:* Implicitly accepted the fallback recommendation as-is, and additionally answered an open question carried over from Decision 4 (how the ~70 default-exercise images get sourced) — not a pushback, but added new scope-clarifying direction.
  - *PM's stated direction:* "the designer agent can generate these images in the build stage."
  - *Decision:* one generic placeholder start/end image pair as fallback for custom exercises. The ~70 default-exercise images (Decision 4) will be **generated during the Stage 3 build** by the build-stage "Designer" role — generation method TBD at build time, not sourced externally by the PM.
  - *Resolved:* see **Addendum 9** below for the generation method (decided during Stage 3 pre-build prep).

- **#10 Stale tip/images for renamed or custom exercises**
  - *Recommended:* acceptable for v1, defer tip/image editing to v2.
  - *PM response:* Agreed — no pushback.
  - *Decision:* confirmed acceptable for v1 — no edit UI for tip or images; deferred to v2.

---

## 6. Accessibility minimums vs. layout density

**Options on the table:**
- A. Accessibility floor — fix contrast (replace `--faint`-equivalent for all meaningful text, ≥4.5:1, free); expand tap targets to 48dp via padding (visual icons unchanged); define a 130% font-scale floor with row-wrap fallback (name on top, weight/reps/toggle/actions below) instead of clipping. 200% scale deferred to v2.
- B. Density-first — fix contrast only; leave tap targets and font-scale handling as-is (single-line dense row, as in the prototype); full compliance becomes a v2 polish pass.

**Recommended:** A.

**PM response:** Agreed with the recommendation — no pushback, no additional rationale offered beyond "I'm fine with your recommendation here."

**Decision:** A.

**Additional rationale:** Contrast and touch-target padding are nearly free and preserve the dense visual at default (100%) font scale — only users who've increased their text size see the row relax to two lines. Given the stated audience (older users, fitness-app newcomers, possibly one-handed with weights), shipping under Material's own tap-target minimum is the wrong place to cut for v1.

---

## 7. Widget interactivity — lock the v1 scope

**Status:** Largely resolved by Decision 1 (widget moved to v2) — no options were presented and no PM vote was requested.

**PM response:** N/A — this conflict was not put to the PM as a decision point; it was presented as already resolved by Decision 1's ripple effect, with one carryover requirement noted below.

Most of this conflict's original substance — no day-navigation on the widget, tap-on-name deep-links instead of an overlay (overlays aren't possible in RemoteViews), capped row count with "+N more" overflow, per-row checkbox PendingIntents, and exact-alarm vs. "lazy" midnight rollover — becomes a v2 planning question, to be revisited once the widget is actually being built, informed by the data model already hardened in Decisions 3-5.

**One concrete v1 carryover (no vote needed — basic correctness, not a tradeoff):** the phone app must recompute "today" on resume/foreground (not just at first launch), and refresh completion state for the new date if the day rolled over while the app was backgrounded. This is the phone-app half of the Android Specialist's "lazy rollover" approach and applies with or without a widget. Folded into Steps C/D of the hardened plan.

---

## 8. Beginner hand-holding vs. simplicity

**Options on the table:**
- A. No onboarding screen — rely on per-exercise tips + the new start/end images (Decisions 2 and 4) to carry reassurance.
- B. One-time first-launch banner ("New to working out? Go at your own pace... You've got this.") — one boolean flag + one dismissible component.

**Recommended:** A.

**PM response:** Agreed with the recommendation — no pushback.

**Decision:** A — no onboarding in v1.

**PM's stated rationale:** "A works for the MVP, onboarding will need more discovery to accomplish correctly."

**Additional rationale:** The two specific anxieties Maya named (sets/reps confusion, unfamiliar exercises/posture) already have targeted fixes via Decisions 2 and 4. A generic welcome message is a tone-setting feature that deserves its own discovery work to get right, rather than being bolted on now. Revisit for v2 with proper discovery.

---

## Addendum 9. Image-generation method (resolves Decision 5 / edge case #9's "TBD at build time")

**Status:** Added during Stage 3 pre-build prep, after this log's original "FINALIZED" status. Decision 5 (edge case #9) deliberately left the *generation method* for the ~70 default-exercise images open — this entry resolves that.

**Open question carried over from Decision 5:** how should the ~70 default-exercise start/end images (Decision 4) actually be produced?

**PM's stated direction:** "images can be AI generated, it's fine if it generates as an illustration. Make sure the styling is calm and all the people are women displaying the exercises. I would like to review one image and approve before moving forward with creating the rest."

**Decision:** AI-generated illustrations (not photos), in a calm, friendly style consistent with the app's supportive tone, with **every human figure depicted as a woman** (matching the target audience). **One sample image** (one exercise's start position) is generated first and **paused for PM approval** of style/tone before the remaining ~69 images plus the generic placeholder pair (Decision 5, edge case #9) are bulk-generated in the same approved style.

**Additional rationale:** Consistent with Decision 4 (images exist to remove Maya's "what does this look like / am I doing it right" friction) and Decision 8 (calm, supportive tone, no intimidation) — illustrations of women performing the exercises reinforce that this app is built for someone like Maya, not a generic stock-photo gym app.

**Build implications:** Step E of `build-plan-hardened.md` includes this image-generation approach with the sample-approval gate as an explicit pause point.

---

## Addendum 10. Image generation deferred — no image-generation tool available

**Status:** Added during Step E build, after this log's original "FINALIZED" status.

**Blocker:** Addendum 9 calls for Claude to generate one sample AI illustration, get PM approval, then bulk-generate the remaining ~66 exercise images plus the placeholder pair. Claude has no image-generation tool available in this environment (confirmed via tool search — no DALL-E/Imagen/MCP image tool is accessible) and cannot produce the artwork itself.

**Options on the table:**
- A. PM generates images externally (ChatGPT/Midjourney/etc.) using a style prompt Claude writes, then hands files back for Claude to wire in.
- B. Build Step E now with placeholder visuals in place of real artwork; defer image generation to a separate follow-up task.
- C. Skip AI-generated illustrations entirely; Claude draws simple vector/line-art graphics directly in Compose instead.

**PM response:** Chose B — no pushback on deferring the artwork itself.

**Decision:** B. Step E's detail view, all-done message, and all data wiring (33 unique exercises × start/end `imageStartRef`/`imageEndRef`, generic placeholder pair) are built and automated-tested now. `Exercise.imageStartRef`/`imageEndRef` are still threaded through correctly so wiring real drawables later only touches `ExerciseImagePlaceholder` in `ExerciseDetailScreen.kt` — no data-model or navigation change needed.

**Placeholder styling, corrected:** the first pass used a generic figure emoji in a plain card-colored box, which wasn't checked against `workout-widget-prototype.html` first. PM flagged it — the prototype's actual popup placeholder (`.popup .pic`) uses a `linear-gradient(135deg, #2a2f52, #1a1d33)` background with centered "Demo image of: {name}" copy, not an icon. Corrected to port that gradient and copy pattern directly, split into labeled Start/End halves (the label itself is this app's own addition, since the prototype's placeholder wasn't split). Also surfaced and fixed separately: the detail view's full-screen-with-back-button presentation (vs. the prototype's modal popup with a dimmed backdrop) was an unflagged deviation from the prototype on Claude's part, not a recorded decision — PM reviewed and explicitly chose to keep full-screen for the real app, so that part stands as now-confirmed, not a silent gap.

**Additional rationale:** This keeps the rest of Step E (which has nothing to do with image generation - the detail view layout, the all-done celebration, the navigation entry point) from being blocked on an external dependency. Addendum 9's actual style decision (AI-generated, calm, every figure a woman) stays valid and unchanged for whenever image generation happens - this addendum defers the *production* of the assets, not the prior decision about what they should look like.

**Build implications:** `test-plan.md`'s ME1 (sample-image approval gate) and ME2 (open detail views, confirm images/tips feel right) are deferred until image generation actually happens. E6 (every exercise resolves to an image resource that exists, no missing-drawable crash) doesn't apply in its original form since there are no drawable resources yet - adapted instead to unit tests confirming every seeded exercise (and the placeholder pair) has well-formed, correctly-paired `imageStartRef`/`imageEndRef` strings ready to wire in later (see `SeedWorkoutDataTest.seededExerciseResolvesToItsOwnImageRefsAndTip` and `.repeatedExerciseNameAcrossDaysResolvesToTheSameImageRefs`).

---

## Addendum 11. Detail view drops reps — weight only

**Status:** Added during Step E follow-up, after this log's original "FINALIZED" status.

**PM's stated direction:** "remove any mention of reps on the image page, it doesn't make sense."

**Decision:** the detail view (`ExerciseDetailScreen.kt`) shows weight only, not reps. This reverses part of `build-plan-hardened.md`'s "Screens & behavior" point 6, which specified a "weight/reps line" for this screen.

**Rationale:** reps already lives on the Today screen's editable row — repeating it read-only next to the exercise's demo image added information that didn't serve a purpose specific to this screen. Weight stays since it's still a useful at-a-glance reference alongside the images.

---

## Addendum 12. Today screen design pass — card depth, row density, and reps format

**Status:** Added in a post-install design review, after this log's original "FINALIZED" status. PM installed the v1 build on a physical device and found the card looked flat and rows misaligned versus `workout-widget-prototype.html`.

**Bug found, not a decision:** the card in `TodayScreen.kt` had no border and no shadow at all — `AppCardLine` (the prototype's `--line` border color) was already defined in `Color.kt` but was never actually applied anywhere. Fixed by adding a 1dp `AppCardLine` border and a drop shadow to the card, matching the prototype's `.card` styling. Also widened the weight/reps column to 70px and row/header horizontal padding to 16dp to match the prototype's grid exactly (both were previously narrower, a small unflagged deviation).

**Decision (row layout - reopens Decision 6):** Decision 6 intended each exercise row to render as a single dense line at 100% font scale (matching the prototype), relaxing to two lines only above 100% scale. The shipped code never implemented that condition — it always rendered two lines, at every scale. This read as "strange alignment" versus the prototype.

- *Options:* (A) implement the font-scale-conditional single-row layout Decision 6 originally intended; (B) keep the always-two-line behavior as shipped.
- *PM response:* Chose A.
- *Complication found during implementation:* a single row with the checkbox, rename, and delete icons all at the full 48dp accessibility tap-target size (Decision 6) doesn't fit next to the 70dp weight and 92dp reps fields on real phone widths — the fixed columns alone exceed a typical ~360dp screen before any room is left for the exercise name.
- *Options presented:* (A) 32dp icons at default (100%) font scale, matching the prototype's density, with the full 48dp floor returning automatically once the user increases their phone's text size (the existing two-line layout becomes the >100%-scale fallback); (B) keep 48dp icons always and abandon the single-row match.
- *PM response:* Chose A.
- **Decision:** `ExerciseRowView` now branches on `LocalDensity.current.fontScale`. At <=100% it renders `CompactExerciseRow`, a single dense row (checkbox, name, rename icon, weight, reps, delete) with 32dp tap targets, matching the prototype's grid. Above 100% it renders `ExpandedExerciseRow`, the prior two-line layout, with the full 48dp tap targets. `TodayScreenEditingTest.coreControlsHaveAtLeast48dpTapTargetsAtDefaultScale` and the new `coreControlsReturnTo48dpAtLargerFontScale` test both scale points.

**Decision (weight/reps field width and reps format):** PM asked for the exercise name to get more room, the weight field narrowed to ~3 characters, and the reps field matched to the same width — which only works if reps values are short. The seed data's reps were a mix of ranges ("8-12 reps"), per-side counts ("8-10 ea leg"), a time-based hold ("30-60 sec"), and one open-ended AMRAP ("as many as you can"), none of which fit a 3-character field as-is.

- *PM's stated direction, given in three parts:* (1) ranges collapse to their upper bound and drop the word "reps" (e.g. "8-12 reps" -> "12"); (2) per-side qualifiers ("ea leg"/"ea side") drop too, down to a bare number; (3) time-based holds (Wall sit, Plank) keep a unit ("60 sec") so a bare number can't be misread as a rep count; (4) Push-ups' open-ended AMRAP gets a concrete target number instead ("15").
- **Decision:** `WEIGHT_COLUMN_WIDTH_DP`/`REPS_COLUMN_WIDTH_DP` (70/92) collapsed into one `NUMERIC_FIELD_WIDTH_DP = 40` constant used by both fields. Every exercise in `SeedWorkoutData.kt` and `Exercise.new()`'s default reps ("10 reps" -> "10") updated to the new short format per the rules above.
- **Tradeoff, flagged not silently made:** per-side reps ("do 10 *each leg*") no longer carry that qualifier anywhere in the UI - the number alone doesn't say "each side." PM chose this over keeping the longer text and accepting a wider field.

---

## Addendum 13. Home screen widget (Phase 2 build) — checkbox interactivity reversed after on-device evidence

**Status:** Added after this log's original "FINALIZED" status. Decision 1/Decision 7 deferred the widget to "once it's actually being built" - this addendum is that build.

**Context:** Built with Jetpack Glance (`androidx.glance:glance-appwidget:1.1.1`), reusing the existing `WorkoutDataStore`/`CompletionStore`/`TodayViewModel` directly (a new `AppContainer.kt` centralizes construction so the widget and `MainActivity` never diverge). Confirmed PM choices going in: lazy rollover (matches the phone app's existing pattern, no exact-alarm), tapping a name deep-links to that exercise's detail screen, one responsive/resizable size.

**Decision (checkbox interactivity - reverses part of Decision 7):** Decision 7 called for the widget's checkbox to toggle completion directly via a tap, no app open required. Built and extensively tested on-device; ultimately reversed.

- *What was tried, in order, each with real on-device testing:* (1) a tappable checkbox using `actionRunCallback` - toggled but didn't visually update reliably; (2) `SizeMode.Responsive` → `SizeMode.Exact` for the resize-time click failures; (3) removing a redundant concurrent widget-refresh path that was racing itself; (4) converting the row list to Glance's `LazyColumn` with a stable per-row `itemId`, after finding taps on one row were firing another row's action entirely (root cause: Android's `PendingIntent` equality doesn't consider intent extras, so multiple rows' tap targets collapsed into one shared identity).
- *Conclusive evidence, not a guess:* pulled the app's persisted `completion` SharedPreferences file directly off the test device mid-debugging. It showed the toggle/save logic was correct throughout - the widget's own on-screen checkmarks were displaying state that didn't match the real saved data. That isolates the remaining failure to Glance's list-rendering reliability on this test device/launcher (Nothing OS), not to any app logic.
- *PM response:* given that evidence, chose to make the widget's checkbox read-only rather than continue debugging a platform rendering issue with uncertain odds of resolution.
- **Decision:** the widget's checkbox is now view-only (matches weight/reps, which were already read-only per the original prototype-mode design). Tapping it - or any other non-editable part of a row - opens the app instead of toggling in place, consistent with the rest of the "read-only, tap opens the app" pattern. `ToggleExerciseAction` (the removed tap-to-toggle handler) was deleted rather than left dead in the tree.
- **Also decided, same debugging session:** resizing is temporarily disabled (`SizeMode.Single`, one fixed size) as the reliability baseline established once the checkbox stopped being interactive. Re-enabling resizing (`SizeMode.Exact`, tested working for read-only content) is a follow-up, not blocked on anything specific - just hasn't been re-verified since this fix.

**Additional finding, not requiring a PM decision:** the phone app → widget refresh (`onDataChanged` → `updateAll`) is eventually consistent, not always instant. If the widget was recently interacted with (its Glance "session" is warm) it updates promptly; if the widget has been idle and an edit is made purely in the phone app, the widget can take a few seconds to catch up (confirmed on retest - not stuck, just delayed) rather than updating the moment the edit happens. Acceptable for v1 given the widget's read-only nature; no action taken.

---

## Addendum 14. Widget checkbox restored — Addendum 13's diagnosis was wrong

**Status:** Added after the widget had been in real use for a few days. PM reported two symptoms: the widget was stuck showing Tuesday, and the checkboxes did nothing. Both turned out to be the same root cause, and that cause also invalidates the conclusion recorded in Addendum 13.

**The bug:** `DailyLiftWidget` read its data *above* `provideContent`, not inside it. Glance calls `provideGlance` once when a widget session starts and then keeps that composition alive; later `update()` calls recompose the existing composition rather than re-running the function. A value read outside the composition is therefore captured once, at session start, and replayed on every subsequent redraw — indefinitely.

That single placement produced both reported symptoms:

- *The frozen weekday:* the widget kept redrawing whichever day its session happened to begin on.
- *The dead checkboxes:* taps saved correctly, then the widget redrew its stale snapshot over the result.

**Why Addendum 13 reached the wrong conclusion.** The decisive evidence there was: persisted completion data is correct, the widget's display is not — read as proof that the failure was in Glance's rendering rather than the app's logic. That evidence was real and correctly gathered. But it fits two explanations, not one: the renderer being unreliable, *or* the renderer being handed stale data. The second was never ruled out, and it was the true one. The scope cut followed from an inference with an untested alternative, not from the evidence itself.

Worth stating plainly because the failure mode is generic: **"saves correctly, displays wrong" is a statement about what the renderer received, not about the renderer.** The instinct to trust persisted data as ground truth was right; treating the display as the only remaining suspect was the error.

**Two further bugs found in the same pass, each mimicking the same symptom:**

- Reading data *asynchronously* inside the composition (`produceState`) also fails, differently: after a tap, Glance tears the session down within a few hundred milliseconds, and the read does not reliably finish first. The composition is cancelled before it can emit. The read must be synchronous — Glance composes off the main thread, so this is safe.
- Caching that read against a revision key (`remember(revision)`) fails a third way: when the revision write hasn't propagated by the time the composition runs, the cache returns *pre-toggle* data. This is what produced "I can check the box but I can't uncheck it" — the un-check saved, the widget redrew as still-checked, and the natural response was to tap again, which re-checked it. Confirmed from device logs showing `true -> false` immediately followed by `false -> true` on the same exercise.

**Also fixed, same session:**

- Widget refreshes are now serialised and conflated through a single consumer. Two overlapping Glance sessions cancel each other and the losing one silently drops its redraw, so a refresh fired per-edit into a background scope raced itself. This is the real explanation for the "eventually consistent, warm session" behaviour recorded as an accepted quirk at the end of Addendum 13 — it was a self-inflicted race, not a property of Glance.
- The app now re-reads from disk on resume. It held completion in memory from construction, so widget taps were invisible to it, and its next edit would write the stale copy back over them.
- `SizeMode.Exact` restored. Under `SizeMode.Single`, `LocalSize` always reports the provider's declared minimum, so the row-count logic capped *every* widget at 2 rows regardless of actual size — a second, unnoticed bug that the fixed-size decision had been masking.
- The checkbox tap target moved off the checkmark glyph, which rendered as an empty string when unchecked and so collapsed to zero size, taking no taps at all.

**Decision:** the widget's checkbox is tappable-to-toggle again, as Decision 7 originally specified. Addendum 13's *decision* was sound given what was believed at the time; only its premise was wrong. Both stay in this log — the reversal and its correction — because the sequence is the useful part.

**Verified on-device** (Nothing Phone 2a): correct weekday, checkboxes toggling in both directions across repeated taps, and app and widget agreeing in both directions. **Not yet verified:** the midnight weekday rollover, which cannot be observed without either waiting for it or changing the device clock. The periodic ~30-minute update bounds the worst case at roughly half an hour of staleness past midnight.

---
