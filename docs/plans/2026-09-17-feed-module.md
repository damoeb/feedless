# Extract the feed vertical from server-core into `packages:feed`

> The feed engine still lives inside `server-core`'s `scrape` and `pipeline` packages, although nothing outside `server-core` references it. It moves to a new `packages:feed` module, and the four pieces `domain` can legitimately own move there.

## Status

- **State:** Approved
- **Type:** infra
- **Review:** PR
- **Impl:** same branch
- **Approved:** 2026-09-17, damoeb, plan-PR #125 reviewed

## Approval

- **Assignee:** damoeb

## Changelog

- No user-visible change. The feed engine becomes its own Gradle module, so a change to feed handling no longer means touching `server-core`.

## Problem

`server-core` still owns the code that turns a web page or a native feed into a `JsonFeed`, even though the rest of the feed vertical has already been pushed outward: `FeedService`, `FeedParser`, `JsonFeed`, `FeedUtil`, `RemoteNativeFeedRef` and `GenericFeedRule` live in `domain`; `FeedResolver` in `graphql-api`; `FeedController` in `http-api`; parsers and exporters in `feed-parser`. What remains in `server-core` is the engine itself, mixed into the `scrape`, `pipeline/plugins` and `pipeline/transformer` packages, where it is indistinguishable from the generic scraping infrastructure that surrounds it.

Nothing outside `server-core` references any of this code, so the boundary is already latent — it is simply not expressed in the build.

## Goal

Move the feed engine into a new Gradle module `packages:feed`, and push down to `domain` every piece that `domain` can legitimately own. Behaviour must not change.

## Non-goals

- Decoupling the feed engine from the GraphQL generated types. `WebToFeedTransformer`, `WebExtractService` and both plugins use `DOMExtract`, `ScrapeEmit`, `ScrapeExtractResponse`, `Record` and `RemoteNativeFeed` as internal working types. Introducing domain equivalents and mapping at the `graphql-api` edge would be a rewrite of an 868-line file plus changes in `ScrapeService` and `ScraperAdapter`. `packages:feed` therefore depends on `graphql-api`, exactly as `server-core` does today.
- Any refactoring of `WebToFeedTransformer` itself, including the `// todo split into list parser, field parser` it carries.
- Touching the other scrape services (`ScrapeService`, `ScraperAdapter`, `PageInspectionService`, `MarkupSimplifier`, `WebToArticleTransformer`, `FulltextPlugin`), which stay in `server-core`.

## The new module

`packages:feed`, included in `settings.gradle`, with:

```
dependencies {
  api(project(":packages:domain"))
  implementation(project(":packages:feed-parser"))
  implementation(project(":packages:graphql-api"))
}
```

plus the third-party dependencies these files already use: jsoup, xsoup, gson, commons-lang3, kotlinx-coroutines, spring-context, slf4j.

`server-core` gains `implementation(project(":packages:feed"))`. `packages:feed` must never depend on `server-core`.

### Files moved from `server-core/src/main`

Kotlin package names are unchanged, so no import in any other file has to move.

| File | Rationale |
|---|---|
| `feed/FeedParserService.kt` | the `FeedParser` port implementation |
| `feed/discovery/GenericFeedLocator.kt` | feed discovery |
| `feed/discovery/NativeFeedLocator.kt` | feed discovery |
| `scrape/WebToFeedTransformer.kt` | the web-to-feed engine, and the `WebToFeed` port implementation |
| `scrape/WebExtractService.kt` | referenced only by `WebToFeedTransformer` and `FeedPlugin` |
| `pipeline/transformer/Transformer.kt` | referenced only by `WebExtractService` and `MarkupToListTransformer` |
| `pipeline/transformer/MarkupToListTransformer.kt` | referenced by nothing else; travels with `Transformer` |
| `pipeline/plugins/FeedPlugin.kt` | the `org_feedless_feed` plugin |
| `pipeline/plugins/FeedsPlugin.kt` | the `org_feedless_feeds` plugin |
| `pipeline/plugins/FeedParamsInput.kt` | `FeedPlugin`'s params holder |

### Spring wiring

No configuration changes. `@SpringBootApplication` sits on `org.migor.feedless.FeedlessApplication` and component-scans `org.migor.feedless`, which the moved classes remain under. Every moved bean keeps its existing `@Profile("${AppProfiles.scrape} & ${AppLayer.service}")` gate.

## Pushed down to `domain`

1. **`pipeline/FragmentTransformerPlugin.kt`** moves from `server-core` to `domain/pipeline`. It references only domain types (`ExecuteAction`, `HttpResponse`, `LogCollector`, `Plugin`) and belongs beside the `Plugin`, `FilterEntityPlugin` and `MapEntityPlugin` family already there. This is the change that makes it possible for the two feed plugins to live outside `server-core` at all. `FragmentOutput` stays in `graphql-api`, where it already is.

