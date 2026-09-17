# Per-use-case properties: replace `PropertyService` and every `@Value`

**Status:** design, approved in conversation 2026-09-15. Lands on `feature/per-use-case-properties` (branched from `develop`) as one PR against `develop`.

**Goal:** configuration reaches code only through small, immutable `@ConfigurationProperties` data classes that live next to the code that reads them. The global `PropertyService` (already `@Deprecated("use dedicated properties")`), the `domain` port `AppConfig` it implements, and all 31 `@Value` injections in 17 main-source files across `domain`, `graphql-api`, `http-api`, `server-core`, `stripe-payments` and `mail-adapter` go away.

**Non-goal:** renaming configuration. Every existing property key and every `APP_*` / `STRIPE_*` / `MAILGUN_KEY` env var keeps working; no deployment changes. Aligning key names is a follow-up (see the end).

## Why

- `PropertyService` binds all of `app.*` into one bean that every consumer depends on, so a session service, a scraper and the seeder share one class. Of its 11 fields, `dateFormat`, `timeFormat` and `webToFeedVersion` are never read, `timezone` is read around it, `anonymousEmail` is a constant, and only `apiGatewayUrl` and `appHost` are genuinely shared.
- `@Value` scatters key strings over the code base, and the same value ends up read in different ways:
  - the git commit is `app.git.commit` in `AppInitListener`, `APP_GIT_COMMIT` with a default in `ServerStatusService`, and `APP_GIT_COMMIT` without one in `ServerConfigResolver`;
  - `auth.token.anonymous.validForDays` and a `default.` copy of it are injected and parsed twice, in `JwtTokenIssuer` and in `StatefulAuthService`;
  - `rootEmail` / `rootSecretKey` reach `Seeder` through `PropertyService` and `StatelessAuthService` through `@Value`.
- Two of these are bugs today:
  - `MailGatewayConfig` injects `@Value("MAILGUN_KEY")` without `${}`, so the Mailgun client authenticates with the literal string `"MAILGUN_KEY"` whenever Mailgun is enabled.
  - `application-prod.yaml` sets `app.defaultDateFormat` / `app.defaultTimeFormat`, which nothing binds.

## Decisions

- **Each consumer owns its properties class, in its own module and package.** `domain` classes use `@ConfigurationProperties` directly; `spring-boot` already reaches `domain` through `spring-boot-starter-validation` and becomes an explicit dependency. Rejected: plain `domain` types bound centrally in `server-core` (rebuilds a central config file) and one port interface per use case (twice the types, tests keep mocking getters).
- **Only `PublicUrls` and `BuildInfo` are shared across use cases.** Copying `apiGatewayUrl` into every use case's class would rebuild the global config field by field.
- **Keys and env vars stay; yaml aliases bridge prefixes.** Where a value lives under a bare env var or a foreign prefix, `application.yaml` gains an alias under the class's prefix that resolves the legacy key, so the legacy key and env var keep working. The aliases are also where a later key rename happens.
- **`@ConfigurationPropertiesScan` on `FeedlessApplication`** registers every class; it scans `org.migor.feedless`, which covers all modules. The Boot 4.1.1 scanner is created with the `Environment`, so `@Profile` on a properties class is evaluated.

## Properties classes

