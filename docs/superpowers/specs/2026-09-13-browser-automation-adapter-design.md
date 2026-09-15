# Browser automation adapter: own module, trusted hosts, owner-aware routing

**Status:** design, approved in conversation 2026-09-13. Spec and implementation land on `feature/browser-automation-adapter`, after `2026-09-13-spring-boot-4-dgs-12-upgrade-design.md`, which this design depends on.

**Goal:** move the core's side of the browser-automation-app protocol out of `server-core` into its own module, and stop handing one user's scrape jobs to another user's self-hosted agent. Agents from allowlisted hosts become open instances that serve everyone; every other agent may still connect with its secret key but only serves its owner.

**Follows** `2026-09-12-hexagonal-modules-design.md`: packages stay unchanged, files move with `git mv`, ports are named after the capability, the executed test count does not drop.

**Separate spec:** extracting `AnalyticsService` into `packages/plausible-adapter`.

## Findings that shape the design

- **On DGS 9, a subscription resolver cannot see the client's host.** The legacy `graphql-transport-ws` handler calls `DgsQueryExecutor.execute(…, headers = null, …, webRequest = null)`. This is why the platform upgrade comes first.
- **On Spring for GraphQL, an interceptor can.** A `WebSocketGraphQlInterceptor` receives a `WebSocketGraphQlRequest` whose `getSessionInfo().getRemoteAddress()` is the WebSocket session's socket address, and `configureExecutionInput` puts values into the GraphQL context the resolver reads. Supported API, no thread-bound state.
- **Every agent is an open instance today.** `BrowserAutomationService.addAgent` saves `openInstance = true`, and `prerender` picks a random agent from all connected ones regardless of owner.
- **The existing allowlist does not fit.** `app.whitelistedHosts` is resolved to IPs once at startup (`StatefulAuthService.resolveWhitelistedHosts`), so restarted pods and containers stop matching. It contains `172.26.0.1`, the Docker bridge gateway, which is where every request through Compose's published port appears to come from. `HttpUtil.getRemoteAddr` trusts a client-supplied `X-Real-IP`.
- **Public traffic reaches the core through proxies.** k8s routes it through Ingresses (controller pods sit in the pod network); Compose publishes `8080:8080`. A pod CIDR or the bridge gateway in a trust list would trust every external agent.
- **No forwarded-header handling is configured.** `server.forward-headers-strategy` is unset, so the session's remote address is the TCP peer. Enabling it would let `X-Forwarded-For` rewrite that address.
- **The job owner is already in the coroutine context.** Harvests run under `childRequestContext(repository.ownerId, …)`; ad-hoc scrapes carry the session user in `RequestContext.userId`.

## Module layout

New Gradle module `packages/browser-automation-adapter`, depending on `domain` and `graphql-api`, never on `server-core`. It is the one adapter module that depends on `graphql-api`, because the agent protocol is made of generated GraphQL types and uses `graphql-api`'s `Source`/`ScrapeResponse` mappers. `server-core` depends on it with `implementation(project(...))`, as on `mail-adapter`.

Moves from `server-core` (package `org.migor.feedless.browserautomation`, unchanged):

- `BrowserAutomationService`, `BrowserAutomationRef`, `BrowserAutomationResponse`
- `BrowserAutomationRegistry`, `StatefulBrowserAutomationRegistry`, `StatelessBrowserAutomationRegistry`
- `BrowserAutomationSyncExecutor` and its test

Bean profiles are unchanged.

Stays:

- `BrowserAutomationGateway` in `graphql-api`; `registerAgent` gains `trusted: Boolean`.
- `BrowserAutomation`, `BrowserAutomationDirectory`, `BrowserAutomationRepository` in `domain`.
- `AgentTrustInterceptor` and `HostAllowlist` in `server-core`. The adapter only ever receives `trusted: Boolean`.

New seams, so the adapter needs nothing from `server-core`:

| Seam | Where | Implemented by | Replaces |
|---|---|---|---|
| `SecretKeyLookup` (`findBySecretKeyValue`, `updateLastUsed`) | `domain`, package `userSecret` | `AuthService` (stateful and stateless unchanged) | `BrowserAutomationService` → `AuthService` |
| `TokenIssuer.issueAgentToken(secret: UserSecret): AuthToken` | `domain`, existing port | `JwtTokenIssuer`, same claims as `createJwtForService` (`AgentCapability`, `UserCapability(owner)`, type `SERVICE`, service expiry) | `BrowserAutomationService` → `JwtTokenIssuer` |
| `Prerenderer` (`suspend fun prerender(source: Source): ScrapeResponse`) | `browser-automation-adapter` | `BrowserAutomationService` | `ScrapeService` → `BrowserAutomationService` |

`Prerenderer` returns the generated `ScrapeResponse` because `ScrapeService` already maps it; a domain mirror type is out of scope. `JwtTokenIssuer.createJwtForService` goes away once `issueAgentToken` covers its only caller.

## Trust: session address to `openInstance`

**Config.** New key `app.browserAutomation.trustedHosts` (env `APP_BROWSER_AUTOMATION_TRUSTED_HOSTS`), separate from `app.whitelistedHosts`. Entries are hostnames, IPs or CIDR ranges (IPv4 and IPv6), separated by commas or spaces. No built-in entries; an empty list trusts no agent.

| Environment | Value |
|---|---|
| `application-dev.yaml` | `127.0.0.1,::1` |
| `application-prod.yaml` | `${APP_BROWSER_AUTOMATION_TRUSTED_HOSTS:}` |
| Compose (`docker-compose.yml`, `docker-compose.prod.yml`) | `feedless-browser-automation-app` (Docker DNS returns the container IP) |
| k8s (`feedless-config.yaml`) | `feedless-browser-automation-app-headless.default.svc.cluster.local` |

