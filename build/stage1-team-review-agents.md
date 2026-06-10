# Claude Code Prompt — Stage 1: Team Review & Plan Hardening ("Daily Lift")

> Paste everything below the line into Claude Code, and attach two files:
> the working prototype (`workout-widget-prototype.html`) and the current
> build plan (`claude-code-prompt-stage2-build.md`).
>
> This prompt does NOT build the app. It runs a structured, multi-role review
> that stress-tests the plan, surfaces real disagreements, and produces a
> hardened build plan plus a record of the debate. Building happens in Stage 2,
> using the output of this review.

---

You are going to run a structured product team review for a native Android app called **Daily Lift** (a beginner-friendly home-workout app with a home screen widget). I am the Product Manager and the final decision-maker.

I've attached two things: a fully working interactive **prototype** (the design source of truth) and a current **build plan**. Your job is NOT to build anything yet. Your job is to put the plan through a rigorous team review, find what's weak or missing, force the hard tradeoffs into the open, and produce a hardened plan.

## The team (play each as a distinct voice with its own mandate)

Each role must argue from its own priorities. Do not let the roles blandly agree. Where their priorities conflict, make the conflict explicit.

1. **Product Manager (me) — the decider.** Owns scope and the "ship a lean, real v1" line. Arbitrates disagreements and makes the final call on each. (You will surface options and tradeoffs to me; I decide.)
2. **Senior Staff Engineer.** Owns feasibility and the actual build. Has the authority and obligation to say "don't build it that way" and to flag what will cause pain later. Pushes back on scope that isn't worth the cost. Cares about a clean data model the widget can read.
3. **Android Platform Specialist.** Owns widget-specific reality: RemoteViews limitations, what interactivity is actually possible on a home screen widget, the update/refresh lifecycle, day-rollover at midnight, and how the widget reads the app's local data. This is the riskiest part of the project, so this voice carries weight on widget questions.
4. **QA Engineer.** Owns "what breaks." Must enumerate concrete edge cases and failure states, e.g. day rollover at midnight, a day with zero exercises, deleting the last exercise, completion flags when exercises are reordered or removed, completion reset across dates, the widget and app being out of sync, very long exercise names, empty weight vs. "bodyweight."
5. **Product Designer.** Owns layout, visual hierarchy, and the widget/app visual consistency. Advocates for a friendly, unintimidating feel for nervous beginners. Will sometimes want richer visuals than the engineer wants to build.
6. **Accessibility Specialist.** Owns contrast ratios, tap-target sizes, dynamic text scaling, and screen-reader labelling. Will sometimes push for larger text or simpler density than the designer's preferred layout. Especially important because users may be older or new to fitness apps.
7. **Beginner User Voice ("Maya").** Not a builder — a stand-in for the actual end user: a woman working out at home who is new to exercise and a little anxious about doing it wrong. Reacts honestly: "I don't know what RDL means," "is 8–12 reps per set or total?", "what if I can't do a push-up?", "I'm worried I'll hurt myself." Advocates for clarity and reassurance.

## Deliberately out of scope for v1 (note these as parked, don't review them)

Marketing/growth, monetization, accounts/cloud sync, analytics, and security beyond basic local-data hygiene. The PM has deferred these on purpose. Name them as explicitly parked so it's clear they were considered, not forgotten.

## Built-in tensions to resolve (don't avoid these — work them through)

These conflicts are real. For each, present the positions and let me (PM) decide:

- **Visual richness vs. build cost** — Designer/Maya may want exercise images or animations; Senior Engineer wants text-only form tips for v1. (Current plan defers visuals to v2 — pressure-test that.)
- **Edge-case coverage vs. shipping lean** — QA will want more states handled; PM wants the smallest shippable v1. Decide which edge cases are v1-blocking vs. acceptable-for-now.
- **Accessibility minimums vs. layout density** — Accessibility wants larger text / bigger targets; Designer wants the clean dense card. Find the v1 floor.
- **Widget interactivity vs. platform limits** — Maya/Designer may want more on the widget; Android Specialist defines what's actually possible. Lock the realistic widget scope.
- **Beginner hand-holding vs. simplicity** — Maya wants more guidance/reassurance; everyone else wants simplicity. Decide how much guidance ships in v1.

## How to run the review

1. **Pass 1 — each role reviews the attached plan from its mandate.** Concise, specific, no fluff. Each role lists what it would change, add, or cut, and why.
2. **Pass 2 — surface the disagreements.** Explicitly state where roles conflict (use the tensions above plus any others that emerge). For each conflict, give the competing positions in one or two lines each.
3. **PM decision points.** For every conflict, present it to me as a clear decision with a recommended option and the tradeoff, then pause for my call. Do not silently resolve conflicts yourself.
4. **Output a hardened plan.** After I've made the calls, produce: (a) a revised build plan incorporating the decisions, structured in the same reviewable build steps as the original, and (b) a short "decision log" recording each disagreement and how it was resolved.

## Important

- Keep each role distinct and honest; a review where everyone agrees has failed.
- Be concrete and specific to THIS app and THIS audience — generic advice is not useful.
- Pause at the PM decision points rather than barrelling to an answer.
- The end product of this session is a hardened plan + decision log, NOT app code. We build in Stage 2.
