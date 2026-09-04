# <title>

> <one-line summary>

## Status

- **Phase:** Draft
- **Type:** feature | bug | docs | infra
- **Story:** <!-- optional, story slug (docs/stories/<slug>/) — the durable intent this plan serves -->
- **Sprint:** <!-- optional, filled when plan is added to a sprint — a time-boxed selection of planned work -->
- **Issue:** <!-- optional, tracker issue(s) this plan answers (`#228`, or `#226, #228`) — the board removes an issue from its inbox once a plan names it here -->
- **Review:** <!-- pr | in-session | ballot — how is this plan reviewed & approved? -->
- **Impl:** <!-- own branches | same branch | other repo | none — where does implementation happen? -->
<!-- Transition records — written by the workflow commands, not by hand:
- **Approved:** <date>, <who>, <channel>
- **Started:** <date>, <who>, <branch>   (one line per started branch)
-->

## Changelog

<!-- Release note entry. Written during planning, refined during implementation. -->

- <user-facing change description>

## Motivation

<!-- Why does this matter? What problem does it solve? -->

## Design

### Approach

<!-- How will this be implemented? Key architectural decisions. -->

### Open Points

- [ ] ...

## Definition of Done

<!-- The repo gate. A branch is done when this passes. -->

- [ ] `./gradlew lint test` passes
- [ ] Changelog entry above is accurate

## Branches

<!-- Optional: define a tracer bullet (thin vertical slice) first. -->
<!-- See the tracer-bullets skill for guidance. -->
<!-- ### Tracer -->
<!-- - `feature/<slug>-tracer` — <thin slice description> -->
<!--   Layers: <layer> → <layer> → <layer> -->
<!--   Proves: <what this validates> -->
<!--   Status: Not started -->

<!-- When using ### Tracer, wrap remaining branches in ### Implementation: -->
<!-- ### Implementation -->

<!-- Waves: branches under one ### subheading may run concurrently. A wave is
     eligible once every non-deferred branch in every PRIOR wave is merged, so
     `### Tracer` proves the seam before `### Implementation` fans out. Add
     `### Wave 3`, `### Wave 4`, … for anything that must follow.
     No subheadings at all = one wave = every branch eligible at once (the
     pre-wave behaviour). Check state with /plot-fleet.
     Annotations (deferred:/claimed:/moved:) must sit on the SAME line as
     the backticked branch name — a wrapped continuation line is not read. -->

- `feature/<slug>` — <description>

## Notes

<!-- Session log, decisions, links -->
