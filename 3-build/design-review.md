# Daily Lift — Design Review (Steps D.1 and D.2)

**Last updated by:** Claude Sonnet 4.6 (`claude-sonnet-4-6`)
**Tokens for this revision (measured):** ~368,430 (Step D.1 designer review
pass — reads all source files + screenshots, writes observations table and
summary; transcript captures 3 deduped API calls before this turn's output
tokens are flushed; the prior document-creation figure was ~51,208,736, see
git history or BUILD-DIARY.md for that entry)

## Purpose

This document records the designer's feedback from Step D.1 and the PM's
implementation decisions from Step D.2. It is the decision record for
D.1/D.2 design changes — equivalent to `DECISION-LOG.md`'s role for Stage 2
build decisions, but scoped to this design review pass.

---

## Designer Agent Brief (Step D.1 prompt)

*This section is the prompt for the designer agent. Read everything below
before beginning the review. Do not modify this section — add your findings
under "Designer Observations" further down.*

### What you are reviewing

You are a UX/product designer reviewing the **Step D editing UI** of the
Daily Lift Android app — a simple home-workout app for beginners (target
user: women working out at home, new to exercise, possibly anxious about
doing things wrong). The tone goal throughout is calm, supportive, and
never intimidating.

Your job is to produce specific, actionable design feedback. You are **not**
re-evaluating the product scope or data model — those are locked. See
"Do not revisit" below.

### Files to read

Read all of these before forming any opinions:

- `workout-widget-prototype.html` — the original prototype; the visual
  reference and source of truth for layout/tone where the build plan doesn't
  override it
- `app/src/main/java/com/dailylift/app/today/TodayScreen.kt` — the full
  current UI implementation
- `app/src/main/java/com/dailylift/app/today/TodayUiState.kt` — state,
  display logic (`weightDisplay`, `weightAccessibilityLabel`)
- `app/src/main/java/com/dailylift/app/ui/theme/Color.kt` — the color palette
- `DECISION-LOG.md` — every locked design/product decision from Stage 2
- `build-plan-hardened.md` — v1 scope and accessibility requirements

### Screenshots to examine

Two screenshots are in the project root. Examine both carefully:

- **`design-review-screenshot-normal.png`** — the app at default (100%)
  system font scale; shows the full editing UI including the Bodyweight
  toggle and exercise rows
- **`design-review-screenshot-130pct.png`** — the workout day screen at 130%
  system font scale; shows how the `FlowRow` layout wraps (or fails to wrap)
- **`design-review-screenshot-130pct-restday.png`** — the rest day screen at
  130% system font scale; shows how the rest day layout handles large text

### Priority issues flagged by the PM

The following were specifically called out by the PM after manual testing —
address these directly in your observations:

1. **Bodyweight toggle** — described as "weird and hard to follow." Examine
   the toggle's visual design, its position relative to the weight field, its
   label, and whether its purpose is discoverable without explanation.
2. **130% font-scale layout** — described as "the layout breaks." Examine
   the screenshot and the `FlowRow` implementation to identify specifically
   what breaks and what a correct wrap should look like.

### Do not revisit (already decided — locked in DECISION-LOG.md)

Do not suggest changes to any of the following — they were deliberate PM
decisions and are not open for reconsideration in this review:

- Removing the widget from v1 (Decision 1)
- The static "Do 3 sets of each exercise below" header — not per-row, not
  editable (Decision 2)