| Class | Module · package | Binds | Replaces |
|---|---|---|---|
| `PublicUrls` | `domain` · `common` | `app.apiGatewayUrl`, `app.appHost` | `AppConfig` and its `PropertyService` implementation; `@Value` in `HttpService`, `TelegramBotService` |
| `BuildInfo` | `domain` · `status` | `app.build.version`, `app.build.commit`, `app.build.timestamp` (aliases) | `@Value` in `AppInitListener`, `ServerStatusService`, `LicenseUseCase` (build timestamp), `LinceseResolver`, `ServerConfigResolver`, `HttpApiVersionHeaderFilter` |
| `LocaleProperties` | `domain` · `common` | `app.defaultLocale` as `Locale`, `app.timezone` | `PropertyService.locale` (`WebToFeedTransformer`), `@Value` in `AppInitListener` |
| `ReportProperties` | `domain` · `report` | `app.report.subscription-mode` as `ReportSubscriptionMode`, `app.report.sender` (alias) | `@Value` in `ReportUseCase`; `ReportSubscriptionMode.parse` |
| `LicenseProperties` | `domain` · `license` | `app.license.key`, `app.license.pem-file` (aliases) | `@Value` in `LicenseUseCase` |
| `SessionProperties` | `server-core` · `session` | `app.jwtSecret`, `app.whitelistedHosts` as `List<String>`, `app.auth.anonymous-token-valid-for` as `Duration` in days (alias) | `PropertyService.jwtSecret`; `@Value` and duplicate parsing in `JwtTokenIssuer`, `StatefulAuthService` |
| `RootUserProperties` | `server-core` · `session` | `app.rootEmail`, `app.rootSecretKey` | `PropertyService` in `Seeder`; `@Value` in `StatelessAuthService` |
| `WebSecurityProperties` | `server-core` · `config` | `app.cors.allowedOrigins`, `app.actuatorPassword` | `@Value` in `SecurityConfig` |
| `PrivacyProperties` | `server-core` · `pipeline.plugins` | `app.privacy.blacklisted-domains` as `List<String>` (alias) | `@Value` in `PrivacyPlugin`; `doubleclick.net` stays a built-in entry |
| `AnalyticsProperties` | `server-core` · `analytics` | `app.analytics.*` | the bound fields on `AnalyticsService`, which stops being a properties holder |
| `CliInstallProperties` | `http-api` · `cli` | `app.cli.installScriptLocation` | `@Value` in `CliInstallScriptController` |
| `StripeProperties` | `stripe-payments` | `stripe.api-key`, `stripe.webhook-secret` | `@Value` in `StripeUseCase` |
| `MailgunProperties` | `mail-adapter` | `app.mail.mailgun-key` (alias) | `@Value("MAILGUN_KEY")` in `MailGatewayConfig` |

`TelegramProperties`, `ProductsAuthProperties` and `MailGatewayProperties` keep their keys and move to the conventions below.

### Yaml aliases

Added to `packages/server-core/src/main/resources/application.yaml`; the right-hand side is the legacy key or env var, unchanged:

```yaml
app:
  build:
    version: ${app.version}
    commit: ${APP_GIT_COMMIT:${app.git.commit:unknown}}
    timestamp: ${APP_BUILD_TIMESTAMP:}
  license:
    key: ${APP_LICENSE_KEY:}
    pem-file: ${APP_PEM_FILE:}
  privacy:
    blacklisted-domains: ${APP_BLACKLISTED_DOMAINS:}
  report:
    sender: ${app.mail.sender:feedless-sender@localhost}
  auth:
    anonymous-token-valid-for: ${auth.token.anonymous.validForDays:28}
  mail:
    mailgun-key: ${MAILGUN_KEY:}
```

`ServerConfigResolver` today fails to start without `APP_GIT_COMMIT`; through `BuildInfo` it gets the same `unknown` default as `ServerStatusService`. The Mailgun bean stays conditional on `MAILGUN_KEY` itself: the alias always defines `app.mail.mailgun-key` (empty when unset), and `@ConditionalOnProperty` treats an empty value as a match.

### Deleted

- `PropertyService` and `AppConfig`.
- Keys nothing reads: `app.dateFormat`, `app.timeFormat`, `app.webToFeedVersion`, `app.defaultDateFormat`, `app.defaultTimeFormat` (with `APP_DEFAULT_DATE_FORMAT` / `APP_DEFAULT_TIME_FORMAT` in `application-prod.yaml`), and `default.auth.token.anonymous.validForDays`, whose 28 days becomes the alias default.
- Non-config values leave config: `anonymousEmail` becomes a constant next to `User`; `AppConfig.maxPageSize` becomes a paging constant in `domain`.

## Conventions