k8s gets a headless Service `feedless-browser-automation-app-headless` (`clusterIP: None`, same selector) so DNS returns the agent pod IPs. A comment next to each value says never to list ingress, proxy or gateway addresses, or ranges that cover them.

**Matcher.** `HostAllowlist` in `server-core`, package `session`: `matches(address: InetAddress): Boolean`. IPs and CIDR ranges are matched directly; hostnames are resolved with `InetAddress.getAllByName` on every check, uncached, because agent connections are rare. A name that does not resolve does not match and logs a warning; it never throws. `StatefulAuthService.isWhitelisted` switches to a `HostAllowlist` built from `app.whitelistedHosts` plus the loopback and local addresses it adds today, which drops the startup resolution from throttling without changing its config. `StatelessAuthService.isWhitelisted` stays `true`.

**Interceptor.** `AgentTrustInterceptor : WebSocketGraphQlInterceptor` in `server-core` (profile `${AppProfiles.browserAutomation} & ${AppLayer.api}`). In `intercept`, for a `WebSocketGraphQlRequest` it takes `sessionInfo.remoteAddress.address`, computes `allowlist.matches(address)`, and adds it to the GraphQL context under `AgentTrust.CONTEXT_KEY` via `configureExecutionInput`. It never reads `X-Forwarded-For`, `X-Real-IP` or any other header, and never rejects. `server.forward-headers-strategy` must stay unset; a comment in `application.yaml` says so, and the interceptor test guards the header case.

**Resolver.** `AgentTrust` (`graphql-api`, package `browserautomation`) holds the key and `fun isTrusted(dfe): Boolean = dfe.graphQlContext.getOrDefault(CONTEXT_KEY, false)`. `BrowserAutomationResolver.registerAgent` takes a `DataFetchingEnvironment` and calls `gateway.registerAgent(data, AgentTrust.isTrusted(dfe))`.

**Adapter.** `BrowserAutomationRef` gains `openInstance`; `addAgent` saves `openInstance = agentRef.openInstance` instead of `true`.

Trust is fixed per connection. Agents reconnect on restart, so a changed trust list applies from the next connection.

## Routing

`prerender(source)`:

1. `owner = currentCoroutineContext()[RequestContext]?.userId`
2. Candidates are connected agents with `ownerId == owner`; if none, connected agents with `openInstance`.
3. No candidates: `ResumableHarvestException("No agents available", 10 minutes)`, as today.
4. Pick a random candidate.

A job without an owner only reaches open agents. An untrusted agent never receives a job that is not its owner's. In stateless mode every agent and job belongs to root, so routing is unchanged there.

`agentRefs` becomes a `CopyOnWriteArrayList` and `pendingJobs` a `ConcurrentHashMap`; both are mutated from IO threads today.

## Error handling

- The owner's agent timing out (60 s) fails the job as today. There is no retry on an open agent; fallback applies only when the owner has no agent connected.
- An unresolvable trusted hostname makes the connection untrusted and logs a warning.

## Tests

- `HostAllowlistTest` (unit, `server-core`): exact IPv4 and IPv6, CIDR IPv4 and IPv6, hostname `localhost`, unresolvable name, empty list.
- `AgentTrustInterceptorTest` (unit, `server-core`): a `WebSocketGraphQlRequest` from a non-allowlisted remote address carrying `X-Forwarded-For` and `X-Real-IP` headers with an allowlisted value yields `false` in the context; an allowlisted remote address yields `true`; a non-WebSocket request is passed through unchanged.
- Routing tests (unit, `browser-automation-adapter`): owner's agent chosen over an open one; open agent chosen when the owner has none; an untrusted non-owner agent is never chosen; ownerless job goes to an open agent; no candidates throws `ResumableHarvestException`; `addAgent` persists `openInstance` from the flag.
- WebSocket integration test (`server-core`, random port; extends the one the upgrade adds): a `graphql-transport-ws` client over loopback subscribes to `registerAgent`; the mocked `BrowserAutomationGateway` receives `trusted = true` when `trustedHosts` contains `127.0.0.1,::1` and `false` when it is empty. It also pins that the interceptor's context reaches a `@DgsSubscription` fetcher, which the DGS docs do not state.
- The nine integration tests that list `BrowserAutomationService` in `@MockitoBean` switch to `Prerenderer` and `BrowserAutomationGateway`.
- Definition of Done: `./gradlew lint test`; executed test count does not drop.

## Docs

- `AGENTS.md`: module table row for `browser-automation-adapter`; module count 17 → 18 Gradle modules, 22 → 23 directories.
- `packages/browser-automation-app/README.md`: the trusted-hosts setting and what an untrusted agent receives.

## Out of scope

- Verifying that `submitAgentData` comes from the agent that received the job (callback IDs are random UUIDs).
- Domain mirror types for `ScrapeResponse` behind `Prerenderer`.
- Rejecting untrusted hosts.
- Fixing `HttpUtil.getRemoteAddr`'s trust in `X-Real-IP` for throttling.

## Risks

- If the interceptor's GraphQL context does not reach `@DgsSubscription` fetchers, the WebSocket integration test fails; the fallback is reading the attribute in a Spring for GraphQL `@SubscriptionMapping` for `registerAgent` instead of the DGS annotation.
- Enabling `server.forward-headers-strategy` later would make trust spoofable through `X-Forwarded-For`.
- Agents in environments without a configured trust list become owner-only after deploy. Operators must set `APP_BROWSER_AUTOMATION_TRUSTED_HOSTS` in the same release, or no open instance exists and every job whose owner has no agent of their own fails with "No agents available" (in k8s the agents use the root key, so that is every user's job).