- Stable UUID-based exercise IDs (Decision 3)
- Static start/end images per exercise — not GIFs/video (Decision 4)
- The delete confirm dialog (Decision 5, edge case #1)
- The 30-character rename cap (Decision 5, edge case #3)
- The ≥4.5:1 contrast floor and ≥48dp tap targets (Decision 6)
- No onboarding screen in v1 (Decision 8)

### Output format

Write your findings under **"Designer Observations"** below. Use the table
provided. For each observation:
- Be specific: name the component, describe exactly what is wrong, and give
  a concrete suggested direction (not just "improve it")
- Flag the two PM-priority issues first (rows 1 and 2)
- Keep suggestions within v1 scope — no new features, no scope creep
- Note if a suggestion conflicts with a locked decision so the PM can make
  the call

After completing the table, add a **"Summary"** paragraph (3-5 sentences)
covering the most important patterns you saw across the whole UI — the
things the PM should weigh most when setting D.2 priorities.

---

## Step D.1 — Designer Observations

*Designer agent: fill in this table, then add a Summary paragraph below it.*

| # | Area | Observation | Suggested direction |
|---|---|---|---|
| 1 | Bodyweight toggle — discoverability and placement | **PM-priority issue.** The toggle has no label and sits between the weight field and the reps field, a position the user has no reason to associate with "this controls whether weight is bodyweight." At 100% scale the toggle reads visually as part of the reps area, not the weight area. When the toggle is OFF the weight field shows "—" and the user has no hint that the toggle is how they'd enter "bodyweight" instead of a number. The toggle is also an unfamiliar affordance for a beginner — a Material Switch conveys "on/off for a setting," but nothing here tells the user *which* setting. In the screenshots, exercises already set to Bodyweight (Push-ups, Leg raises) show "Body" text + a blue toggle, but the two states look like separate unrelated UI chunks rather than a coherent control pair. | Add a concise label directly above or immediately left of the toggle — "Bodyweight" (or just "BW" with a tooltip) — so its purpose is stated, not implied. Alternatively, replace the toggle with a small labeled chip/button ("Use bodyweight") that appears below the weight field only when the field is empty or tapped, making it a secondary action rather than an always-visible switch. Either way, the toggle and weight field must be visually grouped — a subtle background container or tighter horizontal gap — so users understand they control the same thing. No new feature scope: the toggle's existence is locked (Decision 5); only its label and visual grouping are in play. |
| 2 | 130% font-scale — FlowRow wrap is incoherent | **PM-priority issue.** At 130%, the FlowRow wraps but produces a layout that looks broken rather than intentional. The "Dumbbell deadlift" row shows the bodyweight toggle jumping to the far left of the second line (where the checkbox normally sits), the reps value floating mid-row with no column alignment, and the trash icon pushed off-screen or to a different line than the weight field. The column headers (EXERCISE / WEIGHT / REPS) remain fixed at the top but no longer align with any row data at 130% — the FlowRow's free-wrap destroys the grid relationship. The spec (Decision 6) calls for "name on top; weight, toggle, reps, action icons below" — what the screenshot shows is the components wrapping in FlowRow's natural order (checkbox → name → pencil → weight → toggle → reps → trash), which puts the toggle and trash on unpredictable lines depending on text length. The reps text is also truncated ("8-10", "6-10 e") because it still competes with the other controls on the second line. | Enforce the two-line structure explicitly at 130%+ rather than relying on FlowRow's natural wrap order. A practical approach: at 130% font scale (detectable via `LocalDensity` + `LocalConfiguration`), switch the row to a fixed two-`Row` Column layout: Row 1 = checkbox + exercise name (full width minus checkbox) + pencil icon; Row 2 = a fixed inner Row of [weight field OR "Body" text] + toggle + reps field + trash icon, left-aligned under the exercise name. This guarantees the two-line intent from Decision 6 is realized as a legible layout, not a scrambled FlowRow. Also hide or remove the column headers (EXERCISE / WEIGHT / REPS) at 130%+ since they no longer align with data and just add noise. |
| 3 | Reps column — truncation at 100% scale | Even at default font scale (100%), the reps value is already truncated in the screenshot — "as many a" for "as many as you can" (Push-ups), "8-12 r" for "8-12 reps", "12-15 r" and "10-15 r" visible. The REPS column is 72dp wide (`REPS_COLUMN_WIDTH_DP = 72`) but the full strings, rendered at 13sp, exceed that. This means users editing reps can type a value that immediately truncates in the display, which feels like a bug. | Widen the REPS column from 72dp to 88–96dp, or allow the reps EditableField to expand using `weight(1f)` rather than a fixed width (since reps is the rightmost data column and the trash icon follows on a separate FlowRow item). Alternatively, drop the "reps" unit suffix from the display string (show "8-12" not "8-12 reps") so the number fits the column — but check with PM first since this removes the word "reps" from the row entirely. |
| 4 | Reps value position relative to column header | At 100% scale, the reps value renders on line 2 of the FlowRow (below the weight field) while the "REPS" column header is on a fixed `Row` at the top. The result is that "REPS" aligns over the weight column area, not over the actual reps values. This misalignment happens because the FlowRow is already wrapping at 100% — the checkbox + name + pencil + weight + toggle collectively exceed one line width, so reps spills to a second line. The column headers give a false impression of a grid that doesn't exist. | If the column-header approach is retained, either (a) remove the REPS header (since reps values don't align to it), or (b) redesign the row so all controls fit on one line at 100% — e.g., by dropping the pencil emoji from the inline flow and making the exercise name itself the rename tap target, saving ~48dp per row. If the FlowRow two-line wrap is kept as the baseline layout (not just at 130%), align the headers to the actual second-line positions. |
| 5 | Pencil icon uses emoji, visually heavy | The rename affordance uses the ✏️ emoji rendered at `fontSize = 14.sp`. The emoji version is colorful and visually heavier than the monochrome outlined-style trash icon (🗑️). At small sizes emojis also render inconsistently across Android versions and OEMs (they are rasterized bitmap glyphs, not vector icons, so they can appear blurry or over-saturated on some devices). The prototype used an SVG pencil path at 13px — the implementation diverged from this by using an emoji. | Replace ✏️ and 🗑️ with vector icons (Material Icons `Edit` / `Delete` or equivalent SVG paths, as the prototype used). This gives consistent rendering, allows tinting with `AppTextFaint` / `AppAccent` color (matching the prototype's hover behavior), and removes the visual weight inconsistency between the emoji and the rest of the dark-card UI. The icons should remain small (18–20dp visual, 48dp tap target via the existing TapTarget wrapper). |
| 6 | No visual feedback when weight field is active/focused | The EditableField for weight uses a static background (`Color.White.copy(alpha = 0.06f)`) with no focused-state border or highlight. The prototype showed a blue border (`border-color: var(--accent)`) and slightly lighter background on focus. Without this, tapping the weight field gives no immediate indication that the field is now active — particularly problematic for beginner users who may not be confident they tapped correctly. | Add a focused-state border using `BasicTextField`'s `interactionSource` (or a `BorderStroke` that changes on focus) so the field gets a 1dp `AppAccent`-colored border when active. This matches the prototype's intent and Material's own guidelines for text field focus states. Low implementation cost — can be layered into the existing `EditableField` composable with an `InteractionSource`. |
| 7 | Rest day layout at 130% — no issues | The rest-day screen at 130% (`design-review-screenshot-130pct-restday.png`) renders cleanly. The centered Column layout wraps text naturally, the "Next up" card box expands appropriately, and no content is clipped or misaligned. The "Goblet squat, Dumbbell RDL, Reverse lunges + more" preview wraps to a second line inside the box and remains fully legible. | No change needed here. The rest-day layout is a good model for how the app handles large text when unconstrained by a fixed-column grid — the simplicity of the centered Column is its strength. |
| 8 | "Do 3 sets of each exercise below" — line weight and tone | The static header line is the right decision (Decision 2 confirmed). However, at 11.5sp with `AppTextMuted` color it has very low visual weight — it reads almost like small print rather than a helpful instruction. The target user (Maya, a beginner who may be uncertain about sets) is the person most likely to need this line, and it's likely to be skimmed over. | Slightly increase the line's visual prominence: either bump to 12sp and `AppTextPrimary.copy(alpha = 0.7f)`, or add a subtle left border accent (2dp, `AppAccent.copy(alpha = 0.5f)`) to differentiate it from column headers. Do not make it bold — that would tip the tone toward commanding. The goal is "friendly reminder" not "fine print." |
| 9 | Today badge color — accessibility | The "Today" badge uses `Color(0xFFB9C7FF)` on a background of `AppAccent.copy(alpha = 0.18f)` composited over `AppCard` (`#1A1A2E`). The effective background is approximately `#24264A`. B9C7FF on #24264A is approximately 3.8:1 contrast — below the 4.5:1 floor set in Decision 6. This is a small element but it does convey information (which day is today). | Darken the badge background to `AppAccent.copy(alpha = 0.35f)` or lighten the text to pure white (`AppTextPrimary`) to push contrast above 4.5:1. The badge's shape and size are fine; only the color values need adjusting. Confirm with `ContrastTest` before shipping. |

**Summary:**

The two most pressing issues are the Bodyweight toggle's lack of labeling and the 130%-scale FlowRow breakdown — both stem from the same root cause: the FlowRow is being used as a single flat sequence of controls, with no structural grouping that signals which controls belong together or which layout zone they occupy. At 100% scale the toggle is already confusing because it floats between the weight and reps fields with nothing to identify it; at 130% the FlowRow wraps randomly and the controls scatter. The fix for both issues is the same underlying change: group the weight field and toggle as a named unit (visually and semantically), and enforce the intended two-line layout explicitly at 130%+ rather than trusting FlowRow's natural wrap order. Beyond these two priority items, the reps column width and the emoji icon rendering are quick wins that will meaningfully improve fit-and-finish without touching any locked decisions — replacing the emoji icons with vector icons in particular will make the UI look more intentional and professional on real devices. The rest-day screen, the column header contrast, and the static "3 sets" line are smaller polish items; of these, the "Today" badge contrast issue is the only one that touches an accessibility requirement from Decision 6 and should be confirmed with a contrast test before D.2 closes.

---

## Step D.1 — Design Prototype

Before providing PM direction, a visual prototype of all designer recommendations
was created for review: **`design-review-prototype.html`** (project root).

Open it in any browser. It shows:
- A full proposed screen with every change applied
- Before/after comparisons for each of the 9 observations
- Both Option A and Option B for the Bodyweight toggle (the one open choice)
- A summary table of which items need a PM decision vs. are automatic

*Note: images vs. HTML was considered — HTML was chosen because it supports
interactive states (toggle on/off, focus, font-scale layout) that static images
cannot show, and can be built directly without a separate image-generation tool.*

---

## Step D.1 — PM Direction

*PM: after reading the Designer Observations above, add your direction for
each item. Confirm which suggestions to act on, set priorities, and redirect
anything that doesn't fit the tone/scope. This section is your call — the
designer agent does not write here.*

1. **Bodyweight toggle — remove it for the MVP.** Drop the toggle entirely
   rather than fixing its labeling/grouping. The weight field becomes a
   single free-text input; the user types "bodyweight" directly (already
   handled case-insensitively by `weightDisplay`/`weightAccessibilityLabel`,
   no data-model change needed). `DECISION-LOG.md` Decision 5/edge case #2
   has been updated to reflect this directly — the toggle requirement was
   removed, not superseded by an addendum.

2. **130% font-scale layout — fix it.** Approved as proposed: replace the
   FlowRow's natural wrap with the fixed two-line Row/Column structure
   (line 1 = checkbox + name + rename icon; line 2 = weight/reps/delete) at
   130%+. Bug-level issue, not a polish call.

3. **Reps column truncation — widen the column.** Approved as proposed:
   widen the REPS column from 72dp to the prototype's 92dp. Not taking the
   alternative (dropping the word "reps" from the display) — keep the label.

4. **Column header alignment at 100% — no separate decision.** Resolved as
   a side effect of #2's fixed two-line layout; the prototype's own summary
   table confirms this. Nothing to implement beyond #2.

5. **Pencil/trash icons — switch to vector icons.** Approved as proposed:
   replace the ✏️/🗑️ emoji with Material vector icons (Edit/Delete),
   tinted to match the existing dark-card UI, same 48dp tap target.

6. **Weight field focus state — add it.** Approved as proposed: 1dp
   `AppAccent`-colored border on focus via `BasicTextField`'s
   `interactionSource`.

7. **Rest day layout at 130% — no change needed.** Designer found no
   issues; nothing to implement.

8. **"Do 3 sets" line prominence — bump it slightly.** Approved as
   proposed: increase to 12sp / `AppTextPrimary.copy(alpha = 0.7f)` (not
   bold — stays a friendly reminder, not a command).

9. **Today badge contrast — fix it.** Approved as proposed: darken the
   badge background (or lighten the text to pure white) to clear the 4.5:1
   floor from Decision 6. This one isn't optional polish — it's a locked
   accessibility requirement. Confirm with `ContrastTest` before D.2 closes.

All 9 observations are now resolved. Proceeding to Step D.2.

---

## Step D.2 — Implementation Decisions

| Issue | Decision | Rationale |
|---|---|---|
| 1. Bodyweight toggle | Removed entirely. Weight field is now a single free-text `EditableField` (keyboard type changed from `Decimal` to `Text` so "bodyweight" can be typed); `toggleBodyweight()` removed from `TodayViewModel`, `isBodyweight` removed from `ExerciseRow`. | PM direction: MVP doesn't need a dedicated control for this — free text is enough. |
| 2. 130% layout fix | Replaced `FlowRow`'s natural wrap with an explicit two-line `Column` per row: line 1 = checkbox + name + rename icon; line 2 = weight + reps + delete icon. **Revised after PM visual testing** (see below) to mirror `ColumnHeaders`' exact column widths/arrangement instead of just indenting under the name. | A deterministic structure, not a scale-conditional one — simpler than branching on font scale, and removing the toggle freed up enough space that line 2 no longer needs the toggle slot. |
| 3. Reps column width | `REPS_COLUMN_WIDTH_DP` 72 -> 92. | Matches the prototype's approved value; keeps the word "reps" rather than dropping it. |
| 4. Header alignment at 100% | Originally assumed resolved as a side effect of #2 - **PM visual testing showed this was wrong** (see below); fixed by making the row and header share an identical column structure. | The prototype's own PM-decision table predicted this would resolve automatically; in the real build it didn't, because the row's indent-under-name approach didn't actually reproduce the header's flex/fixed-width column math. |
| 5. Vector icons | Replaced ✏️/🗑️ emoji with `Icons.Default.Edit` / `Icons.Default.Delete` (`androidx.compose.material:material-icons-core` added as a new dependency), tinted `AppTextFaint`, same 48dp `TapTarget` wrapper. | Consistent rendering across devices; matches the dark-card UI's existing icon treatment. |
| 6. Weight field focus state | Added a 1dp `AppAccent` border via `MutableInteractionSource`/`collectIsFocusedAsState`, shown only while focused. | Matches the prototype; cheap to add via the existing `EditableField` composable. |
| 7. Rest day at 130% | No change. | Designer found no issues. |
| 8. "Do 3 sets" line prominence | `11.5sp`/`AppTextMuted` -> `12sp`/`AppTextPrimary.copy(alpha = 0.7f)`, not bold. | Friendly reminder, not fine print — matches PM direction to avoid a commanding tone. |
| 9. Today badge contrast | Background alpha `0.18f` -> `0.35f`; text color `Color(0xFFB9C7FF)` -> `AppTextPrimary`. New `ContrastTest.todayBadgeTextMeetsContrastFloorAgainstBadgeBackground` confirms >=4.5:1 (passed on the first try at this alpha). | Locked accessibility floor from Decision 6 — not optional polish. |

**Automated tests after all changes:** `./gradlew test connectedDebugAndroidTest lintDebug` — 38/38 unit tests, 9/9 instrumented tests, lint clean. Test rewrites: D2 (`TodayViewModelTest`) now covers free-text "bodyweight" entry instead of the toggle; D9/D10 (`TodayScreenEditingTest`) no longer assert on the removed toggle.

**Manual test (PM):** debug APK installed and launched on `Pixel_10(AVD) - 17`. Visual approval needed: toggle removal feels right, 130% font-scale layout wraps cleanly (Settings > Display > Font size), vector icons/focus border/badge contrast look correct, and overall feel is right.

### PM visual testing pass — 3 bugs found, all fixed

| # | Bug | Fix |
|---|---|---|
| 1 | No scroll — a long exercise list (or "Add exercise") got clipped off the bottom of the screen with no way to reach it. | `TodayScreen` now pins `HeaderRow` and wraps the workout/rest content below it in a `Column.weight(1f).verticalScroll(rememberScrollState())`. New regression test: `TodayScreenEditingTest.longExerciseListScrollsToRevealAddExerciseButton` (adds 15 exercises, asserts "+ Add exercise" is reachable via `performScrollTo()`). |
| 2 | Weight/reps fields didn't line up with the WEIGHT/REPS column headers (the #2/#4 fix above was wrong in practice). | `ColumnHeaders` and `ExerciseRowView`'s line 2 now share an identical `Row` structure: same 8dp horizontal padding, same `Arrangement.spacedBy(4dp)`, same leading 48dp spacer, same flex spacer in place of the EXERCISE column, same fixed widths for weight/reps, same trailing 48dp spacer where the delete icon sits. Alignment is now structural (guaranteed by matching layout math), not coincidental. |
| 3 | New exercises defaulted to "10-12 reps" instead of "10 reps". | `Exercise.new()`'s default `reps` changed to `"10 reps"`. New assertion added to `TodayViewModelTest.addExerciseAppendsNewPlaceholderExerciseWithUniqueIdAndDefaults`. |

Full suite re-run after these fixes: 38/38 unit tests, 10/10 instrumented tests (one new), lint clean.
