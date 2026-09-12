# Hexagonal module split — implementation inventory

Source spec: `docs/superpowers/specs/2026-09-12-hexagonal-modules-design.md` (worktree `.worktrees/feed-ctl`, branch `feature/feed-ctl`, HEAD `8ab731034`). Research is read-only; nothing in the repo was changed.

**Decisions after this inventory was written (override conflicting lines below):** `RepositoryHarvester` moves to `domain` with its own scrape-result types (step 5 main path; "Alternative A" is not taken). Flyway migrations move to `jpa-data` (§0.6). The executable task order is in `2026-09-12-hexagonal-modules.md`.

## Conventions used throughout

- Paths are repo-relative, `packages/<module>/src/<main|test|testFixtures>/kotlin/org/migor/feedless/...`. Packages never change; only the source root does. Every move is a `git mv`.
- "(edit)" after a move means the file needs code changes beyond the move; the reason follows.
- Ports live in `domain`, named after a capability, in the feature package listed below. The `server-core` class named in each port section implements it (`: PortName` on the existing class unless an adapter is called out).
- Catalog aliases refer to `gradle/libs.versions.toml`. Versions come from `implementation(platform(libs.spring.boot.bom))` (Spring Boot 3.4.11), the pattern `jpa-data`, `feed-parser`, `mail-adapter`, `stripe-payments` and `github-connector` already use; `server-core` uses the `io.spring.dependency-management` plugin instead.

## Port names (fixed for all steps)

| Port (domain FQN) | Implemented by (server-core) | Introduced in step |
|---|---|---|
| `org.migor.feedless.session.TokenIssuer` | `session/JwtTokenIssuer.kt` | 3 |
| `org.migor.feedless.session.SessionCookies` | `session/CookieProvider.kt` | 4 |
| `org.migor.feedless.common.AppConfig` | `common/PropertyService.kt` | first step that needs it |
| `org.migor.feedless.pipeline.PipelinePlugins` | `pipeline/PluginService.kt` | 5 |
| `org.migor.feedless.message.Notifications` | `transport/TelegramBotService.kt` + `message/MessageService.kt` | 3 or 5 (see steps) |
| `org.migor.feedless.scrape.Scraper` | `scrape/ScrapeService.kt` | 5 |
| `org.migor.feedless.feed.FeedParser` | `feed/FeedParserService.kt` | 7 |
| `org.migor.feedless.scrape.WebToFeed` | `scrape/WebToFeedTransformer.kt` | 7 |
| `org.migor.feedless.session.UserAuthenticator` | `session/AuthService.kt` (Stateful/Stateless) | 7 |
| `org.migor.feedless.agent.AgentDirectory` | `agent/AgentService.kt` + `agent/AgentRegistry.kt` | 7 |
| `org.migor.feedless.analytics.Analytics` | `analytics/AnalyticsService.kt` | 6 or 7 |

No type with any of these simple names exists anywhere in `packages/` today (checked), so there are no clashes.

## Cross-cutting facts found during research (affect several steps)

- **`feed-parser` → `domain` cycle.** `packages/feed-parser/build.gradle.kts` has `api(project(":packages:domain"))`, but use cases that move to `domain` use `org.migor.feedless.feed.parser.json.JsonItem`/`JsonFeed`/`JsonAttachment`/`JsonPoint` (feed-parser) and `org.migor.feedless.common.HttpResponse`. `domain` cannot depend on `feed-parser`. The json types only depend on `RepositoryId`, gson and kotlinx-serialization (all already in `domain`), so step 0 moves them to `domain` (same packages).
- **Duplicate FQNs already on the classpath.** `org.migor.feedless.common.HttpResponse` is declared in both `feed-parser/.../common/HttpResponse.kt` and `server-core/.../common/HttpService.kt` (identical); `object org.migor.feedless.util.HtmlUtil` and `object org.migor.feedless.util.FeedUtil` exist in both `feed-parser` and `server-core`. Today whichever jar wins is used; moves must not add a third copy.
- **Two `Mother` objects in the same package.** `domain/src/test/.../Mother.kt` (random-field `randomUser`, `randomOrderID`, `randomUserID`, …; consumed by `mail-adapter` and `stripe-payments` through the `testOutput` configuration) and `server-core/src/test/.../Mother.kt` (`randomUserId`/`randomGroupId`/`randomDocumentId`/`randomRepositoryId`/`randomSourceId`/`randomConnectedAppId`, deterministic `randomUser`, `randomOneTimePassword`, plus top-level Mockito helpers `anyList`/`any`/`any2`/`anyOrNull2`/`argThat`/`anyOrNull`/`eq`). Once `domain` exposes a test fixture, both are `org.migor.feedless.Mother` on server-core's test classpath: they must be merged.
- **Server-core `Exceptions.kt`** declares `BadRequestException`, `UnavailableException`, `SiteNotFoundException`, `NoItemsRetrievedException`, `TemporaryServerException` in package `org.migor.feedless`. Use cases and `GraphQLExceptionHandler` need them outside `server-core`, so they merge into `domain/.../Exceptions.kt` in step 0.
- **Every existing "IntTest" is a `@SpringBootTest`.** None is a `@DataJpaTest` slice. "Persistence-only" below means the test autowires only repositories, DAOs, `PlatformTransactionManager`/`DataSource` and jpa-data beans. It can run in `jpa-data` once `jpa-data` has a test `@SpringBootApplication`; tests that autowire a use case or a server-core bean stay.
- **Security bridge needs OAuth2 types.** `session/SessionService.kt` uses `OAuth2AuthenticationToken` (spring-security-oauth2-client), `Jwt` (spring-security-oauth2-jose), `LazyGrantedAuthority` and `Jwt.capabilities()` (in `session/JwtRequestFilter.kt`). Moving it to `domain` needs more than `spring-security-core` (see step 0).


## Reconciliation across steps (read before executing; overrides conflicting lines below)

The step sections were researched in parallel. Where they overlap, these rules apply:

- **Mappers move in step 0, all of them.** Step 0 moves `api/DtoMapperExtensions.kt`, `api/DtoEnumMapper.kt` and all 13 remaining `api/mapper/*.kt` files to `graphql-api`, and deletes `api/mapper/FeatureMapper.kt`. Rows in steps 1, 3 and 5 that move `api/mapper/ProductMapper.kt`, `UserMapper.kt`, `UserSecretMapper.kt`, `RepositoryMapper.kt`, `RepositoryCommandMapper.kt`, `DocumentMapper.kt`, `DocumentCommandMapper.kt`, `SourceMapper.kt`, `ScrapeActionMapper.kt` or `ScrapeResponseMapper.kt` are no-ops; so are the "if step 0 did not" rows in step 5a. The feature-owned server-core mappers `product/ProductMapper.kt`, `product/PricedProductMapper.kt` and `order/OrderMapper.kt` have no callers and are deleted in step 2. `annotation/AnnotationDtoMapper.kt` moves in step 6.
- **Value types step 0 must pull into `domain`** (because the mappers need them): `GenericFeedRule`, `GenericFeedSelectors`, `ExtendContext`, `GenericFeedParserOptions` (split out of `scrape/WebToFeedTransformer.kt`), `RemoteNativeFeedRef`, `FeedType` (from feed-parser), and the plugin parameter types (`CompareBy`, `RecordField`, `ConditionalTag`, `CompositeFieldFilterParams`, `DiffRecordsParams`, `FeedPluginParams`, `FulltextPluginParams`, `ItemFilterParams`, `CompositeFilterParams`, `NumericalFilterParams`, `StringFilterParams`, `StringFilterOperator`, `NumberFilterOperator`, `EventsReportPluginParams` + `toPluginExecutionJson`, `createAttachmentUrl`). Step 5a's and step 6's "prerequisites" lists are then already satisfied.
- **`common.AppConfig` grows over the steps:** `val apiGatewayUrl: String` (step 0, `DocumentMapper`), `val appHost: String` (step 2, `PaymentController`). Steps 5 and 7 reuse those two members and add no new ones. It has 2 members in total.
- **The coroutine-context extensions** (`userId()`, `userIdMaybe()`, `groupId()`, `corrId()`, `isAdmin()`, declared at the bottom of `user/UserUseCase.kt`) move to `domain` in step 1, not step 3. `UserGuard` moves in step 2, not step 3. `OrderResolver` moves in step 3, not step 2.
- **`Analytics`** is introduced in step 5 (`DocumentController`), not step 6/7. **`ApiUrls`** moves to `domain` in step 6 (`ReportController`, `MailController`). **`TokenIssuer`** is created in step 3 with 2 methods, grows to 4 in step 4 and to 6 in step 7. **`Scraper`** gains `fetch` in step 7 and **`PipelinePlugins`** gains `describeAll` in step 7.
- **`UserAuthenticator` and `SessionCookies` are not created.** `FeedService` injects `AuthService` but never calls it, so drop the constructor parameter. `SessionTokenPort` already covers the cookie calls.
- **Flyway migrations move to `jpa-data` in step 0** (`packages/server-core/src/main/resources/db/migration` → `packages/jpa-data/src/main/resources/db/migration`). The files themselves are unchanged, so checksums stay the same. AGENTS.md rule 5 and the Pre-Commit checklist name the old path, so step 8 updates them. So does `FlywayTargetTest`/the duplicate-version test from commit `9a40a8a22`, in step 0.
- **Persistence-only tests.** Every Postgres test is a full `@SpringBootTest` today. Three can move to `jpa-data` in step 5: `harvest/HarvestRepositoryIntTest.kt`, `source/SourceRepositoryIntTest.kt` and `repository/AbstractRepositoryEntityTest.kt`. They need the step-0 `JpaDataTestApplication`, and their `StatelessAuthService` mock is dropped. Every other IntTest stays in server-core.
- **Test counting.** Delete stale results first. Otherwise a test that has moved is counted in both the old and new module:
  `trash packages/*/build/test-results`, then run the Gradle command from the Baseline, then the counting one-liner.
- **`message.Notifications` is one port, not two.** Step 3 defines it with the 2 Telegram methods `UserUseCase` calls, implemented by `TelegramBotService`. Step 5 defines it with the 1 method `DocumentUseCase` calls, implemented by a new server-core `NotificationsAdapter` over `TelegramBotService` + `MessageService`. Create `NotificationsAdapter` already in step 3 (it is unconditional, unlike `@ConditionalOnBean TelegramBotService`), with the step-3 methods, and inject `Notifications` rather than `Optional<Notifications>`. Step 5 then adds its 1 method, for 3 methods in total. Do not have `TelegramBotService` or `MessageService` implement the port directly.
- **`TokenIssuer` return types.** Steps 3–4 give the port domain types (`AuthToken`, `HttpSetCookie`). Steps 6–7 add `decodeJwt(token: String): Jwt` and `createJwtForAnonymousFeed(host: String, id: RepositoryClaimId): Jwt`, which return `org.springframework.security.oauth2.jwt.Jwt`. That type is acceptable only because step 0 already gives `domain` `spring-security-oauth2-jose` for `injectCapabilitiesFromJwt(jwt: Jwt)`. If you want the port free of Spring types, `createJwtForAnonymousFeed` can return `AuthToken`. `decodeJwt` feeds `injectCapabilitiesFromJwt` directly, so it keeps `Jwt`.


## Baseline

Abbreviations below: `SC` = `packages/server-core/src/main/kotlin/org/migor/feedless`, `SCT` = `packages/server-core/src/test/kotlin/org/migor/feedless`, `DOM` = `packages/domain/src/main/kotlin/org/migor/feedless`, `GQL` = `packages/graphql-api/src/main/kotlin/org/migor/feedless`, `FP` = `packages/feed-parser/src/main/kotlin/org/migor/feedless`. In the move tables the full path is always written out on the `to` side.

Test classes today, counted as files containing `@Test|@ParameterizedTest|@Property|@RepeatedTest`:

| Module | Test classes | Annotated methods | Notes |
|---|---|---|---|
| domain | 1 | 2 | `capability/UserCapabilityTest`; `Mother.kt` in `src/test` |
| jpa-data | 1 | 1 | `data/jpa/user/UserMapperTest` |
| graphql-api | 0 | 0 | no `tasks.test { useJUnitPlatform() }` yet |
| http-api | 15 | 154 | |
| server-core | 115 | 512 | 12 classes tagged `nlp` and 1 tagged `unstable` (`LicenseUseCaseTest`) are excluded by `excludeTags`; `@Disabled` methods count as skipped |
| feed-parser | 5 | 5 | |
| mail-adapter | 2 | 2 | uses domain `testOutput` Mother |
| stripe-payments | 1 | 7 | uses domain `testOutput` Mother |
| github-connector | 4 | 19 | |
| freemarker-templates | 1 | 5 | |
| document-classifier | 1 | 1 | |

Class count command (source side):

```bash
for m in domain jpa-data graphql-api http-api server-core feed-parser mail-adapter stripe-payments github-connector freemarker-templates document-classifier; do echo "$m $(grep -rlE '@(Test|ParameterizedTest|Property|RepeatedTest)\b' packages/$m/src/test 2>/dev/null | wc -l)"; done
```

Executed-test totals (run side). Always delete old results first, because a moved test leaves stale XML in its old module and gets counted twice:

```bash
trash packages/*/build/test-results 2>/dev/null; ./gradlew :packages:domain:test :packages:jpa-data:test :packages:graphql-api:test :packages:http-api:test :packages:server-core:test :packages:feed-parser:test :packages:mail-adapter:test :packages:stripe-payments:test --continue
```

```bash
python3 -c "import glob,collections,xml.etree.ElementTree as E;c=collections.defaultdict(collections.Counter);[c[f.split('/')[1]].update({k:int(E.parse(f).getroot().get(k,0)) for k in ('tests','skipped','failures','errors')}) for f in glob.glob('packages/*/build/test-results/test/*.xml')];[print(m,dict(v),'executed=',v['tests']-v['skipped']) for m,v in sorted(c.items())];t=sum(c.values(),collections.Counter());print('TOTAL',dict(t),'executed=',t['tests']-t['skipped'])"
```

server-core needs Docker (Testcontainers/PostGIS). Record the TOTAL `executed` before step 0; every later step must match or exceed it. server-core currently has only 1 stale XML file, so a fresh run is mandatory for the baseline.

## Step 0 — Cross-cutting

### 0.1 Moves

All moves use `git mv`. The package stays the same unless noted. **E** means the file needs edits after the move.

**Security bridge → domain**

