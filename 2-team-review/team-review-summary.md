# Daily Lift — Stage 2 Team Review (Pass 1 Findings)

This document captures the team's initial review of the prototype
(`workout-widget-prototype.html`) and the draft build plan
(`stage3-build-prompt-DRAFT.md`), before the conflicts they surfaced were
resolved into PM decisions. For how these findings were resolved, see
`DECISION-LOG.md`. For the resulting plan, see `build-plan-hardened.md`.

---

## Key Takeaways

- **Scope mismatch between the draft plan and the project framing:** the
  draft says "DO NOT build the widget yet," but the project is pitched as
  "a home-workout app *with* a home screen widget" and the prototype's whole
  "Widget view" toggle exists to validate that experience. Flagged by the
  **Senior Staff Engineer agent** and **Android Platform Specialist agent**;
  resolved as Decision 1 (phone app = v1, widget = v2).
- **The single biggest beginner-clarity gap is missing "sets" information** —
  "8-12 reps" doesn't tell a beginner how many rounds to do. Identified by
  **Maya agent**; resolved via a static "3 sets" header line (Decision 2).
- **Completion tracking by array index is fragile** — the prototype's
  `deleteExercise()` has to manually re-shift indices to keep checkmarks
  aligned, and any future widget needs a stable per-row identifier. Flagged
  by the **Senior Staff Engineer agent** and **QA Engineer agent**; resolved
  by adding stable exercise IDs (Decision 3).
- **The form-tip popup's image placeholder reads as "broken," not
  "unfinished by design"** — literal text like "Exercise demo image would
  appear here" would ship to real users. Flagged by the **Product Designer
  agent**; this became the seed for the larger visuals decision (Decision 4:
  mandatory start/end images per exercise).
- **The prototype fails its own accessibility floor** — `--faint` text
  contrast computes to ~2.9:1 (WCAG AA requires 4.5:1), and every tap target
  (checkbox, nav arrows, pencil, trash) is well under the 48dp minimum.
  Flagged by the **Accessibility Specialist agent**; resolved as Decision 6.
- **"Bodyweight" is an undocumented magic string** — the prototype matches
  `weight === 'bodyweight'` exactly, but there's no UI to set it; typing
  "Bodyweight" or "BW" displays as broken text ("Bodyweight lb"). Flagged by
  the **QA Engineer agent**; resolved via a dedicated toggle (Decision 5).
- **Jargon appears with zero explanation** — "AMRAP" sits directly in the
  reps column with no tap needed and no definition anywhere. Flagged by
  **Maya agent**; fixed via a seed-data content rewrite (Decision 2).
- **No reorder feature exists today** (confirmed by the **QA Engineer
  agent**) — lower risk than initially feared, but reinforces the case for
  ID-based completion (Decision 3) in case it's ever added.

---

## Token Cost of This Review (measured)

Pulled directly from this session's local transcript log (`message.usage`
per API call — `cache_creation_input_tokens` + `cache_read_input_tokens` +
`output_tokens`, deduplicated where one call emitted multiple log lines).
This covers every API call from your initial review brief through the point
where Pass 1 + Pass 2 + the Conflict 1 framing were delivered to you.

| Call | What happened | Tokens | Cost (est.) |
|---|---|---|---|
| 1 | Initial framing message processed; first tool call (listing project files) | 30,507 | $0.1856 |
| 2 | Reading `stage2-team-review-prompt.md` / setup | 30,961 | $0.0166 |
| 3 | Reading the prototype HTML, draft plan, and review brief in full (parallel reads) | 32,119 | $0.0223 |
| 4 | **Pass 1 (six role reviews) + start of Pass 2** — hit the 32,000-token output cap mid-response | 79,555 | $0.5848 |
| 5 | Continuation: rest of Pass 2 + Conflict 1 framing + creating `DECISION-LOG.md` | 82,808 | $0.2545 |
| 6 | Conflict 1 presentation text finalized | 83,650 | $0.0537 |
| **Total (measured)** | | **339,600** | **~$1.12** |

**Cost basis:** standard published Claude Sonnet 4.x API rates — input
$3/MTok, output $15/MTok, prompt-cache write (1-hour TTL, used throughout
this session) $6/MTok, prompt-cache read $0.30/MTok. This is an
**API-equivalent cost**: it's what this work would bill as on pay-per-token
API usage. If this session is running under a Claude Pro/Max subscription,
there's no per-token charge — the flat subscription fee applies regardless,
and this figure is for reference/comparison only. Verify current rates on
Anthropic's pricing page, as they can change.

