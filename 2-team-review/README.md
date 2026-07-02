# 2 - Team Review

Before building, the prototype and draft plan are handed to a simulated cross-functional team to pressure-test the idea and harden the plan.

`stage2-team-review-prompt.md` is a prompt to paste into Claude Code. It runs a structured review where a panel of **AI agents** each argues from its own priorities, while **I (the human PM) direct the session and make every final call.**

**The human in the loop:**

- **Product Manager (me)** - I run the review, weigh the agents' input, resolve every disagreement, and own the final decisions. I am not one of the agents; I am the person directing them.

**The AI agents on the panel:**

- **Senior Staff Engineer** (feasibility, can say "don't build it that way")
- **Android Platform Specialist** (widget reality and limitations)
- **QA Engineer** (edge cases and what breaks)
- **Product Designer** (layout, hierarchy, friendly feel)
- **Accessibility Specialist** (contrast, tap targets, text scaling)
- **Beginner User Voice ("Maya")** (reacts as a nervous first-timer)

The review deliberately forces real disagreements between the agents into the open and routes each one to me as a decision. Every decision I make is recorded.

## What running this produces

- `team-review-summary.md` - the full review from each agent team member
- `DECISION-LOG.md` - the record of each disagreement, the options, the PM's call, and the rationale
- `build-plan-hardened.md` - the revised build plan that the build stage (`../3-build/`) will execute

This stage does not produce code. It produces a hardened plan and a record of the thinking behind it.
