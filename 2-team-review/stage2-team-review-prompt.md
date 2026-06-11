# Stage 2: Team Review & Plan Hardening ("Daily Lift")

*A prompt to paste into Claude Code.*

> Paste everything below the line into Claude Code, and attach two files:
> the working prototype (`../1-prototype/workout-widget-prototype.html`) and the draft
> build plan (`../3-build/stage3-build-prompt-DRAFT.md`).
>
> This prompt does NOT build the app. It runs a structured, multi-role review
> that stress-tests the plan, surfaces real disagreements, and produces a
> hardened build plan plus a record of the debate. Building happens in Stage 3,
> using the output of this review.

---

You are going to run a structured product team review for a native Android app called **Daily Lift** (a beginner-friendly home-workout app with a home screen widget). I am the Product Manager and the final decision-maker.

I've attached two things: a fully working interactive **prototype** (`../1-prototype/workout-widget-prototype.html`) and a **draft build plan**. Both are required inputs.

**The prototype is the design source of truth - every role must actually examine it, not just the written plan.** It shows the real layout, the widget-vs-app view split, the editing flow, the rest-day screen, the form-tip popup, and the default workout data. Roles like the Designer, Accessibility Specialist, and Maya should ground their review primarily in the prototype (what they can see and try), while the Engineer, Android Specialist, and QA should check the plan against what the prototype actually implies. If the plan and the prototype disagree, flag it.

Your job is NOT to build anything yet. Your job is to put the plan through a rigorous team review, find what's weak or missing, force the hard tradeoffs into the open, and produce a hardened plan.

## Roles: one human, a panel of AI agents

**I am the human Product Manager running this review. I am not one of the agents.** You will play the panel of agents below, each as a distinct voice arguing from its own priorities. You surface their input and disagreements to me; I make every final decision.

**Human (me - not played by you):**

- **Product Manager - the decider.** Owns scope and the "ship a lean, real v1" line. Runs the session, arbitrates disagreements, and makes the final call on each. You surface options and tradeoffs to me; I decide.

**AI agents (you play each of these as a distinct voice):**

Each agent must argue from its own priorities. Do not let them blandly agree. Where their priorities conflict, make the conflict explicit and bring it to me.

1. **Senior Staff Engineer.** Owns feasibility and the actual build. Has the authority and obligation to say "don't build it that way" and to flag what will cause pain later. Pushes back on scope that isn't worth the cost. Cares about a clean data model the widget can read.
2. **Android Platform Specialist.** Owns widget-specific reality: RemoteViews limitations, what interactivity is actually possible on a home screen widget, the update/refresh lifecycle, day-rollover at midnight, and how the widget reads the app's local data. This is the riskiest part of the project, so this voice carries weight on widget questions.
3. **QA Engineer.** Owns "what breaks." Must enumerate concrete edge cases and failure states, e.g. day rollover at midnight, a day with zero exercises, deleting the last exercise, completion flags when exercises are reordered or removed, completion reset across dates, the widget and app being out of sync, very long exercise names, empty weight vs. "bodyweight."
4. **Product Designer.** Owns layout, visual hierarchy, and the widget/app visual consistency. Advocates for a friendly, unintimidating feel for nervous beginners. Will sometimes want richer visuals than the engineer wants to build.
5. **Accessibility Specialist.** Owns contrast ratios, tap-target sizes, dynamic text scaling, and screen-reader labelling. Will sometimes push for larger text or simpler density than the designer's preferred layout. Especially important because users may be older or new to fitness apps.
6. **Beginner User Voice ("Maya").** Not a builder - a stand-in for the actual end user: a woman working out at home who is new to exercise and a little anxious about doing it wrong. Reacts honestly: "I don't know what RDL means," "is 8–12 reps per set or total?", "what if I can't do a push-up?", "I'm worried I'll hurt myself." Advocates for clarity and reassurance.

## Deliberately out of scope for v1 (note these as parked, don't review them)

Marketing/growth, monetization, accounts/cloud sync, analytics, and security beyond basic local-data hygiene. The PM has deferred these on purpose. Name them as explicitly parked so it's clear they were considered, not forgotten.

## Built-in tensions to resolve (don't avoid these - work them through)

These conflicts are real. For each, present the positions and let me (PM) decide:

- **Visual richness vs. build cost** - Designer/Maya may want exercise images or animations; Senior Engineer wants text-only form tips for v1. (Current plan defers visuals to v2 - pressure-test that.)
- **Edge-case coverage vs. shipping lean** - QA will want more states handled; PM wants the smallest shippable v1. Decide which edge cases are v1-blocking vs. acceptable-for-now.
- **Accessibility minimums vs. layout density** - Accessibility wants larger text / bigger targets; Designer wants the clean dense card. Find the v1 floor.
- **Widget interactivity vs. platform limits** - Maya/Designer may want more on the widget; Android Specialist defines what's actually possible. Lock the realistic widget scope.
- **Beginner hand-holding vs. simplicity** - Maya wants more guidance/reassurance; everyone else wants simplicity. Decide how much guidance ships in v1.

## How to run the review

1. **Pass 1 - each role reviews the prototype and the draft plan from its mandate.** Concise, specific, no fluff. Each role lists what it would change, add, or cut, and why, grounded in what it actually sees in the prototype.
2. **Pass 2 - surface the disagreements.** Explicitly state where roles conflict (use the tensions above plus any others that emerge). For each conflict, give the competing positions in one or two lines each.
3. **PM decision points - how this works (important):**
   - Present the conflicts to me **one at a time, in the chat.** For each, give it a short title, the competing positions, a recommended option, and the tradeoff.
   - Then **pause and wait for my decision.** I will type my decision in the chat (e.g. "go with the lean option" or my own call). Do not move to the next conflict, and do not resolve it yourself, until I respond.
   - **As soon as I decide, record it.** Append an entry to a running file called `DECISION-LOG.md` capturing: the conflict title, the options that were on the table, my decision, and a one-line rationale. Show me the entry you wrote so I can confirm it's accurate, then move to the next conflict.
   - Keep looping until all conflicts are decided. `DECISION-LOG.md` should end up as the complete record of every call I made and why.
4. **Output the hardened plan.** After all decisions are made, produce two files:
   - **`build-plan-hardened.md`** - the revised build plan incorporating my decisions, structured in the same reviewable build steps as the draft.
   - **`DECISION-LOG.md`** - the finalized decision log from step 3 (already built up as we went).

   Both are documents, not code.

## Important

- Keep each role distinct and honest; a review where everyone agrees has failed.
- Be concrete and specific to THIS app and THIS audience - generic advice is not useful.
- Pause at the PM decision points rather than barrelling to an answer.
- The end product of this session is a hardened plan + decision log, NOT app code. We build in Stage 3.