**Why this is so much higher than a word-count estimate would suggest:** every
API call in a Claude Code session carries a fixed **~30,000-token baseline**
(system prompt + tool definitions) — visible as call 1 above, before any
review content existed. From then on, prompt caching means each subsequent
call **re-processes the entire accumulated context** (`cache_read_input_tokens`),
so the total compounds turn-over-turn as the conversation grows. Most of the
339,600 total is this re-processed cache-read volume, which is billed at a
steep discount (~10% of fresh-input rate) — so the dollar cost is much smaller
than the raw token count implies, but the *token count itself* is real and
measured, not estimated.

This total covers Pass 1/Pass 2 only (through presenting Conflict 1). The cost
of working through all 8 conflicts and producing `build-plan-hardened.md` and
`DECISION-LOG.md` is tracked separately, per-conflict, in `DECISION-LOG.md`.

---

## Full Team Reviews

### Senior Staff Engineer agent

- Completion is tracked by array index (`completed.done[i]`), not a stable
  ID. The prototype's own `deleteExercise()` has to manually re-shift every
  index above the deleted item to keep checkmarks aligned — fragile, and it
  means the widget and app must always agree on array order. Recommend: give
  every exercise a stable ID at creation; key completion off ID, not
  position.
- Storage: for 5 days x ~7 exercises (tiny, no relations, no queries), Room
  is overkill and its async/Flow model is awkward for a widget's ~10s
  BroadcastReceiver window. Recommend a single JSON file (workout data:
  name/weight/reps/tip/id) + SharedPreferences (small daily completion map).
  Both are synchronously readable from the widget process.
