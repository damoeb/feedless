# Log datetime candidates from event bodies with confidence against startingAt

> `DateClaimer` becomes `DateTimeExtractor`, and the fulltext plugin logs every datetime it finds in an event's body with a confidence level measured against the selector's `startingAt`, as signals for a later model.

## Status

- **State:** Approved
- **Type:** feature
- **Review:** in-session
- **Impl:** same branch
- **Approved:** 2026-09-17, damoeb, in-session
- **Started:** 2026-09-17, damoeb, `feature/datetime-extractor`

## Approval

- **Assignee:** damoeb

## Changelog

- The harvest log shows, for each event the fulltext plugin fetched, the datetime strings found in its body and how confidently they match the event's start date.

## Motivation

- **Event lists are shallow.** Selectors on a list page yield a start date, but rarely an end time or a series. The event's own page usually holds them, and the fulltext plugin already fetches that page.
- **No signals to learn from.** `DateClaimer` logs its input and its single result. Which formats matched, which substrings, and why one won are never recorded, so there is nothing to evaluate or train a model on. The flaky `DateClaimerTest` case in `docs/tasks.md` ("12 Dezember 24" → year ~282) is hard to diagnose for the same reason.
- **A free reference.** For events, `startingAt` comes from a dedicated, well-standardized selector. Comparing body candidates against it turns every harvested event into a labelled example.

Goals, in order: determine the start datetime, then the end time, then whether the event is a series. This plan covers only the first, while logging the candidate strings the later two will need.

## Design

### Approach

**Rename.** `DateClaimer` → `DateTimeExtractor`, `DateClaimerTest` → `DateTimeExtractorTest`, both in `server-core/.../feed/`. `claimDatesFromString` → `extractDateTime`; `WebExtractService` keeps calling it, and its behaviour does not change.

**Candidates.** A new `extractCandidates(text, locale): List<DateTimeCandidate>` finds every match in the text (`findAll` rather than the current first `find` per format). A `DateTimeCandidate` holds the matched input string, its range in the text, the format pattern, the parsed `LocalDateTime`, and whether it carried a time. Candidates resolving to the same instant are merged, keeping an occurrence count; a string matched by several formats keeps the most specific one, using the existing ordering (time first, then longest pattern).

**Hook.** `FulltextPlugin.mapEntity` runs the extraction after the fulltext/readability step when all of these hold:

- the fetch produced a mapped document
- `params.extractDates` is not `false`
- `document.startingAt != null`, which marks the document as an event

`FulltextPluginParams` gains `extractDates: Boolean? = null`, with `null` treated as `true`. It is nullable because Gson ignores Kotlin defaults, so a non-null `Boolean` would read as `false` for every stored repository. It is not added to `schema.graphqls`: nothing needs to turn it off yet.

The extraction reads the mapped document's `text`. The locale is the fetched page's `<html lang>`, falling back to `PropertyService.locale`, which mirrors `WebToFeedTransformer.extractLocale`. The step only observes and never changes the document.

**Confidence** against `startingAt`:

| Level | Rule |
|---|---|
| `high` | exactly one distinct candidate with a time, and it equals `startingAt` |
| `medium` | a candidate with a time equals `startingAt` among other distinct candidates, or the only candidate matches the date but has no time |
| `low` | a candidate matches only the date among other candidates, or matches the date with a different time |
| `mismatch` | candidates exist, none on the `startingAt` date |
| `none` | no candidates |

The rules live in one pure function so they can be tested by table and refined without touching extraction.

**Log line.** One line through the plugin's `LogCollector`, which puts it in the harvest log:

```
3 datetime candidates ['27.09.2024 | 20:15', '20:15 Uhr - 22:15', '12.08.2024'] vs startingAt 2024-09-27T20:15 -> confidence medium
```

At most 5 input strings are listed, then `+N more`; each is trimmed to 40 characters.

**Tests.**

- `DateTimeExtractorTest`: the existing cases, plus bodies with several candidates, duplicates, and none.
- A table-driven test for each confidence rule.
- `FulltextPluginTest`: the line is logged when `startingAt` is set, and nothing is logged when `startingAt` is missing or `extractDates = false`.

### Open Points

- [ ] End times, ranges ("bis", "–") and series detection are later plans. `Document` has no end field, so storing an end needs a migration and a schema change.
- [ ] The flaky "12 Dezember 24" case (`docs/tasks.md`) stays open unless the candidate logs reveal its cause along the way.

## Slices

- `feature/datetime-extractor` — rename to DateTimeExtractor, candidate extraction, confidence, log line in FulltextPlugin <!-- builds: DateTimeExtractor.extractCandidates, a multi-candidate datetime scan with confidence against startingAt -->

## Notes

- 2026-09-17: Design agreed in session. Rejected: a separate plugin (schema enum change), a fixed pipeline step for every document, and a separate trace table (migration before we know which signals matter). The step runs only when `startingAt` is set, so it never fills a missing start date in this plan.
- Uncommitted `FulltextPlugin` work was stashed (`stash@{0}`, "wip before datetime-extractor") before branching. This plan builds on the committed version.
