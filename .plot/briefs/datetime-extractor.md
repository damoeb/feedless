## Implementation brief — datetime-extractor

- **Plan (canonical):** `docs/plans/2026-09-17-datetime-extractor.md` on `feature/datetime-extractor`
- **Approved:** 2026-09-17, damoeb, in-session
- **Branch:** `feature/datetime-extractor` (base: `develop`)
- **Ends as:** one PR to `develop`, carrying plan and code; the body links the plan and mirrors its approval record
- **Review of the code:** CI gate `./gradlew lint test`, then the owner's PR review

### What to build

`DateClaimer` logs its input and one result, so there is nothing to evaluate a date model against. Rename it to `DateTimeExtractor`, add `extractCandidates` (every match, not the first per format), and have `FulltextPlugin` log one line per event with the candidate input strings and a confidence level against `startingAt`. The plan is canonical; this is orientation.

### Settled decisions — do not re-derive

- **Inside `FulltextPlugin`, not a new plugin or a pipeline step.** A new plugin needs a `FeedlessPlugins` enum value in `schema.graphqls` and regenerated TS clients. A fixed step runs on non-event repositories, and repositories without plugins never reach `processDocumentPlugins`.
- **Only when `startingAt != null`.** That is the event qualifier, and it provides the reference. So the step never fills a missing `startingAt`: observe only, never mutate the document.
- **`extractDates: Boolean? = null`, `null` means on.** Gson ignores Kotlin defaults, so a non-null `Boolean` would read `false` for every stored repository. Not added to `schema.graphqls`.
- **One summary line in the harvest log.** No JSON trace, no table: a table needs a Flyway migration before we know which signals matter, and verbose lines crowd out the harvest log, which keeps only its newest lines.
- **Confidence is a pure function** of candidates and `startingAt`, tested by table.

### Done when

- `DateTimeExtractorTest` keeps every existing case green, plus multi-candidate, duplicate and empty bodies. A naive `find` per format passes single-date tests but returns one candidate for a body with two dates; the multi-candidate case catches that.
- Confidence table test covers `high`, `medium` (both branches), `low` (both branches), `mismatch`, `none`.
- `FulltextPluginTest`: the line is logged when `startingAt` is set; nothing is logged for `startingAt == null` or `extractDates = false`; `extractDates` missing from JSON counts as on.
- `WebExtractService` behaviour is unchanged.
- `./gradlew :packages:server-core:test` and `:packages:domain:test` pass, and in the end `./gradlew lint test`.

### Bookkeeping

Open the PR with `plot-open-pr.sh`, then append `→ #<n>` to the slice line in the plan.

### Scope guard

Owns `server-core/.../feed/DateClaimer*` (renamed), `scrape/WebExtractService.kt` (call site only), `pipeline/plugins/FulltextPlugin.kt`, `domain/.../pipeline/plugins/PluginParams.kt` (`FulltextPluginParams`), and their tests. The owner's `FulltextPlugin` WIP is in `stash@{0}`; do not apply it. Report anything the plan did not anticipate rather than improvising outside this scope.