| from | to | E |
|---|---|---|
| `SC/session/SessionService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/session/SessionService.kt` | – (package `session` kept, so none of the ~20 callers change their import; the spec says "next to RequestContext", but moving it to package `capability` would force import edits in every resolver and controller for no gain) |
| `SC/session/LazyGrantedAuthority.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/session/LazyGrantedAuthority.kt` | E: change `internal val payload` to `val payload`, because server-core reads `.payload` in `api/throttle/IpThrottleService.kt:43`, `session/TokenAuthenticator.kt:33-34` and the test `SCT/session/JwtClaims.kt:12,17` |
| `SC/capability/SecurityContextCapabilityService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/capability/SecurityContextCapabilityService.kt` | – (keep `@Component("capabilityService")`: http-api's `@PreAuthorize("@capabilityService.hasCapability('user')")` / `hasToken()` in `HarvestHttpController` and `RepositoryHttpController` resolve by that bean name) |

**GraphQL infrastructure → graphql-api**

| from | to | E |
|---|---|---|
| `SC/api/graphql/GraphQLExceptionHandler.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/graphql/GraphQLExceptionHandler.kt` | – (compiles once 0.3 has moved `BadRequestException`/`UnavailableException` to domain) |
| `SC/config/GraphqlConfig.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/config/GraphqlConfig.kt` | – (holds `GraphqlConfig`, `CustomContextBuilder`, `DgsCustomContext`, `UrlValidatorDirective`, `XPathValidatorDirective`, `EmailValidatorDirective`; needs xsoup and commons-validator) |
| `SC/common/CacheKeyGenerator.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/common/CacheKeyGenerator.kt` | – (uses generated `ServerSettingsContextInput`; its only consumer is `GraphqlConfig.cacheKeyGenerator()`) |

`CustomDgsWebSocketConfig` stays in server-core. `DgsCustomContext` keeps package `config`, so `AnnotationResolver` and `DocumentResolver` need no import edits.

**Shared mappers → graphql-api.** The spec's "shared mappers" cannot move on their own. `DtoMapperFacade` (in `DtoMapperExtensions.kt`) injects `DocumentMapper`, `FeatureMapper`, `UserSecretMapper` and `ScrapeResponseMapper`, and calls `toSource`/`toDto` (`SourceMapper`) and `scrapeFlowFromDto` (`ScrapeActionMapper`). `ScrapeActionMapper` imports `toParams` from `RepositoryCommandMapper`. `ScrapeResponseMapper` and `UserSecretMapper` `use` `EnumMapper`. `ProductMapper`, `RepositoryMapper` and `ScrapeActionMapper` import `api.toDto`/`fromDto` from `DtoEnumMapper`/`DtoMapperExtensions`. The minimal consistent set is therefore the whole `api/mapper/` directory plus the two `api/*.kt` files:

| from | to | E |
|---|---|---|
| `SC/api/DtoMapperExtensions.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/DtoMapperExtensions.kt` | E: delete the facade members `toDto(entity: FeatureGroupEntity, …)` and `toDto(entity: FeatureValueEntity)`, the extension functions `FeatureGroupEntity.toDto`/`FeatureValueEntity.toDto` (no caller anywhere; they are the only jpa-data imports), the `featureMapper` constructor parameter, and `createDocumentUrl` (moves to domain, see 0.4); change `PropertyService` to `AppConfig` in `toDto(document, …)` and in `Document.toDto(…)` |
| `SC/api/DtoEnumMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/DtoEnumMapper.kt` | – (once the value types in 0.4 are in domain) |
| `SC/api/mapper/DocumentCommandMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/DocumentCommandMapper.kt` | – |
| `SC/api/mapper/DocumentMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/DocumentMapper.kt` | E: `abstract fun toDto(document: Document, appConfig: AppConfig): Record`; `getAttachments(document: Document, appConfig: AppConfig)`; in `@Mapping(target="attachments", expression="java(getAttachments(document, appConfig))")` rename the parameter; the import of `pipeline.plugins.createAttachmentUrl` stays unchanged (the function moves to domain in the same package) |
| `SC/api/mapper/EnumMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/EnumMapper.kt` | – |
| `SC/api/mapper/FeatureMapper.kt` | **delete** (`git rm`) | – (its only two methods map jpa-data entities and have no callers) |
| `SC/api/mapper/MapperUtil.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/MapperUtil.kt` | – |
| `SC/api/mapper/MapStructConfig.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/MapStructConfig.kt` | – |
| `SC/api/mapper/ProductMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/ProductMapper.kt` | – |
| `SC/api/mapper/RepositoryCommandMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/RepositoryCommandMapper.kt` | – |
| `SC/api/mapper/RepositoryMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/RepositoryMapper.kt` | – |
| `SC/api/mapper/ScrapeActionMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/ScrapeActionMapper.kt` | – |
| `SC/api/mapper/ScrapeResponseMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/ScrapeResponseMapper.kt` | – |
| `SC/api/mapper/SourceMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/SourceMapper.kt` | – |
| `SC/api/mapper/UserMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/UserMapper.kt` | – |
| `SC/api/mapper/UserSecretMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/UserSecretMapper.kt` | – |
| `SCT/api/mapper/RepositoryCommandMapperTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/api/mapper/RepositoryCommandMapperTest.kt` | – (imports only generated types and domain) |

Feature-owned mappers stay for their own steps: `SC/order/OrderMapper.kt`, `SC/product/ProductMapper.kt` and `SC/product/PricedProductMapper.kt` (step 2; these are MapStruct mappers over jpa-data entities, so server-core keeps kapt until then), and `SC/annotation/AnnotationDtoMapper.kt` (step 6).

`api/ApiParams.kt` and `api/ApiUrls.kt` stay in server-core, used by `SecurityConfig`, `JwtRequestFilter`, `FeedController`, `MailController` and `ReportController`. The steps that move those controllers must move them too, probably to domain or http-api.

Code that stays in server-core and imports the moved mappers still compiles, because server-core has `api(project(":packages:graphql-api"))`. That covers `agent/AgentService.kt` (`api.fromDto`, `api.mapper.toDto`), `pipeline/plugins/FeedsPlugin.kt` (`api.isHtml`, `api.toDto`), `api/graphql/ServerConfigResolver.kt`, every resolver still in server-core, and the tests `SCT/document/DocumentResolverTest.kt` (it calls `documentMapper.toDto(incoming, propertyService)`, which still compiles because `PropertyService : AppConfig`), `SCT/repository/RepositoryResolverTest.kt` and `SCT/source/SourceUseCaseTest.kt` (it imports `api.mapper.fromDto`/`toSource`, so step 5 must remove that before the test moves to domain). No reverse import exists.

**Value types → domain** (the mappers in graphql-api cannot see server-core)

| from | to | E |
|---|---|---|
| `SC/feed/discovery/RemoteNativeFeedRef.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/discovery/RemoteNativeFeedRef.kt` | – (needs `FeedType` in domain, see 0.2) |
| `SC/pipeline/plugins/NumberFilterOperator.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/NumberFilterOperator.kt` | – |
| `SC/pipeline/plugins/StringFilterOperator.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/StringFilterOperator.kt` | – |
| `SC/pipeline/plugins/NumericalFilterParams.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/NumericalFilterParams.kt` | – (gson `@SerializedName`) |
| `SC/pipeline/plugins/StringFilterParams.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/StringFilterParams.kt` | – |

Split files: cut the value types out of a file that also holds a `@Service` and paste them into a new domain file in the same package.

| Cut from | Types | New domain file |
|---|---|---|
| `SC/scrape/WebToFeedTransformer.kt` lines 79–131 | `abstract class Selectors`, `GenericFeedRule`, `GenericFeedParserOptions` (`@JsonIgnoreProperties`), `GenericFeedSelectors` (`@JsonIgnoreProperties`), `ArticleContext` (jsoup `Element`), `enum class ExtendContext` | `packages/domain/src/main/kotlin/org/migor/feedless/scrape/GenericFeedRule.kt` |
| `SC/pipeline/plugins/DiffRecordsPlugin.kt` lines 46–71 | `DiffRecordsParams`, `CompareBy`, `enum class RecordField` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/PluginParams.kt` |
| `SC/pipeline/plugins/CompositeFilterPlugin.kt` lines 20–30 | `ItemFilterParams`, `CompositeFilterParams`, `typealias CompositeFilterPluginParams` | same `PluginParams.kt` |
| `SC/pipeline/plugins/ConditionalTagPlugin.kt` lines 24–36 | `ConditionalTag`, `CompositeFieldFilterParams`, `typealias ConditionalTagPluginParams` | same `PluginParams.kt` (`Document.asJsonItem` at line 87 stays; steps 5 and 7 need it in domain) |
| `SC/pipeline/plugins/FeedPlugin.kt` lines 29–31 | `FeedPluginParams` | same `PluginParams.kt` |
| `SC/pipeline/plugins/FulltextPlugin.kt` lines 38–44 | `FulltextPluginParams` | same `PluginParams.kt` |
| `SC/pipeline/plugins/PrivacyPlugin.kt` lines 253–254 | `createAttachmentUrl` | `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/AttachmentUrls.kt` (see 0.4) |
| `SC/api/DtoMapperExtensions.kt` | `createDocumentUrl` | `packages/domain/src/main/kotlin/org/migor/feedless/api/DocumentUrls.kt` (see 0.4) |

`EventsReportPluginParams` and `toPluginExecutionJson` (`EventsReportPlugin.kt:21-33`) are not needed here, but `ReportUseCase` needs them in step 6. Moving them into `PluginParams.kt` now keeps the plugin params in one place.

**Tests and fixtures**

| from | to | E |
|---|---|---|
| `packages/domain/src/test/kotlin/org/migor/feedless/Mother.kt` | `packages/domain/src/testFixtures/kotlin/org/migor/feedless/Mother.kt` | E: merge (0.5) |
| `SCT/Mother.kt` | merge its members into the file above, then `git rm`; its top-level mockito helpers go to `packages/domain/src/testFixtures/kotlin/org/migor/feedless/MockitoHelpers.kt` | E |
| `SCT/PostgreSQLExtension.kt` | `packages/jpa-data/src/testFixtures/kotlin/org/migor/feedless/PostgreSQLExtension.kt` | – |
| `SCT/ExceptionsTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/ExceptionsTest.kt` | – (mocks only exception classes, all in domain after 0.3; domain needs mockito as a test dependency) |

`SCT/TestConfigurations.kt` stays in server-core.

### 0.2 feed-parser → domain (breaks the domain → feed-parser cycle)

feed-parser has `api(project(":packages:domain"))`, so domain cannot depend on feed-parser. Use cases that will live in domain need `JsonItem`, `JsonFeed`, `JsonAttachment`, `JsonPoint` and `HttpResponse`: `DocumentUseCase`, `RepositoryUseCase`, `RepositoryHarvester`, `FeedService`, `FilterEntityPlugin`, `FragmentOutput`. Move these files; packages stay the same, and the 16 feed-parser and 39 server-core importers need no edits:

| from | to |
|---|---|
| `FP/feed/parser/json/JsonAttachment.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/JsonAttachment.kt` |
| `FP/feed/parser/json/JsonAuthor.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/JsonAuthor.kt` |
| `FP/feed/parser/json/JsonFeed.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/JsonFeed.kt` (kotlinx `@Serializable`; domain already applies the serialization plugin) |
| `FP/feed/parser/json/JsonItem.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/JsonItem.kt` (imports `RepositoryId`, which is in domain) |
| `FP/feed/parser/json/JsonPoint.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/JsonPoint.kt` |
| `FP/feed/parser/json/OpenSearchDescription.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/OpenSearchDescription.kt` |
| `FP/feed/parser/json/OpenSearchQuery.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/OpenSearchQuery.kt` |
| `FP/feed/parser/json/OpenSearchResponse.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/json/OpenSearchResponse.kt` |
| `FP/common/HttpResponse.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/common/HttpResponse.kt` |
| `FP/util/FeedUtil.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/util/FeedUtil.kt` (imports `HttpResponse`, `FeedType`, spring-core `MimeType`; `FeedService`, and possibly `RepositoryHarvester`, need it in domain) |
| `FP/util/HtmlUtil.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/util/HtmlUtil.kt` (jsoup `Safelist`) |

Edits:

- `FP/feed/parser/XmlFeedParser.kt:286-288`: cut `enum class FeedType { ATOM, JSON, CALENDAR }` into the new file `packages/domain/src/main/kotlin/org/migor/feedless/feed/parser/FeedType.kt`, keeping package `org.migor.feedless.feed.parser`. `JsonFeedParser`, `CalendarFeedParser` and `XmlFeedParser` use it in the same package, so they need no import.
- Duplicates with the same FQN (a split package that works today only through classpath order): `SC/common/HttpService.kt:219-224` holds a `data class HttpResponse` identical to feed-parser's; delete it. `SC/util/FeedUtil.kt` and `SC/util/HtmlUtil.kt` are byte-identical to feed-parser's; delete both. `SCT/util/FeedUtilTest.kt` stays and compiles against domain's copy.

### 0.3 Exceptions

- Append to `packages/domain/src/main/kotlin/org/migor/feedless/Exceptions.kt` (needs `import java.time.Duration`): `BadRequestException(message) : FatalHarvestException`, `UnavailableException(message) : ResumableHarvestException(message, Duration.ofMinutes(5))`, `SiteNotFoundException(url) : FatalHarvestException("$url not found")`, `NoItemsRetrievedException : RuntimeException("no items retireved")`, `TemporaryServerException(message, waitForRefill) : ResumableHarvestException`.
- `git rm packages/server-core/src/main/kotlin/org/migor/feedless/Exceptions.kt`. Same FQN, so no import edits.

### 0.4 Port and detachments

**Port introduced: `org.migor.feedless.common.AppConfig` (1 member).** `DocumentMapper` in graphql-api needs `apiGatewayUrl` and cannot see `PropertyService`. Later steps add members to this port.

```kotlin
// packages/domain/src/main/kotlin/org/migor/feedless/common/AppConfig.kt
package org.migor.feedless.common

interface AppConfig {
  val apiGatewayUrl: String
}
```

Implementation: `SC/common/PropertyService.kt` becomes `class PropertyService : AppConfig`, with `lateinit var apiGatewayUrl: String` changed to `override lateinit var apiGatewayUrl: String`. `@ConfigurationProperties` binding is unaffected. Every caller that passes `propertyService` still compiles.

```kotlin
// packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/AttachmentUrls.kt
package org.migor.feedless.pipeline.plugins
fun createAttachmentUrl(appConfig: AppConfig, id: AttachmentId): String =
  "${appConfig.apiGatewayUrl}/attachment/${id.uuid}"

// packages/domain/src/main/kotlin/org/migor/feedless/api/DocumentUrls.kt
package org.migor.feedless.api
fun createDocumentUrl(appConfig: AppConfig, id: DocumentId): String =
  "${appConfig.apiGatewayUrl}/article/${id}"
```

The callers keep their imports: `PrivacyPlugin` (same package), `RepositoryUseCase` (`api.createDocumentUrl`, `pipeline.plugins.createAttachmentUrl`) and `DocumentMapper`.

What each named class uses from `api.*`, and what to do about it:

| Class | Uses | Action |
|---|---|---|
| `SC/product/ProductUseCaseImpl.kt` | `suspend fun findAll(data: ProductsWhereInput)` (generated) plus `data.vertical!!.fromDto()` (`DtoEnumMapper`) | E: new signature `suspend fun findAll(idEq: ProductId?, idIn: List<ProductId>?, vertical: Vertical?): List<Product>` with the body `idEq?.let { listOf(productRepository.findById(it)!!) } ?: idIn?.let { productRepository.findAllByIdIn(it) } ?: vertical?.let { productRepository.findAllByPartOfOrPartOfIsNullAndAvailableTrue(it) } ?: throw IllegalArgumentException("Insufficient filter params")`; drop both imports. `SC/product/ProductResolver.kt:39` becomes `productService.findAll(data.id?.eq?.let { ProductId(it) }, data.id?.`in`?.map { ProductId(it) }, data.vertical?.fromDto())`. No test calls `findAll`. |
| `SC/repository/RepositoryUseCase.kt` | `createDocumentUrl` (line 423), `createAttachmentUrl` (line 416) | Solved by moving both functions to domain (above); no edit |
| `SC/agent/AgentService.kt` | `ScrapeResponseInput.fromDto()` (line 129), `Source.toDto()` (line 157) | **None.** AgentService stays in server-core, behind the `AgentDirectory` port in step 7, and server-core may import graphql-api |
| `SC/pipeline/plugins/FeedsPlugin.kt` | `isHtml`, `RemoteNativeFeedRef.toDto()`, `GenericFeedRule.toDto()` | **None.** Plugins stay in server-core |

### 0.5 Mother merge (one `object Mother` in `org.migor.feedless`)

`packages/domain/src/testFixtures/kotlin/org/migor/feedless/Mother.kt` holds:

- from server-core: `randomUserId()`, `randomGroupId()`, `randomDocumentId()`, `randomRepositoryId()`, `randomSourceId()`, `randomConnectedAppId()`; `randomUser()`, the **deterministic** one (`email = uuid@localhost`, `lastLogin = now`, `hasAcceptedTerms = true`, everything else defaulted); `randomOneTimePassword(user: User)` (password `"1234"`, `validUntil = now`, `attemptsLeft = 1`).
- from domain: `randomInt`, `randomNullableUUID`, `randomNullableRepositoryId`, `randomOrderID`, `randomUserID`, `randomLocalDateTime`, `randomNullableLocalDateTime`, `randomBoolean`, `randomString`, `randomNullableString`, `randomOneTimePassword(userId: UserId? = null)`; domain's all-random `randomUser()` becomes `randomUserWithRandomFields()`.

Callers keep their semantics:

- server-core tests (`SecurityConfigSsoTokenTest`, `MailAuthenticationServiceTest`, `StatefulAuthServiceTest`, where `nonRootUser` relies on `admin = false`) keep the deterministic `randomUser()` and `randomOneTimePassword(user)`.
- mail-adapter (`MailServiceWith{JavaMail,Mailgun}IntTest`) calls `randomUser().copy(email = …)`, which only needs a valid `User`, and `randomOneTimePassword(userId = user.id)`. The named argument resolves to the `UserId?` overload, and the two overloads do not clash.
- stripe-payments uses `randomOrderID`/`randomUserID`.
- `randomUserId` and `randomUserID` both stay; the names differ only in case, which is legal.

`packages/domain/src/testFixtures/kotlin/org/migor/feedless/MockitoHelpers.kt` (package `org.migor.feedless`, so importers such as `import org.migor.feedless.any2` keep working in the ~44 server-core tests) holds `anyList`, `any(type)`, `any2`, `anyOrNull2`, `argThat`, `anyOrNull(type)` and `eq`.

### 0.6 Persistence test infrastructure (jpa-data), designed here and used from step 1 on

Findings: all 11 PostgreSQL IntTests are full `@SpringBootTest` with `@ExtendWith(PostgreSQLExtension)`, profiles `"test","database",…,AppLayer.repository`, and `@MockitoBean`s. None is a `@DataJpaTest`. jpa-data has no `src/main/resources` and no `src/test/resources`. Flyway migrations (91 files) live in `packages/server-core/src/main/resources/db/migration`. Server-core's `DatabaseConfig` provides `@EnableJpaRepositories(basePackages=["org.migor.feedless"], includeFilters=[ASSIGNABLE_TYPE JpaRepository])`, `@EnableJpaAuditing` and `@EnableScheduling`, because `FeedlessApplication` excludes `JpaRepositoriesAutoConfiguration`. `CustomSQLDialect`/`SqlFunctionsMetadataBuilderContributor` are referenced only in comments, so they are not needed. No jpa-data entity uses server-core's `EncryptionConverter`/`HashConverter` (one commented reference).

Changes:

1. `git mv packages/server-core/src/main/resources/db/migration packages/jpa-data/src/main/resources/db/migration` (91 files). Content is unchanged, so checksums are unchanged. Flyway scans `classpath:db/migration` across jars, and bootJar contains jpa-data under `BOOT-INF/lib`. `application-database.yaml` (with `spring.flyway.target`) stays in server-core.
2. `SCT/config/FlywayTargetTest.kt`: `migrationDirectory()` must resolve `../jpa-data/src/main/resources/db/migration` from the module root and `packages/jpa-data/src/main/resources/db/migration` from the repo root. Split `resolveServerCoreFile` into a general `resolveFile(moduleRelative, repoRelative)`. The test stays in server-core because it cross-checks server-core's yaml.
3. New `packages/jpa-data/src/test/kotlin/org/migor/feedless/data/jpa/JpaDataTestApplication.kt`:

```kotlin
package org.migor.feedless.data.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// Mirrors server-core's FeedlessApplication + DatabaseConfig for the persistence layer only.
@SpringBootApplication(scanBasePackages = ["org.migor.feedless.data.jpa"], exclude = [JpaRepositoriesAutoConfiguration::class])
@EntityScan(basePackages = ["org.migor.feedless.data.jpa"])
@EnableJpaRepositories(
  basePackages = ["org.migor.feedless.data.jpa"],
  includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JpaRepository::class])],
)
@EnableJpaAuditing
class JpaDataTestApplication
```

4. New `packages/jpa-data/src/test/resources/`: copy `application-test.properties`, `application-database.properties`, `testcontainers.properties` and `logback-test.xml` from `packages/server-core/src/test/resources/`. `PostgreSQLExtension` sets `spring.flyway.enabled=true` as a system property, which overrides the `false` in `application-test.properties`.
5. jpa-data's `*JpaRepository` adapters are `@Profile("${AppProfiles.x} & ${AppLayer.repository}")`, so moved tests keep their `@ActiveProfiles`.

### 0.7 `@Throttled`

Change `import org.migor.feedless.api.throttle.Throttled` to `import org.migor.feedless.throttle.Throttled` in these 17 files: `SC/pipeline/PluginResolver.kt`, `SC/repository/RepositoryResolver.kt`, `SC/mail/MailAuthResolver.kt`, `SC/secrets/SecretsResolver.kt`, `SC/attachment/AttachmentResolver.kt`, `SC/annotation/AnnotationResolver.kt`, `SC/user/UserResolver.kt`, `SC/agent/AgentResolver.kt`, `SC/scrape/ScrapeQueryResolver.kt`, `SC/document/DocumentResolver.kt`, `SC/product/ProductResolver.kt`, `SC/feed/FeedResolver.kt`, `SC/feed/FeedController.kt`, `SC/report/ReportResolver.kt`, `SC/feature/FeatureResolver.kt`, `SC/session/SessionResolver.kt`, `SC/session/AuthAnonymousResolver.kt`.

- `git rm packages/server-core/src/main/kotlin/org/migor/feedless/api/throttle/Throttled.kt`.
- `SC/api/throttle/ThrottleAspect.kt`: `@Around("@annotation(org.migor.feedless.throttle.Throttled)")`.
- No test imports the old annotation. `SCT/api/throttle/ThrottleAspectIntTest.kt` is only in that package; check with `grep -n Throttled` and switch it if it uses the annotation by FQN.

### 0.8 Security bridge extras

- New `packages/domain/src/main/kotlin/org/migor/feedless/session/JwtCapabilities.kt` (package `session`) holds three things: `fun Jwt.capabilities(): List<LazyGrantedAuthority>`, cut from `SC/session/JwtRequestFilter.kt:82-95` (it uses gson's `JsonSyntaxException`); `object JwtParameterNames`, cut from `SC/session/AuthService.kt:12-22`; and `enum class AuthTokenType`, cut from `AuthService.kt:24-29`. AuthTokenType is needed by `UserSecretUseCase` in step 3.
- Users keep the same imports because the package is the same: `IpThrottleService`, `TokenAuthenticator`, `JwtTokenIssuer`, `HttpApiJwtFilter`, `UserSecretUseCase`, `SCT/session/JwtTokenIssuerTest.kt`.
- `SessionService.kt` needs `OAuth2AuthenticationToken` (spring-security-oauth2-client) and `Jwt` (spring-security-oauth2-jose). Domain therefore needs both libraries on top of spring-security-core, which the spec did not list.

### 0.9 graphql-api auto-configuration

`FeedlessApplication` (`@SpringBootApplication` in `org.migor.feedless`) already scans every `org.migor.feedless.*` class on the classpath, including graphql-api's, so the auto-configuration is only a guarantee for other consumers. Scanning the same class twice is harmless: `ClassPathBeanDefinitionScanner.checkCandidate` skips compatible duplicates, which is exactly why `HttpApiAutoConfiguration` also works. The resolvers live in feature packages, so scan with an annotation filter:

```kotlin
// packages/graphql-api/src/main/kotlin/org/migor/feedless/graphql/GraphqlApiAutoConfiguration.kt
package org.migor.feedless.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataLoader
import com.netflix.graphql.dgs.DgsDirective
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@AutoConfiguration
@ComponentScan(
  basePackages = ["org.migor.feedless"],
  useDefaultFilters = false,
  includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [DgsComponent::class, DgsDataLoader::class, DgsDirective::class])],
)
@ComponentScan(basePackages = ["org.migor.feedless.api.mapper", "org.migor.feedless.api.graphql"])
@ComponentScan(
  basePackages = ["org.migor.feedless.api", "org.migor.feedless.config"],
  useDefaultFilters = false,
  includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [org.migor.feedless.api.DtoMapperFacade::class, org.migor.feedless.config.GraphqlConfig::class])],
)
class GraphqlApiAutoConfiguration
```

`packages/graphql-api/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:

```
org.migor.feedless.graphql.GraphqlApiAutoConfiguration
```

The third scan uses ASSIGNABLE_TYPE rather than `@Import`, so the bean names match the ones the app's own scan produces (`dtoMapperFacade`, `graphqlConfig`) and no second definition appears under an FQN name. `@Profile(AppLayer.api)` on `GraphqlConfig` and the directives still applies.

### 0.10 Build changes

`packages/domain/build.gradle.kts` (full replacement):

```kotlin
plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring) // opens @Service/@Transactional/@Cacheable classes for CGLIB proxies
  kotlin("plugin.serialization") version "1.9.0"
  `java-test-fixtures`
}

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

dependencies {
  api(platform(libs.spring.boot.bom)) // api: consumers resolve the versionless Spring artifacts below
  testFixturesImplementation(platform(libs.spring.boot.bom))

  implementation(kotlin("stdlib"))
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  api(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
  api(libs.gson)                        // @SerializedName on moved Json*/plugin params; JsonSyntaxException
  api(libs.hibernate.spatial)           // JTS Point in domain types (was implementation)
  api(libs.jsoup)                       // ArticleContext, HtmlUtil
  api("org.springframework:spring-context")
  api("org.springframework:spring-tx")
  api("org.springframework.security:spring-security-core")
  api("org.springframework.security:spring-security-oauth2-client") // OAuth2AuthenticationToken (bridge)
  api("org.springframework.security:spring-security-oauth2-jose")   // Jwt (bridge, Jwt.capabilities)
  api("io.micrometer:micrometer-core")
  api("com.fasterxml.jackson.core:jackson-annotations") // @JsonIgnoreProperties on GenericFeed*
  implementation("org.slf4j:slf4j-api")                  // RequestContext uses MDC today

  testFixturesImplementation("org.mockito:mockito-core")

  testImplementation(kotlin("test"))
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.spring.boot.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test { useJUnitPlatform() }
```

Delete the `configurations { create("testOutput") }`, `testJar` and `artifacts {}` blocks.

`packages/mail-adapter/build.gradle.kts` and `packages/stripe-payments/build.gradle.kts`: replace `testImplementation(project(":packages:domain", "testOutput"))` with `testImplementation(testFixtures(project(":packages:domain")))`.

`packages/graphql-api/build.gradle.kts` (the codegen block stays as is):