- **Shape:** `@ConfigurationProperties("prefix") @Validated @Profile(AppProfiles.properties) data class XProperties(val …)`, defaults for optional values. The single constructor makes Boot bind through it: immutable, no `lateinit`, no `@ConstructorBinding`.
- **Profile:** `AppProfiles.properties`, active in both the `selfHosted` and `saas` groups. A bean gated on other profiles that injects a properties class needs `properties` in its tests' `@ActiveProfiles` (Critical Rule 1).
- **Validation at startup:** Jakarta constraints (`@NotBlank`, `@Size(min = …)`) and Kotlin `init { require(…) }` for the rest, including "does not start with `${`". This replaces `PropertyService`'s `@PostConstruct` asserts; a bad value fails context startup with Boot's binding report naming the key.
- **Typed at the boundary:** `Locale`, `Duration` with `@DurationUnit(ChronoUnit.DAYS)` (a plain `28` stays 28 days), `List<String>` from comma-separated values, enums through Boot's lenient enum binding (`opt-out` → `OPT_OUT`). Hand-written parsing in the session services and `ReportSubscriptionMode.parse` goes.
- **Secrets masked:** classes holding a secret (`SessionProperties`, `RootUserProperties`, `WebSecurityProperties`, `LicenseProperties`, `AnalyticsProperties`, `StripeProperties`, `MailgunProperties`) override `toString()` to mask it; the generated data-class `toString` would otherwise leak it into logs.
- **Startup logging:** `PropertyService`'s dump of all values goes; `AppInitListener` keeps logging version, commit and timezone and adds `PublicUrls`.
- **The only way to read config:** no `@Value`, no `Environment` lookups, no central config class in main sources. `docs/rules/kotlin-spring.md` gains this rule.

## Migration order

One PR, one commit per step; each step compiles and passes `./gradlew lint test`.

1. **Groundwork** (`refactor(server-core)`): `@ConfigurationPropertiesScan`, the yaml aliases, and an empty `LegacyConfigBindingTest` that loads the real `application.yaml`. Each later step writes its class's legacy-key assertions there first, watches them fail, then adds the class, so every commit stays green.
2. **`PublicUrls` replaces `AppConfig`** across `domain`, `graphql-api`, `http-api`, `stripe-payments`, `server-core`. Widest ripple, so it goes first.
3. **`BuildInfo` and `LocaleProperties`.**
4. **Session:** `SessionProperties`, `RootUserProperties`; `PropertyService`'s asserts move into their validation.
5. **Remaining `server-core`:** `WebSecurityProperties`, `PrivacyProperties`, `AnalyticsProperties` split out of `AnalyticsService`, `TelegramProperties` converted.
6. **Other modules:** `ReportProperties`, `LicenseProperties`, `CliInstallProperties`, `StripeProperties`, `ProductsAuthProperties` and `MailGatewayProperties` converted.
7. **`MailgunProperties`** as its own `fix(mail-adapter)` commit, so the bug fix appears in the release notes.
8. **Delete** `PropertyService`, `AppConfig` and the unused keys; add the configuration rule to `docs/rules/kotlin-spring.md`; update the `server-core` row in `AGENTS.md`, which lists `PropertyService`.

## Testing

- **Legacy binding:** `LegacyConfigBindingTest` covers `APP_GIT_COMMIT`, `app.git.commit`, `APP_BUILD_TIMESTAMP`, `APP_LICENSE_KEY`, `APP_PEM_FILE`, `APP_BLACKLISTED_DOMAINS`, `MAILGUN_KEY`, `auth.token.anonymous.validForDays`, `app.mail.sender` and `app.report.subscription-mode=opt-in`.
- **Validation:** a short `jwtSecret` and a `rootSecretKey` of the form `${…}` each fail context startup, and the failure names the key.
- **Mailgun:** the configured key, not `"MAILGUN_KEY"`, reaches `MailGunGateway`.
- **Unit tests** construct properties instead of mocking `PropertyService` / `AppConfig` (about 30 test files); each module's test sources get a small fixture (for example `testPublicUrls()`) so values are not repeated.
- **Integration tests** replace `PropertyService::class` in their context lists with the specific properties classes and add `properties` to `@ActiveProfiles` where a bean now needs it.
- **Definition of Done:** `./gradlew lint test`.

## Follow-ups

- **Align config key names.** Today's keys mix flat camelCase (`app.rootEmail`), kebab-case nesting (`app.report.subscription-mode`) and bare env names used as keys (`APP_LICENSE_KEY`). Regroup them per properties class, keep the env var names, and map them in yaml; direct key overrides such as `-Dapp.rootEmail` would break, so it ships as its own change.
- **A mechanical guard against new `@Value`.** `./gradlew lint` does not cover Kotlin; until something does, the rule in `docs/rules/kotlin-spring.md` is the guard.
