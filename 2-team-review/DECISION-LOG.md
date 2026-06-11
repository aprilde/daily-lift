# Daily Lift — v1 Build Plan: Decision Log

**Status: FINALIZED.** All 8 conflicts from the Stage 2 team review resolved. This log is the complete record of every PM call and the rationale behind it. The resulting plan is `build-plan-hardened.md`.

**Token figures methodology:** the "Tokens" line under each conflict is **measured, not estimated** — pulled directly from this session's local transcript log (`~/.claude/projects/.../<session-id>.jsonl`), summing `cache_creation_input_tokens + cache_read_input_tokens + output_tokens` across the API call(s) that processed your decision for that conflict (deduplicated where one call emitted multiple log lines). Each figure represents the call(s) where your decision was recorded — which, in most cases, also included presenting the *next* conflict in the same response, so figures aren't perfectly isolated to a single conflict. The bulk of each number is `cache_read_input_tokens` — the entire accumulated conversation context being re-processed on that turn — which is billed at a steep discount (~10% of fresh-input rate); the raw token count is real, but the dollar cost is much smaller than the count alone implies.

**Cost figures methodology:** the "Cost (est.)" line under each conflict applies standard published Claude Sonnet 4.x API rates to that conflict's measured token breakdown — input $3/MTok, output $15/MTok, prompt-cache write (1-hour TTL, used throughout this session) $6/MTok, prompt-cache read $0.30/MTok. This is an **API-equivalent cost** (what this work would bill as on pay-per-token API usage); under a Claude Pro/Max subscription there's no per-token charge and this is for reference only. Verify current rates on Anthropic's pricing page, as they can change.

(See `team-review-summary.md` for the separate, also-measured total covering the Pass 1/Pass 2 review that produced these 8 conflicts — ~339,600 tokens / ~$1.12.)

Conflicts resolved:
1. Is the widget actually in v1?
2. Missing "sets" in the data model
3. Stable exercise IDs vs. index-based completion
4. Visual richness vs. build cost
5. Edge-case triage
6. Accessibility minimums vs. layout density
7. Widget interactivity — lock the v1 scope
8. Beginner hand-holding vs. simplicity

**Total measured tokens across all 8 conflicts (incl. writing both final deliverables):** ~2,019,267 / **~$1.90**

**Combined Stage 2 review total (Pass 1/Pass 2 + all 8 conflicts):** ~2,358,867 tokens / **~$3.01**

---

## 1. Is the home-screen widget actually in v1?

**Tokens (measured):** 171,736 — covers recording this decision and presenting Conflict 2

**Cost (est.):** ~$0.32

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

**Tokens (measured):** 274,877 — covers presenting the revised options after your reframe, recording this decision, and presenting Conflict 3

**Cost (est.):** ~$0.19

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

**Tokens (measured):** 195,321 — covers recording this decision and presenting Conflict 4 (Round 1)

**Cost (est.):** ~$0.13

**Options on the table:**
- A. Index-based, matches the prototype exactly. No `id` field; `deleteExercise` keeps index-reshifting logic. Cost shifts to v2 (data migration when widget needs stable IDs).
- B. Add a stable `id` field now (UUID at creation). Completion map keys off `id`. Simplifies v1's own delete logic; zero migration for v2.

**Recommended:** B.

**PM response:** Agreed with the recommendation — no pushback, no additional rationale offered beyond accepting B.

**Decision:** B — add stable `id` field now.

**Additional rationale:** Tiny implementation delta (one field, generated once) that actually simplifies v1's delete logic and avoids migrating real, customized user data when the widget arrives in v2.

---

## 4. Visual richness vs. build cost (image placeholder + per-exercise visuals)

**Tokens (measured):** 326,639 — covers presenting Round 2 (the static-vs-GIF sub-decision), recording the final decision, and presenting Conflict 5

**Cost (est.):** ~$0.33

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

**Tokens (measured):** 238,620 — covers recording this decision and presenting Conflict 6

**Cost (est.):** ~$0.23

**Already resolved/N/A (no decision needed):** widget/app sync (N/A v1, per Decision 1), deleting last exercise/index-reshifting (resolved by Decision 3), reordering exercises (not a v1 feature), completion resets daily + non-today days always show unchecked (confirmed intentional), rest-day "Next up" hardcoded to Monday (acceptable given fixed Mon-Fri schedule).

**Items needing a call:**

- **#1 Empty workout day** (all exercises deleted)
  - *Recommended:* an empty-state message ("No exercises yet — add one below") in place of the list, with "Add exercise" still visible.
  - *PM response:* Modified the recommendation — kept the underlying concept (empty-state message + retained "Add exercise" button) but changed the wording.
  - *PM's stated direction:* "message should read 'Rest Day'."
  - *Decision:* show **"Rest Day"** in place of the exercise list, with the "Add exercise" button still available below it so the user isn't stuck on a dead-end screen.