```kotlin
plugins {
  alias(libs.plugins.dgs.codegen)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kapt)
}

kotlin { jvmToolchain(21) }

dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(platform(libs.dgs.platform))
  implementation(project(":packages:domain"))
  implementation(libs.spring.boot.web)          // MultipartFile typeMapping (already there)
  implementation(libs.spring.boot.security)
  implementation(libs.dgs.starter)              // DgsComponent, DgsCustomContextBuilder, TypedGraphQLError, graphql-java
  implementation(libs.kotlin.reflect)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.reactor)
  implementation(libs.commons.lang3)            // BooleanUtils, StringUtils in mappers
  implementation(libs.xsoup)                    // XPathValidatorDirective
  implementation("commons-validator:commons-validator:1.9.0") // EmailValidatorDirective; today transitive in server-core, pin to `./gradlew :packages:server-core:dependencyInsight --dependency commons-validator`
  implementation("org.mapstruct:mapstruct:1.6.3")
  kapt("org.mapstruct:mapstruct-processor:1.6.3")

  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation(libs.dgs.codegen.test)
}

// kapt stubs must see the DGS-generated types
tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask> { dependsOn("generateJava") }

tasks.test { useJUnitPlatform() }
```

`packages/jpa-data/build.gradle.kts` additions:

```kotlin
plugins { `java-test-fixtures` } // add to the existing block

kotlin { jvmToolchain(21) }

dependencies {
  testFixturesImplementation(platform(libs.spring.boot.bom))
  testFixturesApi("org.junit.jupiter:junit-jupiter-api")
  testFixturesImplementation(libs.testcontainers.core)
  testFixturesImplementation(libs.testcontainers.postgresql) // PostgisContainerProvider
  testFixturesImplementation(libs.testcontainers.junit)
  testFixturesImplementation("org.slf4j:slf4j-api")

  testImplementation(testFixtures(project(":packages:domain")))
  testImplementation(libs.spring.boot.test)
  testImplementation(libs.testcontainers.core)
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.junit)
  testImplementation(libs.flyway.core)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}
```

`packages/server-core/build.gradle.kts` additions:

```kotlin
testImplementation(testFixtures(project(":packages:domain")))
testImplementation(testFixtures(project(":packages:jpa-data")))
```

server-core keeps kapt and MapStruct because `order/OrderMapper`, `product/ProductMapper` and `product/PricedProductMapper` remain until step 2.

http-api needs nothing in step 0. From step 1 on it needs `testImplementation(testFixtures(project(":packages:domain")))`.

### 0.11 Verification

```bash
trash packages/*/build/test-results 2>/dev/null
./gradlew :packages:domain:build :packages:feed-parser:test :packages:jpa-data:test :packages:graphql-api:test :packages:http-api:test :packages:mail-adapter:test :packages:stripe-payments:test :packages:server-core:test --continue
grep -rn 'org.migor.feedless.api.throttle.Throttled' packages --include='*.kt'                                  # expect nothing
grep -rln 'import org.migor.feedless.data.jpa\|import org.migor.feedless.common.PropertyService' packages/graphql-api/src packages/domain/src   # expect nothing
ls packages/graphql-api/build/generated/source/kapt/main/org/migor/feedless/api/mapper/                         # DocumentMapperImpl, EnumMapperImpl, ScrapeResponseMapperImpl, UserSecretMapperImpl
```

Then run the Baseline python one-liner; TOTAL `executed` must be at least the baseline. RepositoryCommandMapperTest and ExceptionsTest now count under graphql-api and domain.

### 0.12 Risks and gotchas

- **Stale JUnit XML** double-counts moved tests unless `build/test-results` is cleaned first.
- **MapStruct impl package** equals the mapper's package (`org.migor.feedless.api.mapper`); the generated `@Component` names (`documentMapperImpl`, …) are unchanged. jpa-data's mappers declare no `componentModel`, so the matching simple names (`DocumentMapperImpl` in `data.jpa.document`) do not become colliding beans.
- **`DtoMapperFacade` static instance.** Extension functions such as `Document.toDto` throw `lateinit` errors unless the facade bean was created. graphql-api unit tests must construct `DtoMapperFacade(Mappers.getMapper(...)…)` (now without `featureMapper`) or call the mapper directly, as `DocumentResolverTest` does.
- **kapt in graphql-api** needs `generateJava` before `kaptGenerateStubsKotlin`, or the stubs fail on missing `org.migor.feedless.generated.*`. Build time grows.
- **Bean `capabilityService`** now comes from domain. Any context that scans `org.migor.feedless` gets it without a profile, as before. http-api's test app scans only `org.migor.feedless.http`, so it is unchanged.
- **`internal` in `LazyGrantedAuthority`** must become public, or server-core stops compiling.
- **`api(platform(spring-boot-bom))` in domain** now reaches every module. server-core also uses the dependency-management plugin and the DGS platform. The versions match today (3.4.11), so re-run `./gradlew :packages:server-core:dependencies` if anything drifts.
- **Duplicate FQNs** (`common.HttpResponse`, `util.FeedUtil`, `util.HtmlUtil`) disappear only once the server-core copies are deleted. Leaving them produces two classes with the same FQN on the runtime classpath.
- **Profiles**: moved beans keep their `@Profile`. `SecurityContextCapabilityService` and `GraphQLExceptionHandler` have none, as before.
- **Circular modules**: domain must not reference feed-parser, jpa-data, graphql-api or server-core. `./gradlew :packages:domain:compileKotlin` alone catches this.
- **Flyway move**: after `git mv`, check that `bootJar` still contains the migrations (`unzip -l packages/server-core/build/libs/app.jar | grep jpa-data`, then list that jar) and that the Docker image boots. Update AGENTS.md rule 5's path in step 8.

## Steps 1–4 (plan · feature/product/order · user/group/auth/secrets · session/mail-auth)

Assumptions carried from step 0 (named where a move depends on them): server-core `Exceptions.kt` merged into domain (`BadRequestException`, `UnavailableException`); `@Throttled` is `org.migor.feedless.throttle.Throttled`; the security bridge is in domain `org.migor.feedless.session`; `api/DtoMapperExtensions.kt` (with `UserSecretMapper`, `DocumentMapper`, `ScrapeResponseMapper` in its closure), `api/DtoEnumMapper.kt`, `api/mapper/EnumMapper.kt`, `MapperUtil`, `MapStructConfig` are in graphql-api; server-core `Mother.kt` (including the top-level `any`/`any2`/`anyList`/`argThat`/`eq`/`anyOrNull*` helpers) is merged into the domain testFixture; domain, graphql-api and http-api tests have `libs.spring.boot.test`, `libs.kotlinx.coroutines.test`, `org.mockito.kotlin:mockito-kotlin:5.4.0`; **domain has the `kotlin.spring` (all-open) plugin** (otherwise `@Transactional FeatureService` and `@Service` beans are final and cannot be CGLIB-proxied). Anything else is listed per step.

Findings that change the spec's step boundaries (details in each step):

- The coroutine-context extensions `corrId()`, `userId()`, `userIdMaybe()`, `groupId()` and `isAdmin()` are declared at the bottom of `user/UserUseCase.kt` (step 3), but `PlanUseCase` (step 1), `OrderUseCaseImpl`, `FeatureService` and `ProductUseCaseImpl` (step 2) use them. **Step 1 extracts them into domain.**
- `UserGuard` is listed under step 3, but `OrderUseCaseImpl` and `OrderGuard` (step 2) inject it. **`UserGuard` moves in step 2.**
- `OrderResolver` is listed under step 2, but it injects `UserUseCase` (step 3), and graphql-api cannot see server-core. **`OrderResolver` moves in step 3.**
- `PlanResolver` (step 1) calls `Product.toDto()` from `api/mapper/ProductMapper.kt`. **That mapper moves in step 1.**
- Dead duplicates, to delete rather than move: `product/ProductMapper.kt`, `product/PricedProductMapper.kt` and `order/OrderMapper.kt` in server-core. They have no callers; jpa-data has its own mappers, which its entities use. `api/mapper/FeatureMapper.kt` and the `FeatureGroupEntity`/`FeatureValueEntity` functions in `DtoMapperFacade` are also dead, and they drag jpa-data into graphql-api. Delete them in step 0; if step 0 kept them, delete them in step 2.
- The "session cookie" port that the spec wants for `CookieProvider` already exists as `SessionTokenPort` (`toCookie(AuthToken): HttpSetCookie`, `createExpiredTokenCookie(name)`), which `SessionTokenPortAdapter` implements. Steps 3 and 4 need no `SessionCookies` port. An optional split is shown in step 4.
- `UserUseCase` needs Telegram, which the spec puts in step 5, so `message.Notifications` is introduced in step 3 with only the Telegram methods.

---

### Step 1 — plan

#### 1. Moves (`git mv`)

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/plan/PlanUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/plan/PlanUseCase.kt` | drop `: PlanUseCasePort` and the `override` modifiers |
| `packages/server-core/src/main/kotlin/org/migor/feedless/plan/PlanGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/plan/PlanGuard.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/plan/PlanConstraintsService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/plan/PlanConstraintsService.kt` | none (`Environment`, `Profiles` and `CronExpression` come from spring-context/spring-core) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/plan/PlanResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/plan/PlanResolver.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/mapper/ProductMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/ProductMapper.kt` | none (needs `api.toDto` for `Vertical`, which step 0 put in graphql-api) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/plan/PlanUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/plan/PlanUseCaseTest.kt` | none |
| `packages/server-core/src/test/kotlin/org/migor/feedless/plan/PlanConstraintsServiceImplTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/plan/PlanConstraintsServiceImplTest.kt` | none (uses `MockitoExtension`) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/plan/PlanResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/plan/PlanResolverTest.kt` | none (tests the `internal` `Plan.toDto`, so it must live in graphql-api) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/user/CoroutineContextGroupIdTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/user/CoroutineContextGroupIdTest.kt` | none |

New file, cut from the bottom of `packages/server-core/src/main/kotlin/org/migor/feedless/user/UserUseCase.kt`: `packages/domain/src/main/kotlin/org/migor/feedless/user/CoroutineContextExtensions.kt`. It holds `fun CoroutineContext.corrId(): String?`, `userId(): UserId`, `userIdMaybe(): UserId?`, `groupId(): GroupId` and `isAdmin(): Boolean`, unchanged, and needs only `RequestContext` and `NoActingGroupException` (both in domain). The package stays `org.migor.feedless.user`, so no import changes anywhere; the 22 server-core files that import them keep compiling.

Counts: main 5 moves + 1 extracted file; test 4.

#### 2. Ports

None.

#### 3. `*Port` removed

Remove `packages/domain/src/main/kotlin/org/migor/feedless/plan/PlanUseCasePort.kt` (`git rm`). Consumers:

- `packages/http-api/src/main/kotlin/org/migor/feedless/http/PlanHttpController.kt`: the constructor parameter becomes `private val planUseCase: PlanUseCase`; the import becomes `org.migor.feedless.plan.PlanUseCase`.
- `packages/http-api/src/test/kotlin/org/migor/feedless/http/PlanHttpControllerTest.kt`: line 12 import; line 40 `@MockitoBean private lateinit var planUseCase: PlanUseCase`.

#### 4. Edits outside moves

`PlanResolver` keeps `@Autowired lateinit var planUseCase: PlanUseCase`; it already uses the concrete class.

#### 5. Build

- domain: `testImplementation(libs.spring.boot.test)` and `testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")`, if step 0 did not add them. `PlanConstraintsServiceImplTest` needs `mockito-junit-jupiter`, which `spring-boot-starter-test` brings.
- graphql-api: nothing beyond step 0.

#### 6. Verification

```bash
./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test
for m in domain graphql-api http-api jpa-data server-core; do printf '%s ' $m; cat packages/$m/build/test-results/test/*.xml 2>/dev/null | grep -o '<testsuite [^>]*' | grep -o ' tests="[0-9]*"' | grep -o '[0-9]*' | paste -sd+ - | bc; done
```

Expected shift: 3 + 22 + 2 declared tests leave server-core for domain (`PlanUseCaseTest` 2, `PlanConstraintsServiceImplTest` 22, `CoroutineContextGroupIdTest` 3), and 1 goes to graphql-api (`PlanResolverTest`).

#### 7. Risks

- `PlanGuard` is profiled `plan & repository` (the layer is inconsistent, left out of scope). Nothing injects it; it compiles as is.
- `PlanHttpController` and `PlanResolver` use `@PreAuthorize("@capabilityService.hasCapability('user')")`. The `capabilityService` bean is server-core's `SecurityContextCapabilityService`, so the http-api test context must still provide it; it already does for the other controllers.
- `PlanConstraintsService` is mocked by type in about 10 server-core tests. The FQN is unchanged, so they keep compiling.

---

### Step 2 — feature, product, order

#### 1. Moves

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/feature/FeatureService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feature/FeatureService.kt` | **yes**: removes the generated types (below) and `mapFeatureName2Dto` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/feature/FeatureResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/feature/FeatureResolver.kt` | **yes**: maps inputs, and receives `mapFeatureName2Dto` (unused today; keep it or drop it) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/product/ProductUseCaseImpl.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/product/ProductUseCaseImpl.kt` | **yes**: `findAll` signature |
| `packages/server-core/src/main/kotlin/org/migor/feedless/product/ProductResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/product/ProductResolver.kt` | **yes**: builds `ProductsFilter` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/product/ProductDataLoader.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/product/ProductDataLoader.kt` | **yes**: `ProductRepository` instead of `ProductDAO` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/order/OrderUseCaseImpl.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/order/OrderUseCaseImpl.kt` | none (needs commons-lang3 in domain) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/order/OrderGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/order/OrderGuard.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/user/UserGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/user/UserGuard.kt` | none (pulled forward from step 3) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/payment/PaymentController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/payment/PaymentController.kt` | **yes**: `propertyService: PropertyService` becomes `appConfig: AppConfig` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/plan/ProductServiceTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/plan/ProductServiceTest.kt` | none (the constructor is unchanged) |

Delete with `git rm` (dead code, zero callers):

- `packages/server-core/src/main/kotlin/org/migor/feedless/product/ProductMapper.kt`
- `packages/server-core/src/main/kotlin/org/migor/feedless/product/PricedProductMapper.kt`
- `packages/server-core/src/main/kotlin/org/migor/feedless/order/OrderMapper.kt`
- `packages/server-core/src/main/kotlin/org/migor/feedless/api/mapper/FeatureMapper.kt`, if step 0 did not already delete it, together with `DtoMapperFacade.toDto(FeatureGroupEntity…)`, `toDto(FeatureValueEntity)`, `FeatureGroupEntity.toDto()` and `FeatureValueEntity.toDto()`.

`OrderResolver` and its tests are deferred to step 3.

Tests that stay in server-core:

- `plan/FeatureServiceIntTest.kt`: it autowires the `FeatureService` use case plus `FeatureGroupDAO`, and mocks server-core's `StatelessAuthService`, so it is not persistence-only.
- `data/jpa/SeederTest.kt`: `Seeder` stays in server-core.
- `user/UserGuardTest.kt`: it builds `RepositoryGuard`, which moves in step 5; move this test in step 5.

Counts: main 9 moves + 3–4 deletions; test 1.

`FeatureService` edits:

```kotlin
// was: updateFeatureValue(id, intValue: FeatureIntValueInput?, boolValue: FeatureBooleanValueInput?, productId)
suspend fun updateFeatureValue(id: FeatureValueId, intValue: Long?, boolValue: Boolean?, productId: ProductId? = null)
//   body: valueBoolean = boolValue!!   /   valueInt = intValue!!
// was: findAllGroups(inherit: Boolean, where: FeatureGroupWhereInput)
suspend fun findAllGroups(inherit: Boolean, id: FeatureGroupId?): List<FeatureGroup>
//   body: if (id == null) featureGroupRepository.findAll() else listOf(featureGroupRepository.findById(id).orElseThrow())
```

`mapFeatureName2Dto` (it uses the generated `FeatureName` as `FeatureNameDto`) moves to `packages/graphql-api/src/main/kotlin/org/migor/feedless/feature/FeatureResolver.kt`, below the resolver.

`FeatureResolver` mapping:

- `featureService.findAllGroups(inherit, where.id?.eq?.let { FeatureGroupId(it) })`
- `featureService.updateFeatureValue(FeatureValueId(data.id), data.value.numVal?.value, data.value.boolVal?.value)`

Check that `FeatureIntValueInput.value` (`Long!` in the schema) matches `FeatureValue.valueInt`.

`ProductUseCaseImpl` edits: remove the imports of `org.migor.feedless.generated.types.ProductsWhereInput` and `org.migor.feedless.api.fromDto`. Add a new domain type, `packages/domain/src/main/kotlin/org/migor/feedless/product/ProductsFilter.kt`:

```kotlin
package org.migor.feedless.product
data class ProductsFilter(val id: ProductId? = null, val ids: List<ProductId>? = null, val vertical: Vertical? = null)
```

```kotlin
suspend fun findAll(filter: ProductsFilter): List<Product> = withContext(Dispatchers.IO) {
  filter.id?.let { listOf(productRepository.findById(it)!!) }
    ?: filter.ids?.let { productRepository.findAllByIdIn(it) }
    ?: filter.vertical?.let { productRepository.findAllByPartOfOrPartOfIsNullAndAvailableTrue(it) }
    ?: throw IllegalArgumentException("Insufficient filter params")
}
```

In `ProductResolver`:

```kotlin
productService.findAll(ProductsFilter(data.id?.eq?.let { ProductId(it) }, data.id?.`in`?.map { ProductId(it) }, data.vertical?.fromDto()))
```

`ProductResolver` still injects the concrete `ProductUseCaseImpl`, because `findAll` is not on the `ProductUseCase` interface. Keep it that way.

`ProductDataLoader`: no new repository method is needed, because `ProductRepository.findAllByIdIn(ids: List<ProductId>): List<Product>` already exists and is implemented by `packages/jpa-data/src/main/kotlin/org/migor/feedless/data/jpa/product/ProductJpaRepository.kt` via `productDAO.findAllByIdIn(uuids)`. So jpa-data needs no change. The loader body becomes:

```kotlin
@Autowired lateinit var productRepository: ProductRepository
override fun load(ids: MutableSet<ProductId>) = CompletableFuture.supplyAsync {
  productRepository.findAllByIdIn(ids.distinct()).map { it.toDto() }
    .fold(mutableMapOf<ProductId, ProductDto>()) { acc, item -> acc[ProductId(item.id)] = item; acc }
}
```

Drop the imports of `org.migor.feedless.data.jpa.product.ProductDAO` and `...toDomain`. The loader profile `plan & api` and the `ProductDAO`/`ProductJpaRepository` profile `plan & repository` are as before.

#### 2. Ports

`packages/domain/src/main/kotlin/org/migor/feedless/common/AppConfig.kt`, implemented by `PropertyService`, 1 member in this step (later steps add `apiGatewayUrl` and others):

```kotlin
package org.migor.feedless.common
interface AppConfig {
  val appHost: String
}
```

In `packages/server-core/src/main/kotlin/org/migor/feedless/common/PropertyService.kt`: `class PropertyService : AppConfig`, and `lateinit var appHost: String` becomes `override lateinit var appHost: String`. A `lateinit var` may override an interface `val`, and `@ConfigurationProperties` binding is unaffected. `PaymentController` injects `AppConfig`. `PaymentUseCase` is already in domain; `stripe-payments` implements it.

#### 3. `*Port` removed

None. `OrderUseCase` and `ProductUseCase` are domain interfaces with one implementation; the spec leaves them alone.

#### 4. Edits outside moves

- Every server-core reference to `FeatureService.updateFeatureValue` or `findAllGroups` is in `FeatureResolver`. `Seeder` calls only `assignFeatureValues`, which is unchanged.
- `FeatureService` users that stay in server-core compile against the domain class: `Seeder`, `FeedService`, `MailAuthenticationService` and `UserUseCase` (until step 3 or 4), plus 11 tests.

#### 5. Build

domain:

```kotlin
implementation(libs.commons.lang3) // OrderUseCaseImpl (BooleanUtils); UserUseCase (StringUtils) in step 3
```

http-api: nothing new (`PaymentController` uses `spring-web`).

#### 6. Verification

The step-1 commands, plus `./gradlew :packages:server-core:bootJar` to catch bean wiring.

Expected shift: `ProductServiceTest` (1) leaves server-core for domain.

#### 7. Risks

- `ProductResolver` is profiled `plan & repository` (the layer is inconsistent, left out of scope). It is picked up at runtime by `FeedlessApplication`'s scan, not by graphql-api's auto-configuration scan, unless that scan covers `org.migor.feedless` and not just a `graphql` sub-package.
- The same applies to `PaymentController` (package `org.migor.feedless.payment`): `HttpApiAutoConfiguration` scans only `org.migor.feedless.http`, so the controller is found only by the application scan. That is fine in production, but it is invisible to `HttpApiTestApplication`.
- `@DgsDataLoader(name = "product")` must be registered by DGS. `OrderResolver` still calls `dfe.getDataLoader("product")` in server-core until step 3, which is fine at runtime.
- `FeatureService.assignFeatureValues` is `@Transactional(propagation = MANDATORY)`. Without all-open in domain, the class is final and the proxy fails at context start.
- There is no MapStruct bean clash: the deleted server-core mappers used `Mappers.getMapper` (not spring beans).