2. **`scrape/WebToTextTransformer.kt`** moves from `server-core` to `domain/scrape`. It is 49 lines with no dependency beyond jsoup, which `domain` already exposes as an `api` dependency. It is used by `WebToFeedTransformer` (moving to `packages:feed`) and by `FulltextPlugin` (staying in `server-core`); `domain` is the only shared home that does not create a cycle.

3. **`AppConfig` gains `locale`.** *(Superseded: develop's per-use-case properties refactor (#110) deleted `AppConfig` and `PropertyService` outright. After merging develop, `WebToFeedTransformer` injects `LocaleProperties` and reads `defaultLocale`, which achieves the same thing — the transformer no longer depends on a `server-core` class.)* `WebToFeedTransformer`'s sole use of `PropertyService` is `propertyService.locale`. `PropertyService` already implements `AppConfig`, so adding the property to the port lets the transformer inject `AppConfig` and drop its dependency on a `server-core` class.

4. **`FeedParserService` injects `HttpFetcher` instead of `HttpService`.** Its two calls, `httpService.prepareGet(url)` followed by `httpService.executeRequest(request, 200)`, map exactly onto the existing domain port's `httpGet(url, 200)`. `HttpService` remains in `server-core` as the adapter.

## Tests

| Test | Destination | Why |
|---|---|---|
| `pipeline/plugins/FeedPluginTest` | `packages:feed` | plain Mockito, no Spring context |
| `transform/WebExtractServiceTest` | `packages:feed` | plain Mockito, no Spring context |
| `util/FeedUtilTest` | `domain` | `FeedUtil` already lives in `domain` |
| `transform/WebToTextTransformerTest` | `domain` | follows its subject |
| `feed/FeedControllerIntTest` | stays in `server-core` | `@SpringBootTest` booting `FeedlessApplication` |
| `pipeline/plugins/FeedsPluginIntTest` | stays in `server-core` | `@SpringBootTest` booting `FeedlessApplication` |
| `transform/WebToFeedTransformerIntTest` | stays in `server-core` | `@SpringBootTest` booting `FeedlessApplication` |

The three integration tests boot the assembled application and use `server-core` test fixtures (`DisableDatabaseConfiguration`, `PropertiesConfiguration`, `any2`, the Mockito bean overrides). Moving them would put `server-core` on the new module's test classpath, which is a dependency cycle. Leaving them where they are is consistent with `server-core`'s stated role as the module that assembles the others, but it does mean `packages:feed` ships with unit tests only, and that `WebToFeedTransformer`'s principal coverage stays behind in `server-core`. This is accepted for this change.

## Deviations found during implementation

Two corrections to the file lists above, both forced by the compiler.

1. **`FragmentTransformerPlugin` goes to `graphql-api`, not `domain`.** Its `transformFragment` returns `FragmentOutput`, which is built from the generated `ScrapeExtractFragment` and `ScrapedFeeds`. `graphql-api` depends on `domain`, not the reverse, so `domain` cannot name that return type. The interface now sits beside `FragmentOutput` in `graphql-api/pipeline/`, carrying the same note that `BrowserAutomationGateway` already carries. `packages:feed` and `server-core` both depend on `graphql-api`, so every caller still resolves it. The domain push is therefore three items, not four.

2. **`pipeline/plugins/SelectorsInput.kt` and `ExtendContentOptions.kt` move to `packages:feed` too.** `FeedParamsInput` references `SelectorsInput` unqualified, as a same-package type; these two hand-written plugin-param DTOs are used by no other plugin in `server-core` main code. They are feed's own params, so they travel with `FeedPlugin`. Twelve files move to `packages:feed`, not ten.

`packages:feed` also needs two dependencies the plan did not list: `commons-text`, which `WebToFeedTransformer` uses for `LevenshteinDistance` in pagination detection, and `testFixtures(project(":packages:domain"))` for the `any2` Mockito helper that `FeedPluginTest` uses.

## Documentation to update

- `settings.gradle.kts` — add `include("packages:feed")`.
- `AGENTS.md` — add `feed` to the module table; correct the counts in the Modules section from "22 directories, only 17 are Gradle modules" to 23 and 18.

## Verification

`./gradlew lint test` from the repository root, exit code 0. Because the change is a relocation, the existing test suite is the whole safety net: the same tests that pass before the move must pass after it, with no test deleted and none newly `@Disabled`.

## Delivery

One branch, `infra/feed-module`, off `develop`, worked in a git worktree at `.worktrees/feed-module`. Commits follow Conventional Commits with the module as scope.
