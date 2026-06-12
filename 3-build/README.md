# 3 - Build

The real product: a native Android app (Kotlin + Jetpack Compose), built from the hardened plan that came out of the team review, through to an installable result.

**Status: in progress.** The plan is locked and pre-build prep is done; the native build is underway.

## What's here

- `test-plan.md` - the full v1 test plan, every case derived from the hardened build plan and the decision log.

The plan this build follows, `build-plan-hardened.md`, lives in `../2-team-review/` because it's the output of that review. It wins wherever it disagrees with the prototype.

## How this build uses AI

The whole point of this stage is to direct AI tools through the engineering rather than write the code by hand. A few specific things worth calling out:

- **Claude Code does the building.** The hardened plan is structured as a step-by-step prompt for Claude Code, which sets up the environment, writes the Kotlin/Compose code, and pauses for me to test between phases.
- **Claude writes and runs its own tests first.** The plan builds in an automated test layer: at each step Claude Code writes JUnit and Compose UI tests, runs them via Gradle, and fixes failures itself before handing anything back. That covers 34 of 46 test cases, so most verification never reaches my desk. The remaining 12 are human judgment or on-device checks.
- **AI-generated exercise illustrations.** The form images are AI-generated rather than stock photos, with one sample approved for style before the full set is produced.
- **Model choice is deliberate.** Sonnet 4.6 handles the bulk of the implementation; the plan flags the two spots (the foundational data model, and any stuck debugging) where switching to a more capable model is worth it.

For the running account of decisions, dead ends, and course-corrections across the whole project, see [`../docs/BUILD-LOG.md`](../docs/BUILD-LOG.md).