---

### Step 3 — user, group, auth, secrets

#### 1. Moves

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/group/GroupGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/group/GroupGuard.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/group/GroupUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/group/GroupUseCase.kt` | drop `: GroupUseCasePort` and the `override` modifiers (`TransactionTemplate` comes from spring-tx) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/auth/AuthUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/auth/AuthUseCase.kt` | **yes**: `groupUseCasePort: GroupUseCasePort` becomes `groupUseCase: GroupUseCase`; drop `: AuthUseCasePort` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/user/UserUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/user/UserUseCase.kt` | **yes**: `UpdateCurrentUserInput` becomes `UserUpdate`; `TelegramBotService` becomes `Notifications` (the extensions already left in step 1) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/user/ConnectedAppUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/user/ConnectedAppUseCase.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/secrets/UserSecretUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/secrets/UserSecretUseCase.kt` | **yes**: `JwtTokenIssuer` becomes `TokenIssuer` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/session/ActingGroup.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/session/ActingGroup.kt` | none (pure functions on `UserGroupAssignmentRepository`; `SecurityConfig`, `TestingEndpoint`, `JwtTokenIssuer`, `StatefulAuthService` and `TokenAuthenticator` keep the same FQN) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/AppMetrics.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/AppMetrics.kt` | none (constants only; `UserUseCase` uses `AppMetrics.userSignup`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/user/UserResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/user/UserResolver.kt` | **yes**: builds `UserUpdate` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/group/GroupResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/group/GroupResolver.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/secrets/SecretsResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/secrets/SecretsResolver.kt` | none (`UserSecret.toDto()` is from step 0's `DtoMapperExtensions`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/order/OrderResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/order/OrderResolver.kt` | none (deferred from step 2; needs `UserUseCase`, `LicenseRepository`, `api.toDTO`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/mapper/UserMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/mapper/UserMapper.kt` | none (`StringUtils`) |
| `packages/domain/src/main/kotlin/org/migor/feedless/auth/AuthUseCasePort.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/auth/AuthenticatedUser.kt` | **yes**: keep only `data class AuthenticatedUser` and delete the interface |
| `packages/server-core/src/test/kotlin/org/migor/feedless/group/GroupUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/group/GroupUseCaseTest.kt` | none |
| `packages/server-core/src/test/kotlin/org/migor/feedless/auth/AuthUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/auth/AuthUseCaseTest.kt` | **yes**: `mock(GroupUseCasePort::class.java)` becomes `mock(GroupUseCase::class.java)` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/user/UserUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/user/UserUseCaseTest.kt` | **yes**: generated `UpdateCurrentUserInput`/`StringUpdateOperationsInput`/`BoolUpdateOperationsInput`/`NullableUpdateOperationsInput` become `UserUpdate`; `mock(TelegramBotService)` becomes `mock(Notifications)` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/secrets/UserSecretUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/secrets/UserSecretUseCaseTest.kt` | **yes**: `mock(JwtTokenIssuer)` becomes `mock(TokenIssuer)`; `Jwt.tokenValue` stubs become `AuthToken(...)` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/session/ActingGroupTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/session/ActingGroupTest.kt` | none |
| `packages/server-core/src/test/kotlin/org/migor/feedless/user/UserResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/user/UserResolverTest.kt` | none (an empty test) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/order/OrderResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/order/OrderResolverTest.kt` | none (tests the `internal` `Order.toDto`) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/plan/OrderResolverIntTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/plan/OrderResolverIntTest.kt` | none (`@Disabled` placeholder; it counts as "skipped" in JUnit XML) |

Delete `packages/domain/src/main/kotlin/org/migor/feedless/group/GroupUseCasePort.kt` (`git rm`).

`AuthTokenType` is split out of `packages/server-core/src/main/kotlin/org/migor/feedless/session/AuthService.kt` into the new domain file `packages/domain/src/main/kotlin/org/migor/feedless/session/AuthTokenType.kt`, moved verbatim (`enum class AuthTokenType(val value: String)`). `JwtParameterNames` stays in server-core. The package is the same, so no imports change.

New domain file, `packages/domain/src/main/kotlin/org/migor/feedless/user/UserUpdate.kt`:

```kotlin
package org.migor.feedless.user
/** null = leave unchanged. */
data class UserUpdate(
  val email: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val country: String? = null,
  val plan: ProductId? = null,
  val acceptedTermsAndServices: Boolean? = null,
  /** true = schedule purge in 30 days, false = unset. */
  val schedulePurge: Boolean? = null,
)
```

`UserUseCase.updateUser(userId: UserId, data: UserUpdate)`: each `data.x?.let { it.set }` becomes `data.x?.let { it }`; `data.plan` is used directly as `ProductId`; `purgeScheduledFor.assignNull` becomes `!schedulePurge`.

In `UserResolver`:

```kotlin
UserUpdate(
  data.email?.set, data.firstName?.set, data.lastName?.set, data.country?.set,
  data.plan?.set?.let { ProductId(it) }, data.acceptedTermsAndServices?.set,
  data.purgeScheduledFor?.let { !it.assignNull },
)
```

`dateFormat`, `timeFormat` and `notificationsLastViewedAt` are ignored today and stay ignored.

Tests that stay in server-core:

- `group/GroupUseCaseIntTest.kt`: a full application context (it autowires `GroupUseCase` and `UserUseCase`, and mocks `AgentService`, `PropertyService`, `InboxService` and others). The FQNs are unchanged.
- `session/{CookieProviderTest,JwtTokenIssuerTest,StatefulAuthServiceTest,TokenAuthenticatorTest,JwtClaims}.kt` and `config/*`.
- `user/UserGuardTest.kt`, until step 5.

Counts: main 13 moves + 1 domain rename/split + 1 deletion + 3 new files (`UserUpdate`, `AuthTokenType`, ports); test 8.

#### 2. Ports

`packages/domain/src/main/kotlin/org/migor/feedless/session/TokenIssuer.kt`, implemented by `JwtTokenIssuer`, 2 methods in step 3 and 4 by the end of step 4:

```kotlin
package org.migor.feedless.session
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.user.User
import kotlin.time.Duration

interface TokenIssuer {
  /** API token acting as [user] in [actingGroup]; callers resolve the group with actingGroupOf. */
  fun issueApiToken(user: User, actingGroup: GroupAndRole): AuthToken
  fun getExpiration(authority: AuthTokenType): Duration
}
```

The call sites are `UserSecretUseCase.createUserSecret` (`jwtTokenIssuer.createJwtForApi(user, actingGroupOf(...)).tokenValue` and `jwtTokenIssuer.getExpiration(AuthTokenType.USER)`). The existing methods return `org.springframework.security.oauth2.jwt.Jwt`, which stays out of domain; the port returns the domain `AuthToken(token)`.

In `JwtTokenIssuer`:

```kotlin
class JwtTokenIssuer(...) : TokenIssuer {
  override fun issueApiToken(user: User, actingGroup: GroupAndRole) = AuthToken(createJwtForApi(user, actingGroup).tokenValue)
  override fun getExpiration(authority: AuthTokenType): Duration { ... } // existing
```

Keep every existing `Jwt`-returning method for server-core callers. `UserSecretUseCase` uses `value = token.token`.

`packages/domain/src/main/kotlin/org/migor/feedless/message/Notifications.kt`, implemented by `TelegramBotService`, 2 methods (step 5 may extend it):

```kotlin
package org.migor.feedless.message
interface Notifications {
  fun showOptionsForKnownUser(chatId: Long)
  fun sendMessage(chatId: Long, message: String)
}
```

The call sites are in `UserUseCase.updateConnectedApp` and `deleteConnectedApp`. `TelegramBotService` becomes `class TelegramBotService(...) : Notifications`, and its two existing methods gain `override`. In `UserUseCase`, `@Lazy private val telegramBotServiceMaybe: Optional<TelegramBotService>` becomes `@Lazy private val notificationsMaybe: Optional<Notifications>`.

#### 3. `*Port` removed

`AuthUseCasePort` (now only `AuthenticatedUser`, above) and `GroupUseCasePort` are removed. Consumers switch to `AuthUseCase` / `GroupUseCase`:

- main:
  - `packages/http-api/src/main/kotlin/org/migor/feedless/http/AuthHttpController.kt` (`authUseCasePort: AuthUseCasePort` becomes `authUseCase: AuthUseCase`)
  - `packages/http-api/src/main/kotlin/org/migor/feedless/http/GroupHttpController.kt`
  - `packages/http-api/src/main/kotlin/org/migor/feedless/http/RepositoryAccessGuard.kt`
  - `packages/server-core/src/main/kotlin/org/migor/feedless/auth/AuthUseCase.kt` (moved)
- http-api tests:
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/AuthHttpControllerTest.kt` (`@MockitoBean` on `AuthUseCasePort` becomes `AuthUseCase`)
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/GroupHttpControllerTest.kt`
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/HarvestHttpControllerTest.kt`
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/RecordHttpControllerTest.kt`
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/RepositoryHttpControllerTest.kt`
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/SourceHttpControllerTest.kt` (`@MockitoBean` of `GroupUseCasePort`)
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/SourceHttpControllerCreateTest.kt` (`mock<GroupUseCasePort>()`)
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/RepositoryAccessGuardTest.kt` (`GroupUseCasePort = mock()`)
  - `packages/http-api/src/test/kotlin/org/migor/feedless/http/RepositoryAccessFixture.kt` (constructor parameter)
- server-core tests (`@MockitoBean(types = [GroupUseCasePort::class, …])` becomes `GroupUseCase::class`):
  - `packages/server-core/src/test/kotlin/org/migor/feedless/config/SecurityConfigIntTest.kt:61`
  - `packages/server-core/src/test/kotlin/org/migor/feedless/cli/CliInstallScriptControllerIntTest.kt:54`
  - `packages/server-core/src/test/kotlin/org/migor/feedless/cli/CliInstallScriptControllerWithFixtureIntTest.kt:55`
  - `packages/server-core/src/test/kotlin/org/migor/feedless/cli/CliInstallScriptControllerWithUnsafeGatewayUrlIntTest.kt:58`
  - Each also has the matching import line 14 or 18.

The tests that used to mock the interfaces now mock final (or all-open) concrete classes. Mockito 5's default inline mock maker, which comes with spring-boot-starter-test 3.4, handles final classes, and `@MockitoBean` by concrete type replaces the real bean just as the port type did.

#### 4. Edits outside moves

- `packages/server-core/src/main/kotlin/org/migor/feedless/session/JwtTokenIssuer.kt`: `: TokenIssuer` plus the override (above).
- `packages/server-core/src/main/kotlin/org/migor/feedless/transport/TelegramBotService.kt`: `: Notifications` and two `override`s.
- `packages/server-core/src/main/kotlin/org/migor/feedless/session/AuthService.kt`: remove `AuthTokenType`, which is now in domain.

#### 5. Build

- domain: `micrometer-core` (`MeterRegistry`, `Tag`) and `spring-tx` (`TransactionTemplate`), from step 0, plus commons-lang3 from step 2.
- graphql-api: `implementation(libs.commons.lang3)` (`UserMapper`).

#### 6. Verification

The same commands as step 1.

Expected shift, server-core to domain: `GroupUseCaseTest` 14, `AuthUseCaseTest` 3, `UserUseCaseTest` 16, `UserSecretUseCaseTest` 5 and `ActingGroupTest` 5. Server-core to graphql-api: `UserResolverTest` 1, `OrderResolverTest` 1 and `OrderResolverIntTest` 1 (skipped).

#### 7. Risks

- `UserUseCase` is profiled `user & service & repository`; `UserSecretUseCase` is profiled `secrets & service & repository`.
- `@Lazy Optional<Notifications>` resolves only while `TelegramBotService`, which is `@ConditionalOnBean(TelegramConnectionDAO, TelegramProperties)`, is the single implementer. If step 5 makes `MessageService` implement `Notifications`, the `Optional` becomes ambiguous. Give `MessageService` its own port, or split the Telegram part out.
- `@PreAuthorize("@capabilityService…")` on `UserResolver` and `SecretsResolver` resolves the `capabilityService` bean name, which is defined in server-core.
- `OrderResolver` uses `runBlocking { userUseCase.createUser(...) }` and `dfe.getDataLoader("product")`. Both work unchanged in graphql-api.
- `AppMetrics` moving to domain changes no FQN.
- `UserSecretUseCase` uses `kotlin.time.Clock` (`@OptIn(ExperimentalTime)`); the Kotlin 2.2.21 stdlib in domain has it.

---

### Step 4 — session, mail-auth

#### 1. Moves

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/mail/MailAuthenticationService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/mail/MailAuthenticationService.kt` | **yes** (below) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/secrets/OneTimePasswordService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/secrets/OneTimePasswordService.kt` | none (profiled `security & service`; `CleanupExecutor` injects `Optional<OneTimePasswordService>` and keeps the FQN) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/mail/MailAuthResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/mail/MailAuthResolver.kt` | **yes**: maps inputs and sets the cookie |
| `packages/server-core/src/main/kotlin/org/migor/feedless/session/SessionResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/session/SessionResolver.kt` | none (it already uses only `SessionTokenPort` and `CapabilityService`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/session/AuthAnonymousResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/session/AuthAnonymousResolver.kt` | **yes**: `JwtTokenIssuer`/`CookieProvider` become `TokenIssuer` + `SessionTokenPort` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/mail/MailAuthenticationServiceTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/mail/MailAuthenticationServiceTest.kt` | **yes**: new signatures; mocks `TokenIssuer` instead of `JwtTokenIssuer`/`CookieProvider`; no `HttpServletResponse` |

These stay in server-core:

- `SessionTokenPortAdapter`, `CookieProvider`, `JwtTokenIssuer`, `AuthService` and its subclasses, `JwtRequestFilter`, `TokenAuthenticator`.
- `mail/MailAuthResolverIntTest.kt`: a full `@SpringBootTest` with a DGS client. **Edit it**: it `@MockitoBean`s `MailAuthenticationService` and stubs it with the generated `ConfirmCode`/`Authentication`, so the stubs must switch to `OtpChallenge`/`AuthToken` and the new parameter lists.
- `mail/MailPropertiesTest.kt`: reads YAML, unrelated.

New domain file, `packages/domain/src/main/kotlin/org/migor/feedless/mail/OtpChallenge.kt`:

```kotlin
package org.migor.feedless.mail
import org.migor.feedless.otp.OneTimePasswordId
data class OtpChallenge(val length: Int, val otpId: OneTimePasswordId)
```

`MailAuthenticationService` edits. Remove the imports of `generated.types.*`, `jakarta.servlet.http.HttpServletResponse`, `JwtTokenIssuer` and `CookieProvider`; inject `TokenIssuer`.

```kotlin
suspend fun authenticateUsingMail(email: String, allowCreate: Boolean, osInfo: String): OtpChallenge
//   returns OtpChallenge(length = otp.password.length, otpId = otp.id)
suspend fun confirmAuthCode(otpId: OneTimePasswordId, code: String): AuthToken
//   ... val token = tokenIssuer.issueTokenForCapabilities(listOf(UserCapability(otp.userId), GroupCapability(actingGroup)))
//   return token   (the cookie is now set by the resolver)
```

`resolveUserByMail(email, allowCreate)` changes accordingly.

`MailAuthResolver`:

```kotlin
authViaMail: mailAuthenticationService.authenticateUsingMail(data.email, data.allowCreate, data.osInfo)
  .let { ConfirmCode(length = it.length, otpId = it.otpId.uuid.toString()) }
confirmAuthCode: val token = mailAuthenticationService.confirmAuthCode(OneTimePasswordId(data.otpId), data.code)
  resolveHttpResponse(dfe).addCookie(toServletCookie(sessionTokenPort.toCookie(token)))
  Authentication(corrId = "", token = token.token)
```

`MailAuthResolver` newly injects `SessionTokenPort` and copies `SessionResolver.toServletCookie`. Consider hoisting that into a shared graphql-api helper, `packages/graphql-api/src/main/kotlin/org/migor/feedless/session/ServletCookies.kt`, used by `SessionResolver`, `MailAuthResolver` and `AuthAnonymousResolver`.

The resulting cookie is identical: `SessionTokenPortAdapter.toCookie` decodes the same JWT and calls `CookieProvider.createTokenCookie`. The order changes slightly (the cookie is added after the OTP row is deleted, as before).

`AuthAnonymousResolver`:

```kotlin
val token = tokenIssuer.issueAnonymousToken()
addCookie(dfe, toServletCookie(sessionTokenPort.toCookie(token)))
AuthenticationDto(token = token.token, corrId = CryptUtil.newCorrId())
```

Counts: main 5 moves + 1 new domain type; test 1.

#### 2. Ports

`TokenIssuer` gains 2 methods; it has 4 in total:

```kotlin
fun issueAnonymousToken(): AuthToken                                              // JwtTokenIssuer: AuthToken(createJwtForAnonymous().tokenValue)
fun issueTokenForCapabilities(capabilities: List<Capability<out Any>>): AuthToken // AuthToken(createJwtForCapabilities(capabilities).tokenValue)
```

`Capability` is in domain (`capability/Capability.kt`).

Session cookies need **no new port**. The existing `org.migor.feedless.session.SessionTokenPort` (4 methods; `toCookie(AuthToken): HttpSetCookie` and `createExpiredTokenCookie(name): HttpSetCookie` are the cookie capability), implemented by `SessionTokenPortAdapter` over `CookieProvider`, already covers both resolvers. If the spec's `SessionCookies` name is wanted anyway, split those two methods out of it:

```kotlin
package org.migor.feedless.session
interface SessionCookies {
  suspend fun toCookie(token: AuthToken): HttpSetCookie
  fun createExpiredTokenCookie(name: String = "TOKEN"): HttpSetCookie
}
interface SessionTokenPort : SessionCookies { suspend fun authenticateUser(...); suspend fun getCurrentSession(): SessionInfo }
```

`SessionTokenPortAdapter` would implement both. `CookieProvider` itself cannot implement it without a `JwtTokenIssuer` dependency, because it takes a `Jwt` and returns `jakarta.servlet.http.Cookie`.

#### 3. `*Port` removed

None. `SessionTokenPort` is an outbound port and stays.

#### 4. Edits outside moves

- `JwtTokenIssuer`: two more overrides.
- `packages/server-core/src/test/kotlin/org/migor/feedless/mail/MailAuthResolverIntTest.kt`: stub signatures (above).

#### 5. Build

- graphql-api: `jakarta.servlet` and `ServletWebRequest` come from the existing `libs.spring.boot.web`; `DgsWebMvcRequestData` comes from the DGS starter (step 0).
- domain: nothing new. `kotlinx.coroutines.delay` and `kotlin.random` are already available.

#### 6. Verification

The same commands. Also run the auth e2e path (`authViaMail` → `confirmAuthCode` → the `TOKEN` cookie is set, then `authAnonymous`) with `MailAuthResolverIntTest` and `AuthenticationIntTest`, both in server-core.

Expected shift: `MailAuthenticationServiceTest` (3) leaves server-core for domain.

#### 7. Risks

- `MailAuthenticationService` is profiled `mail & session & service`, and `MailAuthResolver` `mail & api`. The resolver now also needs the `SessionTokenPort` bean, which is profiled `session & service`. A profile set with `mail` but without `session` already lacked `MailAuthenticationService`, so nothing new breaks.
- `SessionResolver` and `AuthAnonymousResolver` build a servlet `Cookie`, which keeps graphql-api tied to the servlet stack. That is acceptable, since it already has `spring-boot-starter-web`.
- `@Throttled` on `confirmAuthCode`, `authUser` and `authAnonymous` depends on `ThrottleAspect` (server-core, profiled `throttle && api`), which matches both annotations after step 0.

## Step 5 — repository, source, document, harvest

Abbreviations: `SC=packages/server-core/src`, `DO=packages/domain/src`, `GQ=packages/graphql-api/src`, `HA=packages/http-api/src`, `JD=packages/jpa-data/src`, all followed by `/<main|test|testFixtures>/kotlin/org/migor/feedless/`. Every move is `git mv`, package unchanged.

Assumptions carried from steps 0–4 (verify before starting): server-core `Exceptions.kt` merged into domain; `feed/parser/json/*` + `common/HttpResponse` in domain; `@Throttled` + security bridge in domain; `GraphQLExceptionHandler`, `GraphqlConfig`/`DgsCustomContext` and the shared mappers in graphql-api. `DtoMapperFacade` references `DocumentMapper`, `SourceMapper` (`toSource`) and `ScrapeActionMapper` (`scrapeFlowFromDto`), so step 0 must already have moved those three plus `FeatureMapper`, `ScrapeResponseMapper`, `UserSecretMapper`; `createDocumentUrl`/`isHtml` must have left `DtoMapperExtensions.kt` for domain when `RepositoryUseCase`/`FeedsPlugin` were detached. `ScrapeActionMapper` and `RepositoryMapper` import plugin param classes (`CompareBy`, `ConditionalTag`, `DiffRecordsParams`, `FeedPluginParams`, `FulltextPluginParams`, `ItemFilterParams`), and `FeedPluginParams` wraps `GenericFeedRule`, so step 0 must also have moved those value types (and `GenericFeedRule`/`GenericFeedSelectors`/`ExtendContext` from `scrape/WebToFeedTransformer.kt`) to domain. If step 0 did not, the "5a conditional" rows below do it here. `AppMetrics.kt` is assumed moved in step 3 (UserUseCase uses it); if not, move it here. `common.AppConfig` exists (PropertyService implements it); Mother + `any2/eq/anyList/argThat` helpers are domain testFixtures; PostgreSQLExtension + jpa-data test `@SpringBootApplication` exist.

Recommended split: **5a** ports, value types and helper extraction, everything still in server-core, green; **5b** the moves. 5a is safe to land alone and shrinks 5b to mechanical `git mv` plus import fixes.

### 5a — ports, value types, helpers (no use case moves)

#### Moves (whole files)

| from | to | edits |
|---|---|---|
| `SC/main/.../pipeline/Plugin.kt` | `DO/main/.../pipeline/Plugin.kt` | none |
| `SC/main/.../pipeline/FilterEntityPlugin.kt` | `DO/main/.../pipeline/FilterEntityPlugin.kt` | none once LogCollector is in domain |
| `SC/main/.../pipeline/MapEntityPlugin.kt` | `DO/main/.../pipeline/MapEntityPlugin.kt` | none |
| `SC/main/.../pipeline/ReportPlugin.kt` | `DO/main/.../pipeline/ReportPlugin.kt` | none |
| `SC/main/.../repository/HarvestLog.kt` | `DO/main/.../repository/HarvestLog.kt` | `internal fun` → `fun` (3×: `harvestLogLine`, `toHarvestLog`, `appendHarvestLog`); callers QueuedHarvestExecutor, SourceDryRunner stay in server-core |
| 5a conditional: `SC/main/.../pipeline/plugins/{CompareByInput,ExtendContentOptions,FeedParamsInput,NumberFilterOperator,NumericalFilterParams,SelectorsInput,StringFilterInput,StringFilterOperator,StringFilterParams}.kt` (9 files) | `DO/main/.../pipeline/plugins/` same names | none (gson only) |
| 5a conditional: `SC/main/.../AppMetrics.kt` | `DO/main/.../AppMetrics.kt` | none |

`FragmentTransformerPlugin.kt` (holds `FragmentOutput` with generated `ScrapeExtractFragment`/`ScrapedFeeds`, and `HttpResponse`) stays in server-core: only ScrapeService and the plugins use it.

#### New domain files (extracted declarations; delete them from the source file)

| new file | content | extracted from |
|---|---|---|
| `DO/main/.../scrape/LogCollector.kt` | `class LogCollector { val logs = mutableListOf<LogEntry>(); fun log(message: String) }` + `data class LogEntry(val message: String, val time: Long)` | `SC/main/.../scrape/ScrapeService.kt` lines 50–56 (today holds generated `LogStatement`) |
| `DO/main/.../scrape/ScrapeResult.kt` | value types of the Scraper port (below) | new |
| `DO/main/.../scrape/ScrapeMimeTypes.kt` | `object ScrapeMimeTypes { const val MIME_DATE = "text/x-date"; const val MIME_URL = "text/x-uri" }` | `SC/main/.../scrape/WebExtractService.kt` companion; keep `const val MIME_URL = ScrapeMimeTypes.MIME_URL` there for FeedPlugin/tests |
| `DO/main/.../pipeline/plugins/PluginParams.kt` | `ItemFilterParams`, `CompositeFilterParams`, `typealias CompositeFilterPluginParams` (CompositeFilterPlugin.kt); `ConditionalTag`, `CompositeFieldFilterParams`, `typealias ConditionalTagPluginParams` (ConditionalTagPlugin.kt); `DiffRecordsParams`, `CompareBy`, `enum RecordField` (DiffRecordsPlugin.kt); `FulltextPluginParams` (FulltextPlugin.kt); `EventsReportPluginParams` + `fun EventsReportPluginParams.toPluginExecutionJson()` (EventsReportPlugin.kt, step 6 needs it); `FeedPluginParams` (FeedPlugin.kt, needs `GenericFeedRule` in domain) | 5a conditional (skip what step 0 moved) |
| `DO/main/.../pipeline/plugins/DocumentJsonItem.kt` | `fun Document.asJsonItem(repository: Repository? = null): JsonItem` | `SC/main/.../pipeline/plugins/ConditionalTagPlugin.kt` line 87ff |
| `DO/main/.../pipeline/plugins/HtmlImages.kt` | `fun org.jsoup.nodes.Document.images(): List<org.jsoup.nodes.Element>` | `SC/main/.../pipeline/plugins/PrivacyPlugin.kt` line 242 |
| `DO/main/.../pipeline/plugins/AttachmentUrls.kt` | `fun createAttachmentUrl(appConfig: AppConfig, id: AttachmentId): String = "${appConfig.apiGatewayUrl}/attachment/${id.uuid}"` | `PrivacyPlugin.kt` line 253 (param type PropertyService → AppConfig; callers pass PropertyService unchanged) |
| `DO/main/.../feed/PointConversions.kt` | `fun JsonPoint.toPoint(): Point`, `fun LatLonPoint.toPoint(): Point` using `GeometryFactory(PrecisionModel(), 4326).createPoint(Coordinate(a, b))` (copy of `JtsUtil.createPoint`; JtsUtil stays in jpa-data) | `SC/main/.../feed/FeedParserService.kt` lines 80–87 |
| `DO/main/.../document/DocumentLimits.kt` | `object DocumentLimits { const val LEN_URL = <copy value> }` | `JD/main/.../data/jpa/document/DocumentEntity.kt` companion → `const val LEN_URL = DocumentLimits.LEN_URL` |
| `DO/main/.../config/CacheNames.kt` | `object CacheNames { FEED_LONG_TTL, FEED_SHORT_TTL, HTTP_RESPONSE, AGENT_RESPONSE, SERVER_SETTINGS }` (package `org.migor.feedless.config` keeps every import valid) | `SC/main/.../config/CacheConfig.kt` lines 29–35 |
| `DO/main/.../actions/ScrapeActionPlacement.kt` | `fun ScrapeAction.placedAt(sourceId: SourceId, pos: Int): ScrapeAction` — `when` over the sealed subtypes, each `copy(sourceId = sourceId, pos = pos)` (verify every subtype is a data class with both ctor params) | replaces SourceUseCase's JPA round trip `scrapeAction.toEntity(); sourceId=…; pos=…; toDomain()` at `source/SourceUseCase.kt` lines 190–193 and 263–266 (imports `data.jpa.source.toDomain/toEntity`) |

#### Ports to add in `domain`

```kotlin
// DO/main/.../scrape/Scraper.kt
package org.migor.feedless.scrape
interface Scraper {
  /** Runs the source's flow; the harvester only needs the last action's fragment. */
  suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeResult
}
// DO/main/.../scrape/ScrapeResult.kt
data class ScrapeResult(val actionCount: Int, val lastFragment: ScrapedFragmentOutput?)
data class ScrapedFragmentOutput(val items: List<JsonItem>?, val fragments: List<ScrapedFragment>?)
data class ScrapedFragment(val html: String?, val text: String?, val data: ScrapedData?, val uniqueBy: ScrapedFragmentPart)
data class ScrapedData(val mimeType: String, val data: String)
enum class ScrapedFragmentPart { html, text, data }
```
Implemented by new `SC/main/.../scrape/ScraperAdapter.kt`: `@Service @Profile("${AppProfiles.scrape} & ${AppLayer.service}") class ScraperAdapter(private val scrapeService: ScrapeService) : Scraper` — maps `ScrapeOutput.outputs.size` / `outputs.lastOrNull()?.fragment` (`items`, `fragments` → `ScrapedFragment(html?.data, text?.data, data?.let { ScrapedData(it.mimeType, it.data) }, ScrapedFragmentPart.valueOf(uniqueBy.name))`). ScrapeService cannot implement it directly: same name and parameters as its existing `scrape(source, logCollector): ScrapeOutput`. 1 method. Step 7 (FeedService) extends this port.

```kotlin
// DO/main/.../pipeline/PipelinePlugins.kt
package org.migor.feedless.pipeline
interface PipelinePlugins {
  suspend fun findAll(): List<Plugin>
  suspend fun findById(id: String): Plugin?
}
```
Implemented by `SC/main/.../pipeline/PluginService.kt`: add `: PipelinePlugins`, `override` on the existing `findAll()`, add `override suspend fun findById(id: String): Plugin? = resolveById<Plugin>(id)`. 2 methods. DocumentUseCase line 211 becomes `when (val plugin = pipelinePlugins.findById(job.pluginId))`; ReportUseCase (step 6) uses `findById(id) as? ReportPlugin<*>`.

```kotlin
// DO/main/.../message/Notifications.kt  (extend it if step 3 already created it for UserUseCase)
package org.migor.feedless.message
interface Notifications {
  /** Pushes items to the owner's authorized Telegram chat; no-op when Telegram is off or unlinked. */
  suspend fun pushToOwner(ownerId: UserId, items: List<JsonItem>)
}
```
Implemented by new `SC/main/.../message/NotificationsAdapter.kt`: `@Service @Profile(AppLayer.service) class NotificationsAdapter(@Lazy private val telegramBotServiceMaybe: Optional<TelegramBotService>, private val messageService: MessageService) : Notifications` with the body of `DocumentUseCase.triggerPostReleaseEffects` lines 373–380 (`findByUserIdAndAuthorizedIsTrue(ownerId)` → `messageService.publishMessage(TelegramBotService.toTopic(chatId), item)`). DocumentUseCase keeps the `pushNotificationsEnabled` check and calls `notifications.pushToOwner(repository.ownerId, documents.map { it.asJsonItem(repository) })`. 1 method (step 5 part). An adapter is needed because TelegramBotService is `@ConditionalOnBean`.

```kotlin
// DO/main/.../common/AppConfig.kt — members step 5 needs (add if missing)
interface AppConfig { val apiGatewayUrl: String; val appHost: String }
```
PropertyService: `override lateinit var apiGatewayUrl`, `override lateinit var appHost` (a `lateinit var` may override an abstract `val`). Callers in moving classes: RepositoryUseCase lines 132, 138, 416/423 (`createAttachmentUrl`, `createDocumentUrl`), DocumentUseCase line 214 (passes it to `toJsonItem`). 2 members.

```kotlin
// DO/main/.../analytics/Analytics.kt
package org.migor.feedless.analytics
interface Analytics { suspend fun track() }
```
`SC/main/.../analytics/AnalyticsService.kt`: `: Analytics`, `override suspend fun track()`. Needed by DocumentController (moves to http-api); steps 6/7 reuse it (AttachmentController, FeedController, ServerConfigResolver). 1 method.

```kotlin
// DO/main/.../source/StoredFlowParser.kt
package org.migor.feedless.source
interface StoredFlowParser { fun storedFlowToDomainActions(storedFlow: String): List<ScrapeAction> }
```
Implemented by `HA/main/.../http/mapper/HttpScrapeFlowMapper.kt` (add `: StoredFlowParser`, `override` on line 70; it parses the stored OpenAPI `ScrapeFlow` JSON with Jackson, so it cannot become a pure domain function). `SC/main/.../repository/QueuedHarvestExecutor.kt`: ctor param `scrapeFlowMapper: HttpScrapeFlowMapper` → `storedFlowParser: StoredFlowParser`, drop `import org.migor.feedless.http.mapper.HttpScrapeFlowMapper`, line 103 uses `storedFlowParser`. This removes server-core's only main import of an http-api class. 1 method.

```kotlin
// DO/main/.../document/DocumentQueryParser.kt
package org.migor.feedless.document
interface DocumentQueryParser {
  /** JSON of the GraphQL RecordsWhereInput, as the feed URL's `where` parameter carries it. */
  fun parseFilter(json: String): DocumentsFilter
  /** JSON of the GraphQL RecordOrderByInput. */
  fun parseOrderBy(json: String): RecordOrderBy
}
```
Needed because RepositoryController parses query params with Gson into generated `RecordsWhereInput`/`RecordOrderByInput` and calls `toDomain()` from `DocumentResolver.kt`; http-api must not see GraphQL types. 5a: implement in `SC/main/.../document/GraphqlDocumentQueryParser.kt` (`@Component @Profile("${AppProfiles.repository} & ${AppLayer.api}")`, `Gson().fromJson(json, RecordsWhereInput::class.java).toDomain()` / `RecordOrderByInput`); 5b moves it to graphql-api with DocumentResolver. 2 methods.

#### Edits outside moves (5a)

- `SC/main/.../repository/RepositoryHarvester.kt`: `scrapeService: ScrapeService` → `scraper: Scraper`; `importElement(output: ScrapeResult, …)`: `if (output.actionCount == 0) throw NoItemsRetrievedException()`, `output.lastFragment?.let { … }`; `importFragment(…, fragment: ScrapedFragment, …)`; `ScrapeExtractFragment.createDocument` → `ScrapedFragment.createDocument` (`ScrapedFragmentPart`, `html`, `text`, `data?.data`, `data?.mimeType` — now plain strings, not `TextData`/`MimeData`); drop imports `generated.types.ScrapeExtractFragment(Part)`, `scrape.ScrapeService`, `scrape.ScrapeOutput`, `data.jpa.document.DocumentEntity.Companion.LEN_URL` (→ `DocumentLimits.LEN_URL`), `WebExtractService.Companion.MIME_URL` (→ `ScrapeMimeTypes.MIME_URL`).
- `SC/main/.../document/DocumentUseCase.kt`: `pluginService: PluginService` → `pipelinePlugins: PipelinePlugins`; `telegramBotServiceMaybe` + `messageService` → `notifications: Notifications`; `propertyService: PropertyService` → `appConfig: AppConfig`; `TooManyConnectionsPerHostException` (async-http-client) check stays (domain gets `compileOnly`, see build).
- `SC/main/.../repository/RepositoryUseCase.kt`: `propertyService` → `appConfig: AppConfig`; `Document.toJsonItem(propertyService: PropertyService, …)` (line 400) → `(appConfig: AppConfig, …)`.
- `SC/main/.../source/SourceUseCase.kt`: replace the two JPA round trips with `scrapeAction.placedAt(source.id, index)`; drop `data.jpa.source.*` imports.
- `SC/main/.../scrape/ScrapeService.kt`: delete `LogCollector`; line 291 maps agent logs to `LogEntry(it.message, it.time)`.
- `SC/main/.../scrape/ScrapeQueryResolver.kt` line 54: `logs = logCollector.logs.map { LogStatement(message = it.message, time = it.time) }`.
- `SC/main/.../api/mapper/ScrapeResponseMapper.kt` line 54 (graphql-api after step 0): unaffected (maps DTO→DTO), check it compiles.
- `SC/main/.../pipeline/plugins/{PrivacyPlugin,ConditionalTagPlugin,CompositeFilterPlugin,DiffRecordsPlugin,FulltextPlugin,EventsReportPlugin,FeedPlugin}.kt`: delete the extracted declarations; same package so no import changes.
- `SC/main/.../feed/FeedParserService.kt`: delete both `toPoint` functions (same FQN `org.migor.feedless.feed.toPoint` now in domain).
- `SC/main/.../config/CacheConfig.kt`: delete `object CacheNames`.
- `SC/main/.../scrape/WebExtractService.kt`: companion consts delegate to `ScrapeMimeTypes`.
- `JD/main/.../data/jpa/document/DocumentEntity.kt`: `LEN_URL = DocumentLimits.LEN_URL`.
- `SC/main/.../repository/RepositoryController.kt`: inject `DocumentQueryParser`; `parseWhere`/`parseOrderBy` delegate; drop `generated.types.*` and `document.toDomain` imports.
- `SC/main/.../document/DocumentController.kt`: `analyticsService: AnalyticsService` → `analytics: Analytics`.

#### Tests touched in 5a (stay in server-core for now)

- `SC/test/.../repository/RepositoryHarvesterTest.kt` (16): ScrapeService mock → Scraper mock; `ScrapeOutput`/`ScrapeActionOutput`/`FragmentOutput` + generated `ScrapeExtractFragment`/`MimeData`/`TextData`/`ScrapeExtractFragmentPart` fixtures → `ScrapeResult`/`ScrapedFragmentOutput`/`ScrapedFragment`/`ScrapedData`/`ScrapedFragmentPart`; `FeedlessPlugins.x.name` → string literal ids (`"org_feedless_feed"` …); `MIME_URL` import → `ScrapeMimeTypes`.
- `SC/test/.../repository/QueuedHarvestExecutorTest.kt` (9): `RepositoryHarvester(…)` gets a Scraper mock and the new ScrapeResult fixtures; `QueuedHarvestExecutor(…, HttpScrapeFlowMapper())` still compiles (it implements StoredFlowParser); test-only import of http-api is acceptable.
- `SC/test/.../repository/OneRealHarvestPerSourceIntTest.kt` (12): constructs `RepositoryHarvester` with `ScraperAdapter(scrapeServiceMock)` or a Scraper mock.
- `SC/test/.../document/DocumentUseCaseTest.kt` (16): `PluginService` mock → `PipelinePlugins` mock; `TelegramBotService`/`MessageService` → `Notifications` mock (assert `pushToOwner`); `PropertyService` → `AppConfig`; `FeedlessPlugins` → literals. Keep constructing real `CompositeFilterPlugin`/`FulltextPlugin` until 5b.
- `SC/test/.../repository/RepositoryUseCaseTest.kt` (10), `RepositoryUpdateTest.kt` (8): `PropertyService` mock → `AppConfig` mock.
- `SC/test/.../source/SourceUseCaseTest.kt` (7): unchanged ctor; verify actions still carry `sourceId`/`pos`.
- `SC/test/.../document/DocumentControllerIntTest.kt`: `@MockitoBean AnalyticsService` still satisfies `Analytics` (the mock subclasses it); no edit.

### 5b — moves

#### Main: server-core → domain (11)

| from | to | edits |
|---|---|---|
| `SC/main/.../repository/RepositoryGuard.kt` | `DO/main/.../repository/RepositoryGuard.kt` | none (`AccessDeniedException` → spring-security-core) |
| `SC/main/.../repository/RepositoryUseCase.kt` | `DO/main/.../repository/RepositoryUseCase.kt` | `: RepositoryUseCasePort` → `: RepositoryProvider`; keeps `Pageable.toPageableRequest()` and `Document.toJsonItem(…)` top-level funcs |
| `SC/main/.../source/SourceUseCase.kt` | `DO/main/.../source/SourceUseCase.kt` | drop `: SourceUseCasePort`; keep `@Lazy RepositoryHarvester` |
| `SC/main/.../document/DocumentGuard.kt` | `DO/main/.../document/DocumentGuard.kt` | `: DocumentGuardPort` → `: ResourceGuard<DocumentId, Document>` |
| `SC/main/.../document/DocumentUseCase.kt` | `DO/main/.../document/DocumentUseCase.kt` | `: DocumentProvider, DocumentUseCasePort` → `: DocumentProvider`; copy the interface's defaults onto `findAllByRepositoryId` (`filter = null, orderBy = null, status = ReleaseStatus.released, tags = emptyList()`) — overrides inherit defaults, the concrete method loses them when the interface goes |
| `SC/main/.../repository/HarvestService.kt` | `DO/main/.../repository/HarvestService.kt` | drop `: HarvestUseCasePort` and the `override`s |
| `SC/main/.../repository/RepositoryHarvester.kt` | `DO/main/.../repository/RepositoryHarvester.kt` | keeps `fun nextCronDate(…)` (ReportUseCase step 6 imports it) |
| `SC/main/.../repository/InboxService.kt` | `DO/main/.../repository/InboxService.kt` | none (TelegramBotService, in server-core, injects it) |
| `SC/main/.../pipeline/DocumentPipelineService.kt` | `DO/main/.../pipeline/DocumentPipelineService.kt` | none |
| `SC/main/.../pipeline/SourcePipelineService.kt` | `DO/main/.../pipeline/SourcePipelineService.kt` | `internal constructor` → public (cross-module) |
| `SC/main/.../repository/HarvestLog.kt` | (moved in 5a) | — |

Pipeline services go to domain: they touch only repositories, and SourceUseCase injects SourcePipelineService. Profiles stay `scrape & scheduler`.

Stay in server-core (infrastructure / scheduler): `repository/QueuedHarvestExecutor.kt`, `RepositoryHarvesterExecutor.kt`, `SourceDryRunner.kt` (uses full `ScrapeOutput` incl. fetch debug; only QueuedHarvestExecutor calls it, so no port), `AnalyticsSyncExecutor.kt`, `GithubService.kt`, `pipeline/DocumentPipelineJobExecutor.kt`, `SourcePipelineJobExecutor.kt`, `common/CleanupExecutor.kt`, `pipeline/PluginService.kt`, all `pipeline/plugins/*Plugin.kt`, `FragmentTransformerPlugin.kt`, `scrape/*`, `transport/*`, `message/MessageService.kt`, the 5a adapters.

#### Main: server-core → graphql-api (5, +3 if step 0 didn't)

| from | to | edits |
|---|---|---|
| `SC/main/.../repository/RepositoryResolver.kt` | `GQ/main/.../repository/RepositoryResolver.kt` | none beyond step-0 imports (`@PreAuthorize("@capabilityService…")` bean name unchanged) |
| `SC/main/.../document/DocumentResolver.kt` | `GQ/main/.../document/DocumentResolver.kt` | `propertyService: PropertyService` → `appConfig: AppConfig` (lines 52, 71, 96, 127, 136; matches the step-0 `Document.toDto(appConfig)` signature) |
| `SC/main/.../document/GraphqlDocumentQueryParser.kt` (5a) | `GQ/main/.../document/GraphqlDocumentQueryParser.kt` | none |
| `SC/main/.../api/mapper/RepositoryMapper.kt` | `GQ/main/.../api/mapper/RepositoryMapper.kt` | needs `DiffRecordsParams`/`FulltextPluginParams` in domain |
| `SC/main/.../api/mapper/RepositoryCommandMapper.kt` | `GQ/main/.../api/mapper/RepositoryCommandMapper.kt` | none |
| `SC/main/.../api/mapper/DocumentCommandMapper.kt` | `GQ/main/.../api/mapper/DocumentCommandMapper.kt` | none |
| conditional `SC/main/.../api/mapper/{DocumentMapper,SourceMapper,ScrapeActionMapper}.kt` | `GQ/main/.../api/mapper/` same | only if step 0 did not (it has to, see assumptions) |

#### Main: server-core → http-api (2)

| from | to | edits |
|---|---|---|
| `SC/main/.../repository/RepositoryController.kt` | `HA/main/.../repository/RepositoryController.kt` | uses `FeedExporter` (feed-parser), `MeterRegistry`, `AppMetrics`, `DocumentQueryParser`; `/feed.xsl` classpath resource — verify where `feed.xsl` lives (server-core `src/main/resources`? then it resolves from the boot jar at runtime but not in http-api tests) |
| `SC/main/.../document/DocumentController.kt` | `HA/main/.../document/DocumentController.kt` | uses `Analytics`, `MeterRegistry`, `AppMetrics` |

Both keep packages `org.migor.feedless.repository` / `.document`, so `HttpApiAutoConfiguration` (scans `org.migor.feedless.http`) does not see them; they are found only by server-core's `@SpringBootApplication` scan. Either widen the auto-configuration (`@ComponentScan(basePackageClasses = [RepositoryController::class, DocumentController::class])` added next to the `http` scan) or accept it; http-api tests for them would need their own test app.

#### `*Port` interfaces removed (5 files deleted)

- `DO/main/.../repository/RepositoryUseCasePort.kt` → consumers: `HA/main/.../http/RepositoryHttpController.kt`, `HA/main/.../http/RepositoryAccessGuard.kt`, `HA/test/.../http/RepositoryAccessFixture.kt`, `RepositoryHttpControllerTest.kt`, `RepositoryHttpControllerCreateTest.kt`, `RecordHttpControllerTest.kt`, `HarvestHttpControllerTest.kt`, `RepositoryAccessGuardTest.kt`, `SourceHttpControllerTest.kt`, `SourceHttpControllerCreateTest.kt` → `RepositoryUseCase`.
- `DO/main/.../source/SourceUseCasePort.kt` → `HA/main/.../http/SourceHttpController.kt`, `HA/test/.../http/SourceHttpControllerTest.kt`, `SourceHttpControllerCreateTest.kt` → `SourceUseCase`.
- `DO/main/.../document/DocumentUseCasePort.kt` → `HA/main/.../http/RecordHttpController.kt`, `HA/test/.../http/RecordHttpControllerTest.kt` → `DocumentUseCase`.
- `DO/main/.../document/DocumentGuardPort.kt` → `RecordHttpController.kt`, `RecordHttpControllerTest.kt` → `DocumentGuard`.
- `DO/main/.../harvest/HarvestUseCasePort.kt` → `HA/main/.../http/HarvestHttpController.kt`, `HA/test/.../http/HarvestHttpControllerTest.kt` → `HarvestService`.
- No server-core main/test code references these five interfaces except the implementers. `RepositoryProvider` (implemented also by github-connector's `GitRepositoryProvider`) and `DocumentProvider` (`GitDocumentProvider`) stay; nothing injects them today.
- http-api tests use `@MockitoBean lateinit var x: <Port>`; switching to concrete classes works (Mockito 5 inline mock maker; kotlin-spring opens `@Service` classes anyway). Check each test for hand-written fakes (`object : XPort`) — none found in the grep of `RepositoryAccessFixture`/`RecordHttpControllerTest`/`HarvestHttpControllerTest`.

#### Tests

| from | to | class | edits |
|---|---|---|---|
| `SC/test/.../repository/RepositoryGuardTest.kt` (2) | `DO/test/.../repository/RepositoryGuardTest.kt` | unit | Mother/`eq` from domain testFixtures |
| `SC/test/.../repository/RepositoryHarvesterTest.kt` (16) | `DO/test/.../repository/RepositoryHarvesterTest.kt` | unit | 5a rewrite done |
| `SC/test/.../repository/RepositoryUseCaseTest.kt` (10) | `DO/test/.../repository/RepositoryUseCaseTest.kt` | unit | — |
| `SC/test/.../repository/RepositoryUpdateTest.kt` (8) | `DO/test/.../repository/RepositoryUpdateTest.kt` | unit | — |
| `SC/test/.../source/SourceUseCaseTest.kt` (7) | `DO/test/.../source/SourceUseCaseTest.kt` | unit | builds inputs from generated `SourceInput`/`ScrapeFlowInput`… via `api.mapper.fromDto`/`toSource`: rewrite fixtures to domain `Source`/`ScrapeAction`; fallback: move to `GQ/test/...` instead |
| `SC/test/.../document/DocumentUseCaseTest.kt` (16) | `DO/test/.../document/DocumentUseCaseTest.kt` | unit | replace real `CompositeFilterPlugin`/`FulltextPlugin` with mocks of `FilterEntityPlugin`/`MapEntityPlugin`; plugin params from domain `PluginParams.kt` |
| `SC/test/.../repository/RepositoryResolverTest.kt` (2) | `GQ/test/.../repository/RepositoryResolverTest.kt` | unit | none |
| `SC/test/.../document/DocumentResolverTest.kt` (3) | `GQ/test/.../document/DocumentResolverTest.kt` | unit | `PropertyService` → `AppConfig` mock (skip if step 0 moved it with DocumentMapper) |
| `SC/test/.../api/mapper/RepositoryCommandMapperTest.kt` (6) | `GQ/test/.../api/mapper/RepositoryCommandMapperTest.kt` | unit | none |
| `SC/test/.../harvest/HarvestRepositoryIntTest.kt` (11) | `JD/test/.../harvest/HarvestRepositoryIntTest.kt` | persistence-only (repositories, `HarvestDAO`, `PlatformTransactionManager`) | drop `session.StatelessAuthService` from `@MockitoBean` and server-core-only profiles; `@ActiveProfiles("test", AppProfiles.repository, AppProfiles.source, AppProfiles.user, AppLayer.repository)` against the jpa-data test app |
| `SC/test/.../source/SourceRepositoryIntTest.kt` (7) | `JD/test/.../source/SourceRepositoryIntTest.kt` | persistence-only (repositories only) | same `StatelessAuthService` removal |
| `SC/test/.../repository/AbstractRepositoryEntityTest.kt` (1) | `JD/test/.../repository/AbstractRepositoryEntityTest.kt` | unit of `data.jpa.repository.extractHashTags` | none |

Stay in server-core (full context or server-core classes): `repository/OneRealHarvestPerSourceIntTest.kt` (constructs ScrapeService mock, RepositoryHarvester, QueuedHarvestExecutor, HttpScrapeFlowMapper — not persistence-only), `QueuedHarvestExecutorTest.kt`, `RepositoryHarvesterExecutorTest.kt`, `AnalyticsSyncExecutorTest.kt`, `RepositoryEntityPluginsDeserializationIntTest.kt` (real plugins), `RepositoryResolverIntTest.kt`, `RepositoryUseCaseIntTest.kt` (mocks AgentService, StatelessAuthService, SourcePipelineService…), `document/DocumentIntTest.kt` (mocks PluginService, HttpService…), `document/DocumentControllerIntTest.kt`, `pipeline/DocumentPipelineJobExecutorTest.kt`, `pipeline/SourcePipelineJobExecutorTest.kt`, `jobs/CleanupExecutorTest.kt`, `transport/TelegramBotServiceTest.kt`. `@MockitoBean PropertyService`/`PluginService`/`AnalyticsService` in these still satisfy `AppConfig`/`PipelinePlugins`/`Analytics` injection points (the mocks subclass the implementing class).

Test methods moved: domain 59, graphql-api 11, jpa-data 19 (89 total); server-core loses exactly these, so the cross-module total must be unchanged.

#### Build changes (on top of step 0)

`packages/domain/build.gradle.kts` (assumes step 0 added the Spring BOM platform, kotlin-spring plugin, spring-context/-tx/-security-core, micrometer-core):
```kotlin
dependencies {
  implementation(libs.jsoup)                        // images(), Jsoup in RepositoryHarvester
  implementation(libs.tika.core)                    // RepositoryHarvester
  implementation(libs.commons.lang3)                // StringUtils
  implementation(libs.spring.boot.validation)       // jakarta.validation in SourceUseCase, RepositoryHarvester (+ provider for tests)
  implementation("org.springframework.data:spring-data-commons") // Pageable/PageRequest in RepositoryUseCase, HarvestService, toPageableRequest
  implementation("org.springframework:spring-web")  // UriComponentsBuilder in RepositoryUseCase line 138
  implementation("jakarta.annotation:jakarta.annotation-api") // @PostConstruct in RepositoryHarvester
  compileOnly(libs.async.http.client)               // TooManyConnectionsPerHostException check in DocumentUseCase
  testImplementation(libs.async.http.client)
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation(libs.kotlinx.coroutines.test)
}
```
`packages/http-api/build.gradle.kts`:
```kotlin
dependencies {
  implementation(platform(libs.spring.boot.bom))
  implementation(project(":packages:feed-parser"))  // FeedExporter in RepositoryController
  implementation("io.micrometer:micrometer-core")    // MeterRegistry in both controllers
}
```
`packages/graphql-api/build.gradle.kts`: `implementation(libs.gson)` (GraphqlDocumentQueryParser, RepositoryMapper) if step 0 did not add it. `packages/jpa-data`: nothing new (step 0 fixtures).

#### Verification

```bash
./gradlew :packages:domain:test :packages:jpa-data:test :packages:graphql-api:test :packages:http-api:test :packages:server-core:test :packages:github-connector:test
# boundaries
grep -rn 'import org.migor.feedless.generated' packages/domain/src/main            # empty
grep -rn 'org.migor.feedless.data.jpa' packages/domain/src/main                    # empty
grep -rn 'org.migor.feedless.http.mapper' packages/server-core/src/main            # empty
grep -rnE 'RepositoryUseCasePort|SourceUseCasePort|DocumentUseCasePort|DocumentGuardPort|HarvestUseCasePort' packages --include='*.kt' | grep -v /build/   # empty
# executed-test total, compare with the pre-step value
find packages -path '*build/test-results/test/TEST-*.xml' -print0 | xargs -0 sed -n 's/.*<testsuite [^>]*tests="\([0-9]*\)".*/\1/p' | paste -sd+ - | bc
```
E2E smoke after step 5 (spec): the Go suite `packages/cli/e2e/` (`//go:build e2e`: `stack_test.go`, `broken_source_test.go`, `feedctl_test.go`, …) run by `./gradlew :packages:cli:e2eTest` (`go test -tags e2e -count=1 -timeout 12m -v ./e2e/...`) against a freshly built core image (`./gradlew :packages:server-core:buildAmdDockerImage -PfeedlessVersion=… -PdockerImageTag=…`; check `stack_test.go` for the image tag/env it expects). Note `packages/cli` is a Gradle module here (settings.gradle.kts includes it), contrary to AGENTS.md.

#### Risks / gotchas

- Ports do not break bean-graph cycles; only `@Lazy`/`ObjectProvider` does. DocumentUseCase → PipelinePlugins (PluginService, `@Lazy val plugins`) → DiffRecordsPlugin (`@Lazy DocumentUseCase`); ScrapeService → PluginService → FulltextPlugin (`@Lazy ScrapeService`); SourceUseCase (`@Lazy RepositoryHarvester`) → RepositoryHarvester → RepositoryUseCase → SourceUseCase. Keep every `@Lazy` in step 5; the spec's claim that the plugin/notification ports dissolve cycles holds only for compile-time module cycles.
- Profiles: RepositoryHarvester is `repository & service & scheduler`; in api-only processes SourceUseCase's `@Lazy` proxy must never be dereferenced (unchanged behaviour). ScraperAdapter must carry ScrapeService's `scrape & service` or the harvester fails to wire under scheduler. NotificationsAdapter must exist whenever DocumentUseCase does → `@Profile(AppLayer.service)` only. GraphqlDocumentQueryParser must match RepositoryController's `repository & api`.
- `@Cacheable(CacheNames.FEED_SHORT_TTL)` on RepositoryUseCase and `@Transactional`s need proxyable (open) classes in domain → kotlin-spring plugin on domain (step 0) is mandatory, else caching silently stops or proxies fail at startup.
- `internal` across modules: HarvestLog functions and `SourcePipelineService`'s `internal constructor` must become public; executors' `internal constructor`s stay (same module).
- MapStruct bean names: graphql-api `api.mapper.DocumentMapper` (componentModel spring via MapStructConfig → bean `documentMapperImpl`) vs jpa-data `data.jpa.document.DocumentMapper` (default component model, `Mappers.getMapper` INSTANCE, not a bean) — no collision. `api/mapper/RepositoryMapper.kt`/`SourceMapper.kt` are extension-function files, not MapStruct; jpa-data's `RepositoryMapper`/`SourceMapper` are INSTANCE mappers. The graphql-api kapt output package stays `org.migor.feedless.api.mapper`.
- Split packages: domain now has files in `pipeline.plugins`, `scrape`, `feed`, `config`, which server-core also uses. Fine without JPMS, but file-facade class names must not clash (`PointConversionsKt` vs `FeedParserServiceKt` etc.); never create a domain file with the same name as a server-core file in the same package.
- Duplicate FQNs already on the classpath (`org.migor.feedless.util.FeedUtil`/`HtmlUtil` in both feed-parser and server-core; `common.HttpResponse` in server-core `HttpService.kt` and feed-parser) — step 0 concern, but domain must not add a third.
- `DocumentUseCase.findAllByRepositoryId` defaults (see table) — without copying them, http-api's RecordHttpController and graphql resolvers fail to compile only after the Port is deleted.
- RepositoryHarvesterTest/QueuedHarvestExecutorTest/OneRealHarvestPerSourceIntTest are the largest fixture rewrites (37 test methods) — do them in 5a so 5b is a pure move.
- `feed.xsl` resource location for RepositoryController (see above).

#### Alternative A (not recommended): keep RepositoryHarvester in server-core

Spec lists RepositoryHarvester among the step-5 moves, but it is the only use case whose data is generated GraphQL types (`ScrapeExtractFragment`, `ScrapeExtractFragmentPart`, and via `ScrapeOutput`: `FetchActionDebugResponse` → `NetworkRequest`, `ViewPort`; `FragmentOutput` → `ScrapeExtractResponse`, `ScrapedFeeds` → `TransientGenericFeed`, `RemoteNativeFeed`; `LogCollector` → `LogStatement`). A: leave `RepositoryHarvester.kt`, `RepositoryHarvesterTest.kt` in server-core, add port `repository.SourceScrapeRunner { suspend fun scrapeSource(source: Source, logCollector: LogCollector): Int }` (RepositoryHarvester implements; SourceUseCase injects it `@Lazy`), no Scraper port in step 5 (step 7 still needs it for FeedService). Saves the ScrapeResult types, ScraperAdapter and the 37-method fixture rewrite; costs a spec deviation, a harvesting use case outside domain, and LogCollector still has to move (SourceUseCase and the plugin interfaces reference it). HarvestLog then stays too.

#### File counts

- 5a: whole-file moves 5 (Plugin, FilterEntityPlugin, MapEntityPlugin, ReportPlugin, HarvestLog) + conditional 10 (9 plugin-param files, AppMetrics); new domain files 17 (LogCollector, ScrapeResult, ScrapeMimeTypes, PluginParams, DocumentJsonItem, HtmlImages, AttachmentUrls, PointConversions, DocumentLimits, CacheNames, ScrapeActionPlacement, Scraper, PipelinePlugins, Notifications, Analytics, StoredFlowParser, DocumentQueryParser); new server-core files 3 (ScraperAdapter, NotificationsAdapter, GraphqlDocumentQueryParser).
- 5b: main moves 18 (domain 10 + graphql-api 6 + http-api 2) + conditional 3 mappers; test moves 12 (domain 6, graphql-api 3, jpa-data 3); deleted 5 Port files.

## Steps 6–8 overview (attachment/annotation/report, feed/license/status/agent/scrape/plugins, wrap-up)

Abbreviations used only in this section's prose: `SC` = `packages/server-core/src/main/kotlin/org/migor/feedless`, `SCT` = `packages/server-core/src/test/kotlin/org/migor/feedless`. All move lines below spell paths out in full.

Prerequisites assumed from steps 0–5 (if one is missing, do it at the start of step 6): `AppMetrics`, `CacheNames` (from `config/CacheConfig.kt`), `LogCollector` (from `scrape/ScrapeService.kt`), `Plugin`/`ReportPlugin`/`FilterEntityPlugin`/`MapEntityPlugin` interfaces, `asJsonItem()` (from `pipeline/plugins/ConditionalTagPlugin.kt`), `nextCronDate` (from `repository/RepositoryHarvester.kt`), `GenericFeedSelectors`/`Selectors`/`ExtendContext` (from `scrape/WebToFeedTransformer.kt`), the filter param types (`ItemFilterParams`, `CompositeFilterParams`, `CompositeFieldFilterParams`, `NumericalFilterParams`, `StringFilterParams`, `StringFilterOperator`, `NumberFilterOperator`) and the coroutine extensions `userId()`/`corrId()` all live in `domain`; `domain` applies `kotlin.spring`, imports the Spring Boot BOM and has `spring-security-oauth2-jose`, because the bridge `injectCapabilitiesFromJwt(jwt: Jwt)` exposes `Jwt`; `PipelinePlugins` has `suspend fun <T : Plugin> resolveById(id: String, type: KClass<T>): T?` plus the domain extension `suspend inline fun <reified T : Plugin> PipelinePlugins.resolveById(id: String): T? = resolveById(id, T::class)`, since an interface cannot declare a reified inline member.

---

## Step 6 — attachment, annotation, report

### 1. Moves

Main (14 files):

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/attachment/AttachmentUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/attachment/AttachmentUseCase.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/attachment/AttachmentGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/attachment/AttachmentGuard.kt` | none (all methods still `TODO()`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/annotation/AnnotationUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/annotation/AnnotationUseCase.kt` | **yes**: generated `CreateAnnotationInput`/`DeleteAnnotationInput`/`AnnotationWhereInput`/`TextAnnotationInput` → domain commands (§4) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/annotation/AnnotationGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/annotation/AnnotationGuard.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/report/ReportUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportUseCase.kt` | **yes**: generated `SegmentInput`/`IntervalUnit` → `SegmentCreate`; `PluginService` → `PipelinePlugins`; `EventsReportPluginParams` import (§4) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/report/ReportGuard.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/report/ReportGuard.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/ApiUrls.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/api/ApiUrls.kt` | none. Needed now, not in step 7: `ReportController` and `MailController` import `reportDelete`/`mailForwardingAllow`. Remaining importers (`config/SecurityConfig.kt`, `feed/FeedController.kt`, `SCT/feed/FeedControllerIntTest.kt`, `SCT/config/SecurityConfigIntTest.kt`) keep the same FQN |
| `packages/server-core/src/main/kotlin/org/migor/feedless/attachment/AttachmentResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/attachment/AttachmentResolver.kt` | none (`MultipartFile` comes from `spring-boot-starter-web`, which `graphql-api` already has) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/annotation/AnnotationResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/annotation/AnnotationResolver.kt` | **yes**: map inputs → commands |
| `packages/server-core/src/main/kotlin/org/migor/feedless/annotation/AnnotationDtoMapper.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/annotation/AnnotationDtoMapper.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/report/ReportResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/report/ReportResolver.kt` | **yes**: drop the unused `jwtTokenIssuer` constructor parameter; map `SegmentInput` → `SegmentCreate` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/attachment/AttachmentController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/attachment/AttachmentController.kt` | **yes**: `HttpService` → `HttpFetcher`, `AnalyticsService` → `Analytics` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/report/ReportController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/report/ReportController.kt` | **yes**: `JwtTokenIssuer` → `TokenIssuer` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/mail/MailController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/mail/MailController.kt` | **yes**: `FreemarkerTemplateService` → domain `TemplateService`, which `FreemarkerTemplateService` already implements (`@Profile(AppLayer.service)`), so `http-api` needs no dependency on `freemarker-templates` |

New files (step 6):
- `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/plugins/EventsReportPluginParams.kt`: `data class EventsReportPluginParams` and `fun EventsReportPluginParams.toPluginExecutionJson()`, both cut verbatim from `SC/pipeline/plugins/EventsReportPlugin.kt`; the plugin keeps importing them under the same FQN.
- `packages/domain/src/main/kotlin/org/migor/feedless/report/SegmentCreate.kt`, `packages/domain/src/main/kotlin/org/migor/feedless/annotation/AnnotationCreate.kt` (§4).
- `packages/domain/src/main/kotlin/org/migor/feedless/analytics/Analytics.kt`, `packages/domain/src/main/kotlin/org/migor/feedless/common/HttpFetcher.kt` (§2).

Tests (5 files):

| from | to | edits |
|---|---|---|
| `packages/server-core/src/test/kotlin/org/migor/feedless/attachment/AttachmentResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/attachment/AttachmentResolverTest.kt` | none (tests `internal fun Attachment.toDto()`, which is visible because the test is in the same module) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/annotation/AnnotationResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/annotation/AnnotationResolverTest.kt` | none |
| `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/report/ReportResolverTest.kt` | none |
| `packages/server-core/src/test/kotlin/org/migor/feedless/annotation/AnnotationUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/annotation/AnnotationUseCaseTest.kt` | **yes**: build `AnnotationCreate` instead of generated inputs; a case that asserts "Insufficient data for annotation" moves to `AnnotationResolverTest` (the input→command mapping now owns it), which keeps the count |
| `packages/server-core/src/test/kotlin/org/migor/feedless/report/ReportUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/report/ReportUseCaseTest.kt` | **yes**: the real `PluginService(emptyList(), emptyList(), listOf(eventsReportPlugin))` → `mock<PipelinePlugins>()` stubbed on `resolveById(any(), eq(ReportPlugin::class))`; the `EventsReportPlugin` import → mock of `ReportPlugin<*>`; generated `SegmentInput`/`ScheduledSegmentInput`/… → `SegmentCreate` |

Staying in server-core: `SCT/report/ReportJobExecutorTest.kt` (the executor stays) and `SCT/report/ReportUseCaseIntTest.kt` (full `@SpringBootTest` with Postgres, `ScrapeService`, `RepositoryHarvester`, `StatelessAuthService`; edit: call `createReport` with a `SegmentCreate`). Persistence-only tests for `jpa-data`: none in this step.

### 2. Ports

- **New** `packages/domain/src/main/kotlin/org/migor/feedless/analytics/Analytics.kt` (1 method). Implemented by `SC/analytics/AnalyticsService.kt` → `class AnalyticsService : Analytics` plus `override`. `track()` reads the request through `RequestContextHolder` itself, so no servlet type leaks. If step 5 already moved `DocumentController` (it calls `analyticsService.track()`), this port has to exist by step 5.
  ```kotlin
  package org.migor.feedless.analytics
  interface Analytics {
    suspend fun track()
  }
  ```
- **New** `packages/domain/src/main/kotlin/org/migor/feedless/common/HttpFetcher.kt` (1 method). Implemented by `SC/common/HttpService.kt` → `class HttpService(...) : HttpFetcher`. Remove the `= null` default from the override: Kotlin forbids defaults on overrides, and callers inherit the interface's default. `HttpResponse` is already in `domain` (step 0).
  ```kotlin
  package org.migor.feedless.common
  interface HttpFetcher {
    suspend fun httpGet(url: String, expectedHttpStatus: Int, headers: Map<String, String>? = null): HttpResponse
  }
  ```
- **Extend** `session.TokenIssuer` by 1 method, if step 3 has not added it: `suspend fun decodeJwt(token: String): Jwt`, already implemented by `JwtTokenIssuer.decodeJwt(token: String)` (used by `ReportController`).
- **Reuse** `PipelinePlugins.resolveById` (`ReportUseCase` resolves `ReportPlugin<*>` twice), plus domain `TemplateService`/`MailService`.

### 3. `*Port` removals

None in this step.

### 4. Edits outside moves

- `packages/domain/src/main/kotlin/org/migor/feedless/report/SegmentCreate.kt`:
  ```kotlin
  package org.migor.feedless.report
  data class SegmentCreate(
    val recipientEmail: String,
    val recipientName: String,
    val startingAt: LocalDateTime,
    val interval: ChronoUnit,          // MONTHS or WEEKS
    val near: LatLonPoint? = null,
    val nearDistanceKm: Double? = null,
    val reporterPluginId: String,
  )
  ```
  `ReportUseCase.createReport(repositoryId: RepositoryId, segment: SegmentCreate)`: replace `segment.recipient.email.email` → `recipientEmail`, `.name` → `recipientName`, `segment.when.scheduled.startingAt.toLocalDateTime()` → `startingAt`, `when (segment.when.scheduled.interval) { IntervalUnit.MONTH -> …; IntervalUnit.WEEK -> … }` → `when (segment.interval) { ChronoUnit.MONTHS -> Pair(ChronoUnit.MONTHS, "0 8 L * *"); ChronoUnit.WEEKS -> Pair(ChronoUnit.WEEKS, "0 8 * * 0"); else -> throw IllegalArgumentException("interval ${segment.interval}") }`, the `latLng.near` block → `segment.near?.let { segmentation.copy(contentSegmentLatLon = it, contentSegmentLatLonDistance = segment.nearDistanceKm) }`, `segment.report.plugin.pluginId` → `reporterPluginId`; `sendReportCreatedMail(segment: SegmentCreate)` uses `recipientEmail`. `ReportResolver` gets `private fun SegmentInput.toDomain() = SegmentCreate(recipient.email.email, recipient.email.name, `when`.scheduled.startingAt.toLocalDateTime(), when (`when`.scheduled.interval) { IntervalUnit.MONTH -> ChronoUnit.MONTHS; IntervalUnit.WEEK -> ChronoUnit.WEEKS }, what.latLng?.near?.let { LatLonPoint(it.point.lat, it.point.lng) }, what.latLng?.near?.distanceKm, report.plugin.pluginId)`.
- `packages/domain/src/main/kotlin/org/migor/feedless/annotation/AnnotationCreate.kt`:
  ```kotlin
  package org.migor.feedless.annotation
  data class AnnotationTarget(val documentId: DocumentId?, val repositoryId: RepositoryId?)
  sealed interface AnnotationCreate { val target: AnnotationTarget }
  data class BoolAnnotationCreate(
    override val target: AnnotationTarget,
    val flag: Boolean = false, val upVote: Boolean = false, val downVote: Boolean = false,
  ) : AnnotationCreate
  data class TextAnnotationCreate(override val target: AnnotationTarget, val fromChar: Int, val toChar: Int) : AnnotationCreate
  ```
  `AnnotationUseCase`: `createAnnotation(data: AnnotationCreate): Annotation = when (data) { is BoolAnnotationCreate -> createBoolAnnotation(data.target, data.flag, data.upVote, data.downVote); is TextAnnotationCreate -> createTextAnnotation(data.target, data.fromChar, data.toChar) }`; `deleteAnnotation(id: AnnotationId)`; `resolveReferences(target: AnnotationTarget)` calls `documentGuard.requireWrite(it)`/`repositoryGuard.requireWrite(it)` on the ids. `AnnotationResolver` maps the inputs, keeping today's precedence flag → text → upVote → downVote: target = `AnnotationTarget(where.document?.id?.let { DocumentId(UUID.fromString(it)) }, where.repository?.id?.let { RepositoryId(UUID.fromString(it)) })`, then `annotation.flag?.let { BoolAnnotationCreate(t, flag = it.set) } ?: annotation.text?.let { TextAnnotationCreate(t, it.fromChar, it.toChar) } ?: annotation.upVote?.let { BoolAnnotationCreate(t, upVote = it.set) } ?: annotation.downVote?.let { BoolAnnotationCreate(t, downVote = it.set) } ?: throw IllegalArgumentException("Insufficient data for annotation")`; delete → `annotationUseCase.deleteAnnotation(AnnotationId(data.where.id))`.
- `ReportController`: constructor `(reportUseCase: ReportUseCase, tokenIssuer: TokenIssuer)`, body `injectCapabilitiesFromJwt(tokenIssuer.decodeJwt(deleteReportJwt))`.
- `MailController`: constructor `(reportUseCase: ReportUseCase, templateService: TemplateService)`; `MailTemplateChangeTrackerAuthorized` is already domain (`template/TemplateService.kt:48`).
- `AttachmentController`: constructor `(attachmentUseCase: AttachmentUseCase, httpFetcher: HttpFetcher, analytics: Analytics)`.
- `SC/pipeline/plugins/EventsReportPlugin.kt`: remove the two cut declarations.
- `SC/analytics/AnalyticsService.kt`: `: Analytics`, `override suspend fun track()`. `SC/common/HttpService.kt`: `: HttpFetcher`, `override suspend fun httpGet(url: String, expectedHttpStatus: Int, headers: Map<String, String>?)`.

### 5. Build

`packages/http-api/build.gradle.kts`: the `Jwt` type in `ReportController`, and `JwtValidationException` in step 8, need oauth2-jose. The dependency is versionless and resolves through the Spring Boot starters' platform, the same way `jackson-module-kotlin` already does:
```kotlin
implementation("org.springframework.security:spring-security-oauth2-jose")
```
(Leave this out if `domain` exposes it with `api(...)`.) `graphql-api`/`domain` test dependencies (added in step 0; add here if missing):
```kotlin
testImplementation(libs.spring.boot.test)
testImplementation(libs.kotlinx.coroutines.test)
testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
```

### 6. Verification

```bash
./gradlew compileTestKotlin
./gradlew :packages:domain:test --tests 'org.migor.feedless.annotation.*' --tests 'org.migor.feedless.report.*'
./gradlew :packages:graphql-api:test --tests 'org.migor.feedless.attachment.*' --tests 'org.migor.feedless.annotation.*' --tests 'org.migor.feedless.report.*'
./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test
```
Executed-test count, run from a clean state each time. Stale XML from the moved tests stays in `packages/server-core/build/test-results` and would be counted twice, so run `trash packages/*/build/test-results` first:
```bash
cat packages/*/build/test-results/test/TEST-*.xml | grep -o '<testsuite [^>]*>' | sed -E 's/.* tests="([0-9]+)" skipped="([0-9]+)".*/\1 \2/' | awk '{t+=$1;s+=$2} END{print "tests="t" skipped="s" executed="t-s}'
```

### 7. Risks

- `internal fun X.toDto()` in resolvers (`AttachmentResolver.kt:73`, `ReportResolver`) becomes invisible to `server-core`. If `server-core` main or tests use one, compilation fails; make it public or move the caller.
- `MailController` now injects `TemplateService` by interface. `FreemarkerTemplateService` is the only implementation, `@Profile(AppLayer.service)`; a `api`-only profile set without `service` fails exactly as it did before.
- `ReportResolver` stays `@Profile("${AppProfiles.DEV_ONLY} & ${AppProfiles.report} & ${AppLayer.api}")`; do not "fix" it here.
- `SecurityConfig`'s permit rules for `/reports/delete/**` and `/mail/forwarding/allow/**` use `ApiUrls`; the FQN is unchanged, so no rule changes.
- `AttachmentGuard`'s methods are all `TODO()` (so `AttachmentUseCase.findById` throws `NotImplementedError`); the move keeps that as it is.

---

## Step 7 — feed, license, status, agent, scrape, plugins

### 1. Moves

Main (15 files):

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/feed/FeedService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/FeedService.kt` | **yes**, large (§4) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/feed/FeedMessage.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/feed/FeedMessage.kt` | none (only `FeedService` uses it) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/license/LicenseUseCase.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/license/LicenseUseCase.kt` | code none; deps (§5) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/license/BuildTimestamp.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/license/BuildTimestamp.kt` | none (also used by `ServerStatusService`) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/status/ServerStatusService.kt` | `packages/domain/src/main/kotlin/org/migor/feedless/status/ServerStatusService.kt` | **yes**: `ObjectProvider<AgentRegistry>` → `ObjectProvider<AgentDirectory>`; drop `: ServerStatusPort`/`override` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/feed/FeedResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/feed/FeedResolver.kt` | none |
| `packages/server-core/src/main/kotlin/org/migor/feedless/license/LinceseResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/license/LinceseResolver.kt` | none (name kept; renaming is out of scope) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/agent/AgentResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/agent/AgentResolver.kt` | **yes**: `AgentService` → `AgentGateway` + `AgentDirectory` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/pipeline/PluginResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/pipeline/PluginResolver.kt` | **yes**: `PluginService` → `PipelinePlugins.describeAll()` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/scrape/ScrapeQueryResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/scrape/ScrapeQueryResolver.kt` | **yes**: `ScrapeService` → `Scraper` or `ScrapeRunner` (§2) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/graphql/ServerConfigResolver.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/api/graphql/ServerConfigResolver.kt` | **yes**: `AnalyticsService` → `Analytics` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/session/ProductsAuthProperties.kt` | `packages/graphql-api/src/main/kotlin/org/migor/feedless/session/ProductsAuthProperties.kt` | none (only `ServerConfigResolver` uses it; `@Configuration @ConfigurationProperties("app.auth.products")` is still picked up by the app's package scan) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/feed/FeedController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/feed/FeedController.kt` | **yes** (§4) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/cli/CliInstallScriptController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/cli/CliInstallScriptController.kt` | **yes**: `PropertyService` → `AppConfig` |
| `packages/server-core/src/main/kotlin/org/migor/feedless/cli/FeedctlBaseUrlValidator.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/cli/FeedctlBaseUrlValidator.kt` | none |

Resource move (recommended, because `LicenseUseCase` owns it): `packages/server-core/src/main/resources/certs/feedless.pub` → `packages/domain/src/main/resources/certs/feedless.pub`, plus `packages/server-core/src/main/resources/certs/.gitignore` → `packages/domain/src/main/resources/certs/.gitignore`. `ClassPathResource("/certs/feedless.pub")` resolves from any jar on the boot classpath.

New files: `packages/domain/src/main/kotlin/org/migor/feedless/feed/FeedParser.kt`, `.../scrape/WebToFeed.kt`, `.../pipeline/ItemFilter.kt`, `.../pipeline/PluginDescriptor.kt`, `.../agent/AgentDirectory.kt`, `.../status/ServerStatus.kt` (the `ServerStatus` data class cut from `ServerStatusPort.kt`), `packages/graphql-api/src/main/kotlin/org/migor/feedless/agent/AgentGateway.kt`, and conditionally `packages/graphql-api/src/main/kotlin/org/migor/feedless/scrape/ScrapeRunner.kt`.

Tests (6 files):

| from | to | edits |
|---|---|---|
| `packages/server-core/src/test/kotlin/org/migor/feedless/feed/FeedServiceTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/feed/FeedServiceTest.kt` | **yes**: mocks of `WebToFeedTransformer`/`FeedParserService`/`ScrapeService`/`ScrapeOutput`/`HttpFetchOutput`/`CompositeFilterPlugin`/`JwtTokenIssuer`/`AuthService`/`PropertyService` → `WebToFeed`/`FeedParser`/`Scraper.fetch`/`ItemFilter`/`TokenIssuer`/`AppConfig`; drop the `authService` argument; `filterPlugin.fromJson(...)` stubs go away |
| `packages/server-core/src/test/kotlin/org/migor/feedless/license/LicenseUseCaseTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/license/LicenseUseCaseTest.kt` | none; it is `@Tag("unstable")`, so the `domain` test task needs the same tag exclusion (§5) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/status/ServerStatusServiceTest.kt` | `packages/domain/src/test/kotlin/org/migor/feedless/status/ServerStatusServiceTest.kt` | **yes**: `ObjectProvider<AgentRegistry>` → `ObjectProvider<AgentDirectory>` |
| `packages/server-core/src/test/kotlin/org/migor/feedless/agent/AgentResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/agent/AgentResolverTest.kt` | none (tests `internal fun Agent.toDto()`) |
| `packages/server-core/src/test/kotlin/org/migor/feedless/pipeline/PluginResolverTest.kt` | `packages/graphql-api/src/test/kotlin/org/migor/feedless/pipeline/PluginResolverTest.kt` | **yes**: today its 8 tests call `toDto()` on real `CompositeFilterPlugin`, `FeedPlugin`, … (server-core classes). Rewrite as `PluginDescriptor(...).toDto()` cases (entity/fragment/listed); add the per-plugin "is fragment transformer" assertions as `describeAll()` tests to `packages/server-core/src/test/kotlin/org/migor/feedless/pipeline/PluginServiceTest.kt`, so the total does not drop |
| `packages/server-core/src/test/kotlin/org/migor/feedless/cli/FeedctlBaseUrlValidatorTest.kt` | `packages/http-api/src/test/kotlin/org/migor/feedless/cli/FeedctlBaseUrlValidatorTest.kt` | none |

Edited in place: `packages/http-api/src/test/kotlin/org/migor/feedless/http/StatusHttpControllerTest.kt` (mock `ServerStatusService` instead of `ServerStatusPort`).

Staying in server-core (their subject stays, or they need the full app): `SCT/feed/FeedControllerIntTest.kt` (RANDOM_PORT full context), `SCT/feed/DateClaimerTest.kt`, `SCT/feed/TimeClaimerTest.kt`, `SCT/agent/AgentSyncExecutorTest.kt`, `SCT/agent/StatelessAgentRegistryTest.kt`, `SCT/agent/StatefulAgentRegistryIntTest.kt` (its subject `StatefulAgentRegistry` is server-core infrastructure, so it does not qualify for `jpa-data`), `SCT/analytics/AnalyticsServiceTest.kt`, `SCT/pipeline/PluginServiceTest.kt`, `SCT/pipeline/plugins/*`, `SCT/service/ScrapeServiceIntTest.kt`, `SCT/transform/*`, `SCT/api/graphql/AuthenticationIntTest.kt`, `SCT/api/graphql/QueryResolverIntTest.kt`, `SCT/api/graphql/ScrapeQueryResolverIntTest.kt`, `SCT/cli/CliInstallScriptControllerIntTest.kt`, `SCT/cli/CliInstallScriptControllerWithFixtureIntTest.kt`, `SCT/cli/CliInstallScriptControllerWithUnsafeGatewayUrlIntTest.kt`, `SCT/community/**`, `SCT/mail/MailPropertiesTest.kt`. The `@MockitoBean(types = [ServerConfigResolver::class, DocumentController::class, SessionResolver::class, …])` references in these tests still resolve, because `server-core`'s test classpath has `graphql-api` and `http-api`. Persistence-only tests for `jpa-data`: none.

### 2. Ports

- **New** `packages/domain/src/main/kotlin/org/migor/feedless/feed/FeedParser.kt` (1). Implemented by `SC/feed/FeedParserService.kt` → `: FeedParser`, `override`.
  ```kotlin
  interface FeedParser { suspend fun parseFeedFromUrl(url: String): JsonFeed }
  ```
- **New** `packages/domain/src/main/kotlin/org/migor/feedless/scrape/WebToFeed.kt` (1). Implemented by `SC/scrape/WebToFeedTransformer.kt` with one small adapter method. It takes the HTML as a string, so `domain` needs neither jsoup nor the `HtmlUtil`/`FeedUtil` objects, which exist under the same FQN in both `server-core` and `feed-parser`. Adapter body: `val document = HtmlUtil.parseHtml(html, url); getFeedBySelectors(selectors, document, URI(url), logger).also { it.title = StringUtils.trimToNull(document.title()) ?: "Feed" }`.
  ```kotlin
  interface WebToFeed { suspend fun webToFeed(html: String, url: String, selectors: GenericFeedSelectors, logger: LogCollector): JsonFeed }
  ```
- **New** `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/ItemFilter.kt` (1). Implemented by `SC/pipeline/plugins/CompositeFilterPlugin.kt` → `class CompositeFilterPlugin : FilterEntityPlugin<CompositeFilterPluginParams?>, ItemFilter`. Its existing `filterEntity` override has exactly this signature (`CompositeFilterPluginParams = List<ItemFilterParams>`), so one method satisfies both interfaces.
  ```kotlin
  interface ItemFilter { suspend fun filterEntity(item: JsonItem, params: List<ItemFilterParams>?, index: Int, logCollector: LogCollector): Boolean }
  ```
- **New** `packages/domain/src/main/kotlin/org/migor/feedless/agent/AgentDirectory.kt` (2). Implemented by making `SC/agent/AgentRegistry.kt` `interface AgentRegistry : AgentDirectory` and deleting its now-inherited `countConnected()`/`findAllByOwnerIdOrOpenInstanceIsTrue()` declarations; `StatefulAgentRegistry` and `StatelessAgentRegistry` need no change. `@ConditionalOnMissingBean` guarantees exactly one bean; `AgentService` does not implement the port.
  ```kotlin
  interface AgentDirectory {
    suspend fun countConnected(): Int
    suspend fun findAllByOwnerIdOrOpenInstanceIsTrue(userId: UserId?): List<Agent>
  }
  ```
- **New, in `graphql-api`, not in `domain`** — `packages/graphql-api/src/main/kotlin/org/migor/feedless/agent/AgentGateway.kt` (2). Implemented by `SC/agent/AgentService.kt` → `: AgentGateway`. The agent protocol consists of generated types (`RegisterAgentInput`, `AgentEvent`, `ScrapeResponseInput`, `AgentRef.emitter: FluxSink<AgentEvent>`), so a domain port would have to duplicate the schema. `server-core → graphql-api` is an existing dependency, so `server-core` can implement a `graphql-api` interface.
  ```kotlin
  interface AgentGateway {
    suspend fun registerAgent(data: RegisterAgentInput): Publisher<AgentEvent>
    suspend fun handleScrapeResponse(harvestJobId: String, scrapeResponse: ScrapeResponseInput)
  }
  ```
- **Conditional, in `graphql-api`** — `packages/graphql-api/src/main/kotlin/org/migor/feedless/scrape/ScrapeRunner.kt` (1). `ScrapeQueryResolver` needs `scrape(source, logCollector): ScrapeOutput` and maps `outputs[].fetch.debug` (`FetchActionDebugResponse`), `fragment.fragments` (`ScrapeExtractFragment`) and `fragment.feeds` (`ScrapedFeeds`), all generated types. If step 5 made `ScrapeOutput` domain-safe and `Scraper` exposes `scrape`, reuse `Scraper` and skip this. Otherwise move `ScrapeOutput`/`ScrapeActionOutput`/`HttpFetchOutput` (from `SC/scrape/ScrapeContext.kt`) and `FragmentOutput` (from `SC/pipeline/FragmentTransformerPlugin.kt`) to `packages/graphql-api/src/main/kotlin/org/migor/feedless/scrape/ScrapeOutput.kt`, and let `ScrapeService : ScrapeRunner`.
  ```kotlin
  interface ScrapeRunner { suspend fun scrape(source: Source, logCollector: LogCollector): ScrapeOutput }
  ```
- **Extend `scrape.Scraper`** by 1 method, used by `FeedService.fetchFeedFromUrl`: `suspend fun fetch(source: Source, logCollector: LogCollector): HttpResponse`. Implementation in `ScrapeService`: `scrape(source, logCollector).outputs.find { it.fetch != null }!!.fetch!!.response`.
- **Extend `pipeline.PipelinePlugins`** by 1 method, used by `PluginResolver`: `suspend fun describeAll(): List<PluginDescriptor>`, with `packages/domain/src/main/kotlin/org/migor/feedless/pipeline/PluginDescriptor.kt` = `data class PluginDescriptor(val id: String, val name: String, val listed: Boolean, val fragmentTransformer: Boolean)`. Implementation in `PluginService`: `findAll().map { PluginDescriptor(it.id(), it.name(), it.listed(), it is FragmentTransformerPlugin) }`. `FragmentTransformerPlugin` cannot move to `domain`, because `FragmentOutput` carries generated types.
- **Extend `session.TokenIssuer`** by 2 methods, if missing: `suspend fun decodeJwt(token: String): Jwt` (added in step 6) and `fun createJwtForAnonymousFeed(host: String, id: RepositoryClaimId): Jwt`, already implemented by `JwtTokenIssuer`.
- **Ensure on `common.AppConfig`**: `val appHost: String` (`FeedService`) and `val apiGatewayUrl: String` (`CliInstallScriptController`); both already exist on `PropertyService`.
- **Reuse `analytics.Analytics`** (step 6) in `FeedController` and `ServerConfigResolver`.
- **`session.UserAuthenticator`: not created (0 methods).** `FeedService` injects `authService` but never calls it; `AgentService`, the only other user (`findBySecretKeyValue`, `updateLastUsed`), stays in `server-core`.

### 3. `*Port` removals

- `packages/domain/src/main/kotlin/org/migor/feedless/status/ServerStatusPort.kt`: delete it. Move `data class ServerStatus` to `packages/domain/src/main/kotlin/org/migor/feedless/status/ServerStatus.kt` (unchanged, KDoc kept). Consumers switch to the class: `packages/http-api/src/main/kotlin/org/migor/feedless/http/StatusHttpController.kt` (`private val serverStatus: ServerStatusService`, import `org.migor.feedless.status.ServerStatusService`) and `packages/http-api/src/test/kotlin/org/migor/feedless/http/StatusHttpControllerTest.kt` (mock `ServerStatusService`). `config/SecurityConfig.kt` imports `StatusHttpController` only for the `PUBLIC_STATUS_PATH` constant; unchanged.
- The only `*Port` still left in `domain` afterwards is `session/SessionTokenPort.kt`, used by `SessionResolver` and implemented by `SessionTokenPortAdapter`. It is a session outbound port, not an `/api/v1` port; keep it.

### 4. Edits outside moves

- `FeedService` (domain) constructor becomes `(appConfig: AppConfig, webToFeed: WebToFeed, feedParser: FeedParser, scraper: Scraper, documentUseCase: DocumentUseCase, documentRepository: DocumentRepository, itemFilter: ItemFilter, tokenIssuer: TokenIssuer, repositoryClaimRepository: RepositoryClaimRepository, repositoryRepository: RepositoryRepository, featureService: FeatureService, sourceRepository: SourceRepository)`; `authService` is dropped (unused). Call-site changes:
  - `propertyService.appHost` → `appConfig.appHost`.
  - `fetchFeedFromUrl`: `val response = scraper.fetch(source, LogCollector()); val feed = webToFeed.webToFeed(response.responseBody.toString(UTF_8), url, selectors, LogCollector())`, then set `feedUrl`/`websiteUrl` as today; the title is set inside the adapter.
  - `feedParserService.parseFeedFromUrl` → `feedParser.parseFeedFromUrl`.
  - `filterPlugin.filterEntity` → `itemFilter.filterEntity`.
  - `jwtTokenIssuer.*` → `tokenIssuer.*`.
  - `fun getRepository(repositoryId: String): ResponseEntity<String>` is spring-web and only `FeedController` calls it: move it verbatim into `FeedController` as a private function.
  - `FeedUtil.toURI(...)` (3 calls): `domain` sees neither copy of `FeedUtil`; paste its `toURI` body as a private function in `FeedService`, unless step 0 already consolidated `FeedUtil` into `domain`.
  - `t is TooManyConnectionsPerHostException`: keep it verbatim and add async-http-client as `compileOnly` plus `testImplementation` (§5).
  - `appendNotifications(..., jwt: Jwt?)` stays; `domain` has oauth2-jose.
- `FeedController` (http-api) constructor becomes `(feedExporter: FeedExporter, feedService: FeedService, meterRegistry: MeterRegistry, analytics: Analytics)`. Add a private copy of `toFullUrlString(request: HttpServletRequest)` from `SC/analytics/AnalyticsService.kt:32`, since `AnalyticsService` keeps its own; add the moved `getRepository`; `analyticsService.track()` → `analytics.track()`.
- `ServerConfigResolver`: `analyticsService: AnalyticsService` → `analytics: Analytics`.
- `CliInstallScriptController`: `propertyService.apiGatewayUrl` → `appConfig.apiGatewayUrl`.
- `ServerStatusService`: `private val agentDirectory: ObjectProvider<AgentDirectory>`, `agentDirectory.ifAvailable?.countConnected() ?: 0`.
- `AgentResolver` constructor becomes `(agentGateway: AgentGateway, agentDirectory: AgentDirectory, capabilityService: CapabilityService)`; `registerAgent` → `agentGateway.registerAgent(data)`, `submitAgentData` → `agentGateway.handleScrapeResponse(...)`, `agents` → `withContext(Dispatchers.IO) { agentDirectory.findAllByOwnerIdOrOpenInstanceIsTrue(userId()) }`, which keeps `AgentService.findAllByUserId`'s dispatcher. `AgentService`: `: AgentGateway`, `override` on both methods; `findAllByUserId` then has no callers and may be deleted.
- `PluginResolver`: `@Autowired lateinit var pluginsService: PluginService` → `pipelinePlugins: PipelinePlugins`; `pipelinePlugins.describeAll().map { it.toDto() }` with `internal fun PluginDescriptor.toDto() = Plugin(id = id, name = name, listed = listed, type = if (fragmentTransformer) PluginType.fragment else PluginType.entity)`.
- `ScrapeQueryResolver`: `scrapeService` → `Scraper` or `ScrapeRunner` (see §2); the private mapping functions move with it unchanged.
- Step-3 leftover to check: `SCT/cli/CliInstallScriptControllerIntTest.kt` imports `org.migor.feedless.group.GroupUseCasePort`, which step 3 removes.

### 5. Build

`packages/domain/build.gradle.kts` (versionless entries rely on the Spring Boot BOM that step 0 imports):
```kotlin
implementation(libs.nimbus.jose.jwt)                       // LicenseUseCase: RSAKey, JWSObject
implementation(libs.commons.lang3)                         // LicenseUseCase, FeedService
implementation(libs.commons.text)                          // LicenseUseCase: WordUtils
implementation("jakarta.annotation:jakarta.annotation-api") // @PostConstruct in LicenseUseCase
compileOnly(libs.async.http.client)                        // FeedService: TooManyConnectionsPerHostException
testImplementation(libs.async.http.client)

tasks.test {
  useJUnitPlatform { excludeTags("unstable", "nlp") }      // LicenseUseCaseTest is @Tag("unstable"), as in server-core
}
```
`packages/http-api/build.gradle.kts`:
```kotlin
implementation(project(":packages:feed-parser"))   // FeedExporter; feed-parser api-depends on domain, no cycle
implementation("io.micrometer:micrometer-core")    // MeterRegistry, Tag, @Timed in FeedController
implementation(libs.commons.lang3)                 // StringUtils, BooleanUtils in FeedController
```
`packages/graphql-api/build.gradle.kts`: nothing new beyond step 0. `org.reactivestreams.Publisher` comes with the DGS starter, and `Environment`/`@ConfigurationProperties` with `spring-boot-starter-web`.

### 6. Verification

```bash
./gradlew compileTestKotlin
./gradlew :packages:domain:test --tests 'org.migor.feedless.feed.*' --tests 'org.migor.feedless.status.*'
./gradlew :packages:graphql-api:test --tests 'org.migor.feedless.agent.*' --tests 'org.migor.feedless.pipeline.*'
./gradlew :packages:http-api:test --tests 'org.migor.feedless.cli.*' --tests 'org.migor.feedless.http.StatusHttpControllerTest'
./gradlew :packages:server-core:test --tests 'org.migor.feedless.feed.FeedControllerIntTest' --tests 'org.migor.feedless.cli.*' --tests 'org.migor.feedless.api.graphql.*' --tests 'org.migor.feedless.pipeline.PluginServiceTest'
./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test
```
Then run the executed-test count from step 6 (after `trash packages/*/build/test-results` and a full run). `LicenseUseCaseTest` counts in neither run, because it is excluded before and after.

### 7. Risks

- `@Cacheable(CacheNames.FEED_LONG_TTL)` on `FeedService`'s suspend functions needs the class proxied, hence open; without `kotlin.spring` in `domain` caching silently stops.
- `FeedUtil` and `HtmlUtil` are declared twice under the same FQN (`server-core` and `feed-parser`). Never add a third copy to `domain`; the `WebToFeed` adapter keeps them out.
- `AgentGateway` and `ScrapeRunner` break the spec's "ports live in `domain`" rule on purpose. `server-core` implements `graphql-api` interfaces there; `graphql-api` still never depends on `server-core`.
- Kotlin `internal` `toDto` in `AgentResolver`/`PluginResolver`: tests must stay in `graphql-api`. Anything in `server-core` that called them fails to compile.
- `LicenseUseCase.onInit()` (`@PostConstruct`) throws on a missing `APP_BUILD_TIMESTAMP`; behaviour unchanged, but a domain test that starts a Spring context with it will fail the same way.
- `ProductsAuthProperties` has no profile; it moves as-is. Do not add a profile, since `SecurityConfigIntTest` expects the bean.
- `AnalyticsService` is `@Profile("${AppProfiles.analytics} & ${AppLayer.service}")`, while `FeedController`/`ServerConfigResolver` require an `Analytics` bean exactly as they required `AnalyticsService`; wiring is unchanged.

---

## Step 8 — wrap-up

### 1. Moves

Main (2 files), no tests reference them:

| from | to | edits |
|---|---|---|
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/http/HttpExceptionHandler.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/api/http/HttpExceptionHandler.kt` | none. Needs `AccessDeniedException` (starter-security, present), `JwtValidationException` (oauth2-jose, added in step 6) and `HostOverloadingException` (domain) |
| `packages/server-core/src/main/kotlin/org/migor/feedless/api/http/AppErrorController.kt` | `packages/http-api/src/main/kotlin/org/migor/feedless/api/http/AppErrorController.kt` | none. Separable: only `ErrorController`, servlet request/response and `sendRedirect("/")` |

The package stays `org.migor.feedless.api.http`, outside `HttpApiAutoConfiguration`'s `@ComponentScan("org.migor.feedless.http")`. The app's package scan picks both up; `http-api`'s own `HttpApiTestApplication` (package `org.migor.feedless.http`) does not, so the existing `HttpApiExceptionHandler*Test`s see no second advice.

### 2–3. Ports / `*Port` removals

None. Sanity check that only `session/SessionTokenPort.kt` remains:
```bash
grep -rlE 'interface \w+Port\b' packages/domain/src/main packages/http-api/src/main packages/graphql-api/src/main
```

### 4. Edits outside moves (docs)

`AGENTS.md` module table (lines 38–55), replacement rows:
- `server-core`: `The Spring Boot application that assembles the modules: security composition (SecurityConfig, JWT filters, TokenAuthenticator), scheduler executors, the scraping pipeline and plugins, infrastructure behind domain's outbound ports (JwtTokenIssuer, PropertyService, PluginService, ScrapeService, AnalyticsService, …), ThrottleAspect, TestingEndpoint. Start at FeedlessApplication.kt.` Recount the "214 Kotlin files" figure or drop it: `find packages/server-core/src/main -name '*.kt' | wc -l`.
- `domain`: `Domain types, repository interfaces, all use cases and guards, outbound ports (TokenIssuer, AppConfig, PipelinePlugins, Scraper, Analytics, …), the security bridge (injectCapabilitiesFrom*), @Throttled, AppProfiles/AppLayer. Spring annotations allowed; no generated GraphQL types, no data.jpa.`
- `graphql-api`: `schema.graphqls (**contract**: generates the Kotlin DGS types and every TS client) plus all DGS resolvers, ProductDataLoader, GraphQL mappers (MapStruct via kapt), GraphQLExceptionHandler, GraphqlConfig. Never depends on server-core.`; Build column `Gradle (codegen, kapt)`.
- `http-api`: `openapi.yaml for /api/v1 (**contract**: generates Kotlin Spring interfaces and the feedctl Go client) plus the /api/v1 controllers and the other web controllers (feed export, repository feeds, documents, attachments, payment callbacks, CLI install script, mail and report links), HttpExceptionHandler, AppErrorController. Never depends on server-core.`
- `jpa-data`: append `Persistence integration tests (Testcontainers/PostGIS; PostgreSQLExtension is its test fixture).`

`AGENTS.md` Critical Rule 2 (line 10): append `graphql-api and http-api now also hold hand-written resolvers, controllers and mappers under src/main/kotlin; only their build/generated/** output belongs to the generators.` Also recommended, though the spec does not ask for it: the Essential Commands row `./gradlew :packages:server-core:test` → `./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test` (jpa-data and server-core need Docker).

`docs/rules/kotlin-spring.md`:
- L53: `— implement the interfaces in server-core` → `— implemented by the controllers in http-api`.
- L58: `Both contract modules hold no hand-written Kotlin at all.` → `Both contract modules also hold hand-written adapters (resolvers, controllers, mappers); only build/generated/** is generator output.`
- L62–77 (`## Package and naming conventions`): L64 → `Code is organised by feature package (document/, repository/, source/, …), and the same package spans modules: use cases and guards in domain, resolvers in graphql-api, controllers in http-api, infrastructure in server-core.` Add a Module column to the table: `*Resolver` graphql-api; `*Controller` http-api; `*Guard` domain (layer: `serviceLayer`; the current `apiLayer` is wrong, e.g. `GroupGuard` is `${AppLayer.service}`); `*UseCase` domain; `*Service` domain, or server-core when it is infrastructure behind a port; `*Repository` interface in domain, implementation in jpa-data. Add a row `outbound port — interface in domain named after the capability, implemented in server-core`.
- L82: append `Persistence integration tests live in jpa-data.`
- L84–88 (`## Tests`): replace `Run: ./gradlew :packages:server-core:test.` with the five-module command; recount `(20 of them)` with `grep -rl '@SpringBootTest' packages/*/src/test | wc -l`; add `Mother is a domain test fixture: testImplementation(testFixtures(project(":packages:domain"))).`

`docs/tasks.md` (German, section `## Plattform und Architektur`, line 92), add three bullets:
- `Profil-Gating vereinheitlichen: PlanGuard hängt an AppLayer.repository, einige Beans an service & repository, teils && statt &, Services ohne Profil.`
- `LinceseResolver in LicenseResolver umbenennen; LicenseUseCase in Provider und Use Case aufteilen.`
- `TestingEndpoint aus server-core herauslösen.`

### 5. Build

None new: `spring-security-oauth2-jose` in `http-api` comes from step 6.

### 6. Verification

```bash
./gradlew :packages:domain:test :packages:graphql-api:test :packages:http-api:test :packages:jpa-data:test :packages:server-core:test
./gradlew lint test
# Sanity: nothing entry-point-shaped or use-case-shaped left in server-core (expect no output)
find packages/server-core/src/main -name '*Resolver.kt' -o -name '*UseCase*.kt' -o -name '*Guard.kt' -o -name '*DataLoader.kt' -o \( -name '*Controller.kt' -not -name 'TestingEndpoint.kt' \)
# e2e smoke against a freshly built core image (spec: after steps 5 and 8); needs Docker and Go
./gradlew :packages:server-core:buildAmdDockerImage   # runs test + bootJar, tags damoeb/feedless:core-latest and core-<7-char hash>; linux/amd64, emulated on Apple silicon
FEEDCTL_E2E_CORE_IMAGE=damoeb/feedless:core-$(git rev-parse --short=7 HEAD) ./gradlew :packages:cli:e2eTest   # packages/cli/e2e, go test -tags e2e; browser-automation image defaults to damoeb/feedless:browserautomation-latest (FEEDCTL_E2E_BROWSERAUTOMATION_IMAGE)
```
`dockerImageTag`/`feedlessVersion` come from `gradle.properties`. Finish with the executed-test count from step 6.

What legitimately stays in `server-core` after step 8: `FeedlessApplication.kt`, `AppInitListener.kt`; `config/*` (SecurityConfig, CacheConfig, CustomDgsWebSocketConfig, DatabaseConfig, FlywayConfig, PostStartupVerificationService, CustomSQL*); `session/*` infrastructure (JwtTokenIssuer, JwtRequestFilter, TokenAuthenticator, CookieProvider, AuthService/Stateful/Stateless, SessionTokenPortAdapter, AuthenticationHttpSessionHandshakeInterceptor, AuthWebsocketRepository); `http/*` (HttpApiJwtFilter, HttpApiRequestContextAttributes, HttpApiServletInvocableHandlerMethod, HttpApiWebMvcConfiguration); `api/throttle/{IpThrottleService,ThrottleAspect}.kt`, `api/ApiParams.kt`, `api/http/TestingEndpoint.kt`; `agent/{AgentService,AgentRef,AgentResponse,AgentRegistry,StatefulAgentRegistry,StatelessAgentRegistry,AgentSyncExecutor}.kt`; `analytics/AnalyticsService.kt`; `common/{HttpService,PropertyService,PdfService,CleanupExecutor}.kt`; `community/**`; `data/Seeder*.kt`; `feed/{FeedParserService,DateClaimer,TimeClaimer}.kt`, `feed/discovery/*`; `pipeline/{PluginService,*JobExecutor,*PipelineService?}` and `pipeline/plugins/*`, `pipeline/transformer/*`; `scrape/*`; `secrets/{EncryptionConverter,HashConverter}.kt`; `transport/Telegram*`; `repository/{QueuedHarvestExecutor,RepositoryHarvesterExecutor,AnalyticsSyncExecutor,SourceDryRunner,GithubService}.kt`; `report/ReportJobExecutor.kt`; `util/*`.

### 7. Risks

- Two `@ControllerAdvice`s: `HttpApiExceptionHandler` (`@Order(HIGHEST_PRECEDENCE)`, `basePackages = ["org.migor.feedless.http"]`) and the global `HttpExceptionHandler`. Moving files keeps the scoping only because every moved controller keeps its feature package. Never relocate a moved controller into `org.migor.feedless.http`, or its errors switch to the `/api/v1` error format.
- `AppErrorController` claims `/error` app-wide; it must stay out of `HttpApiAutoConfiguration`'s scan, or `http-api`'s MockMvc tests redirect errors to `/`.
- The e2e image build runs the whole `server-core` test suite first (`buildAmdDockerImage` depends on `build`); budget for it, or build the image with `docker build` after `bootJar`.