- **#2 "Bodyweight" weight input**
  - *Recommended:* Option (a) — flexible/case-insensitive string matching (cheap, one-line fix, but still not discoverable in the UI).
  - *PM response:* Pushed back — chose the alternative, a dedicated toggle (more discoverable, more UI cost), over the cheaper recommendation.
  - *PM's stated direction:* "B."
  - *Decision:* Option B — a dedicated **"Bodyweight" toggle** next to the weight field (exact placement/layout decided during Step D, informed by Conflict 6's density/accessibility resolution).

- **#3 Long exercise names**
  - *Recommended:* cap rename input at ~30 characters.
  - *PM response:* Agreed — no pushback.
  - *Decision:* cap rename input at **~30 characters**. Confirmed.

- **#9 Missing images for user-added custom exercises** (new edge case introduced by Decision 4)
  - *Recommended:* one generic placeholder start/end image pair as fallback for exercises without dedicated art.
  - *PM response:* Implicitly accepted the fallback recommendation as-is, and additionally answered an open question carried over from Decision 4 (how the ~70 default-exercise images get sourced) — not a pushback, but added new scope-clarifying direction.
  - *PM's stated direction:* "the designer agent can generate these images in the build stage."
  - *Decision:* one generic placeholder start/end image pair as fallback for custom exercises. The ~70 default-exercise images (Decision 4) will be **generated during the Stage 3 build** by the build-stage "Designer" role — generation method TBD at build time, not sourced externally by the PM.

- **#10 Stale tip/images for renamed or custom exercises**
  - *Recommended:* acceptable for v1, defer tip/image editing to v2.
  - *PM response:* Agreed — no pushback.
  - *Decision:* confirmed acceptable for v1 — no edit UI for tip or images; deferred to v2.

---

## 6. Accessibility minimums vs. layout density

**Tokens (measured):** 252,921 — shared with Conflict 7 below; covers recording both decisions and presenting Conflict 8

**Cost (est.):** ~$0.22 (shared with Conflict 7)

**Options on the table:**
- A. Accessibility floor — fix contrast (replace `--faint`-equivalent for all meaningful text, ≥4.5:1, free); expand tap targets to 48dp via padding (visual icons unchanged); define a 130% font-scale floor with row-wrap fallback (name on top, weight/reps/toggle/actions below) instead of clipping. 200% scale deferred to v2.
- B. Density-first — fix contrast only; leave tap targets and font-scale handling as-is (single-line dense row, as in the prototype); full compliance becomes a v2 polish pass.

**Recommended:** A.

**PM response:** Agreed with the recommendation — no pushback, no additional rationale offered beyond "I'm fine with your recommendation here."

**Decision:** A.

**Additional rationale:** Contrast and touch-target padding are nearly free and preserve the dense visual at default (100%) font scale — only users who've increased their text size see the row relax to two lines. Given the stated audience (older users, fitness-app newcomers, possibly one-handed with weights), shipping under Material's own tap-target minimum is the wrong place to cut for v1.

---

## 7. Widget interactivity — lock the v1 scope

**Tokens (measured):** included in Conflict 6's figure above (252,921) — both were resolved in the same response, since this conflict required no separate PM decision

**Cost (est.):** included in Conflict 6's ~$0.22

**Status:** Largely resolved by Decision 1 (widget moved to v2) — no options were presented and no PM vote was requested.

**PM response:** N/A — this conflict was not put to the PM as a decision point; it was presented as already resolved by Decision 1's ripple effect, with one carryover requirement noted below.

Most of this conflict's original substance — no day-navigation on the widget, tap-on-name deep-links instead of an overlay (overlays aren't possible in RemoteViews), capped row count with "+N more" overflow, per-row checkbox PendingIntents, and exact-alarm vs. "lazy" midnight rollover — becomes a v2 planning question, to be revisited once the widget is actually being built, informed by the data model already hardened in Decisions 3-5.

**One concrete v1 carryover (no vote needed — basic correctness, not a tradeoff):** the phone app must recompute "today" on resume/foreground (not just at first launch), and refresh completion state for the new date if the day rolled over while the app was backgrounded. This is the phone-app half of the Android Specialist's "lazy rollover" approach and applies with or without a widget. Folded into Steps C/D of the hardened plan.

---

## 8. Beginner hand-holding vs. simplicity

**Tokens (measured):** 559,153 — covers recording this final decision and writing both deliverables (`build-plan-hardened.md` and the finalized `DECISION-LOG.md`)

**Cost (est.):** ~$0.48

**Options on the table:**
- A. No onboarding screen — rely on per-exercise tips + the new start/end images (Decisions 2 and 4) to carry reassurance.
- B. One-time first-launch banner ("New to working out? Go at your own pace... You've got this.") — one boolean flag + one dismissible component.

**Recommended:** A.

**PM response:** Agreed with the recommendation — no pushback.

**Decision:** A — no onboarding in v1.

**PM's stated rationale:** "A works for the MVP, onboarding will need more discovery to accomplish correctly."

**Additional rationale:** The two specific anxieties Maya named (sets/reps confusion, unfamiliar exercises/posture) already have targeted fixes via Decisions 2 and 4. A generic welcome message is a tone-setting feature that deserves its own discovery work to get right, rather than being bolted on now. Revisit for v2 with proper discovery.

---