- The plan's Steps A-E never touch the widget at all, yet the prototype's
  whole "Widget view" toggle and the project framing ("home-workout app
  *with* a home screen widget") suggest it's core. Either the widget is a
  real Step F in v1, or v1 is "phone app + widget-ready data model" and the
  widget is a separate phase. The draft doesn't pick one.
- No tip-editing UI exists anywhere in the prototype — the popup is
  read-only. If a user repurposes/renames an exercise, its tip can go stale
  with no way to fix it.
- Agrees with deferring images/animations — the prototype itself only shows
  a placeholder box, so a no-image v1 is already the validated design.
- minSdk recommendation: API 26 (Android 8.0) — ~98% device coverage, no
  friction for Compose/Glance/RemoteViews. (No real tradeoff here, just
  stating the choice.)

### Android Platform Specialist agent

- The prototype's widget view has day-navigation arrows that let you flip
  through all 7 days. Every flip = full RemoteViews rebuild via PendingIntent
  + broadcast. Working against "glanceable." Recommend the v1 widget shows
  only today — day-browsing stays app-only.
- Tapping an exercise name opens an overlay popup with the form tip in the
  prototype. A widget cannot show an overlay — RemoteViews has no
  dialog/popup primitive. On the widget, tapping a name must either do
  nothing or deep-link into the app's detail view.
- Checkbox tap-to-toggle is realistic via per-row PendingIntents →
  BroadcastReceiver → rewrite completion → `notifyAppWidgetViewDataChanged`.
  Standard pattern — but needs a unique PendingIntent per (day, exercise-id)
  — this is exactly where stable IDs matter.
- Midnight rollover: `updatePeriodMillis` has a 30-min floor and isn't
  reliable for "exactly at midnight" (Doze delays it further). Two real
  options: (a) exact alarm via `AlarmManager.setExactAndAllowWhileIdle`,
  rescheduled daily, handling boot/timezone broadcasts — correct but real
  complexity, plus an Android 12+ exact-alarm permission wrinkle; (b) "lazy"
  rollover — any redraw compares stored date vs. today and recomputes, no
  special trigger.
- A 7-exercise day may not fit a small flat widget. Recommend capping
  visible rows (e.g., first 4-5) with a "+N more — open app" line rather than
  building a scrollable RemoteViewsService collection.
- Confirms Engineer's storage choice (JSON file / SharedPreferences) works
  fine for widget reads — same process, no IPC needed.

### QA Engineer agent

- Empty day: nothing stops a user from deleting every exercise on a workout
  day. Prototype then shows an empty list + "Add exercise" with no
  empty-state message.
- "Bodyweight" has no discoverable input path. Seed data hardcodes the exact
  lowercase string `"bodyweight"`; display logic does `=== 'bodyweight'`. A
  user typing "Bodyweight"/"BW" gets a broken-looking "Bodyweight lb". No
  toggle exists.
- Long names: prototype CSS truncates with ellipsis, but no max input length
  is defined anywhere — needed so widget RemoteViews don't break.
- Completion reset: confirmed via `loadCompleted()` — resets to `{}` when
  stored date != today. Also, viewing a non-today day always shows unchecked
  regardless of stored state. Worth confirming this is intentional
  ("completion = did I do this today," no history).
- Widget/app sync: any app edit needs to push an immediate widget refresh;
  any widget checkbox toggle needs to be picked up on app resume. Needs to be
  an explicit requirement, not assumed.
- Deleting the last exercise: prototype's reindex correctly collapses
  `completed.done` to `{}` — but it's exactly the fragile logic Engineer
  wants to eliminate via stable IDs.
- No reorder UI exists today (good, less to test) — but flag: if reorder is
  ever added without ID-based completion, checkmarks silently attach to the
  wrong exercise.
- Rest-day "Next up" is hardcoded to "Monday" — correct today (Sat AND Sun
  both roll to Monday), just flagging the assumption in case workout days
  ever become configurable.

### Product Designer agent

- Widget vs. app visual consistency is good — same palette/card shape,
  widget gets a subtle blue-tinted border. Keep it.
- The form-tip popup's image area shows literal placeholder text ("Exercise
  demo image would appear here"). Shipped as-is, users will think the app is
  broken/unfinished. For a no-image v1, recommend removing the image block
  entirely, not leaving an empty box.
- The "focus" tag (e.g. "Lower body") is tiny and low-contrast but genuinely
  useful planning context — want to bump its visual weight slightly.
- Checkboxes (20x20px, thin border) are visually subtle on dark bg — want
  them bigger/more satisfying, which happens to align with what Accessibility
  will ask for anyway.
- "—" for empty weight is clean, keep it.
- Open question for Maya agent: does the cool dark-blue palette feel
  "friendly" or more "serious gym app"?

### Accessibility Specialist agent

- `--faint: rgba(255,255,255,0.35)` on `--card: #1a1a2e` ≈ 2.9:1 contrast —
  fails WCAG AA (4.5:1). Used for column headers (EXERCISE/WEIGHT/REPS), the
  widget's "Tap to open in the app →" footer (the *primary* widget→app CTA!),
  and "Next up" label. Needs a higher-contrast color, not just an opacity
  tweak.
- `--muted: rgba(255,255,255,0.55)` ≈ 5.6:1 — passes AA, fine as-is.
- Tap targets: checkbox 20x20px, nav arrows 30x30px, pencil/trash ~18px
  effective. Material/WCAG guidance is ~48dp. For an audience that may be
  older or less dexterous (and possibly holding a dumbbell), this matters.
  Recommend padding touch targets to 48x48dp without growing the visual
  icons.
- Dynamic text scaling: fixed px in the prototype; Android `sp` honoring up
  to 200% system scale will make the dense grid (especially the widget's
  22/60/70px columns) clip or overlap. Need a defined v1 floor.
- Screen readers: checkbox/pencil/trash are icon-only with no labels — need
  contentDescriptions with state ("Mark Goblet squat done", "Rename Goblet
  squat"). "—" for empty weight needs a spoken label ("no weight set"), not
  "dash."
- Positive: "done" state uses strikethrough + filled green check + checkmark
  icon — not color-only, already correct.

### Maya — Beginner User Voice agent

- Biggest one: where are the SETS? "8-12 reps" — of what, how many rounds?
  Not anywhere in the data, widget, app, or popup. Feels like the most
  important missing piece for someone like me.
- "AMRAP" next to Push-ups — no idea what that means, and it's sitting right
  there in the reps column with no tap needed. Can it just say "as many as
  you can"?
- "Dumbbell RDL" — tapping it shows a tip that explains the movement, that's
  fine — I just almost didn't tap it because the name itself looked like
  something I should already know.
- I like the rest day screen — "Rest & recover... let your muscles rebuild"
  feels like permission, not failure.
- "Nice work — you finished today's workout! 🎉" — motivating.
- Push-up tip ("drop to your knees if needed, that's totally fine") —
  exactly the reassurance I need, more like that please.
- For exercises with empty weight, I don't know what to start with — but if
  the app saves what I type for next time (it does), that's good enough.
- Would love SOME first-time "you've got this, go slow" message — even just
  once.
