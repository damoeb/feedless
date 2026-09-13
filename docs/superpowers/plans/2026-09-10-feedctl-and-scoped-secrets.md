# `feedctl` CLI, GitHub-aligned HTTP API, and Scoped Secrets

<!-- CHALLENGE-THE-PLAN-METADATA
{"rounds": 5, "categoriesCovered": ["technical-architecture", "technical-stack", "technical-build", "technical-testing", "domain-rules", "domain-lifecycle", "security-access", "security-credentials", "security-supply-chain", "ux-error", "ux-scripting", "ux-output", "performance", "scalability-quota", "trade-offs-scope"], "deferred": [], "askedQuestions": ["delivery slicing", "binary integrity", "piped output", "windows support", "token storage", "multiple hosts", "exit codes for failed harvests", "test strategy", "sequencing of API changes vs secrets vs merge", "which credentials /api/v1 accepts", "sync vs async source run", "definition of a broken source", "cross-repository errored lookup", "implementation language", "editor failure handling", "concurrent flow edits", "go toolchain in CI gate", "distribution and version compatibility", "dry-run quota", "dry-run retention and visibility"]}
-->

**Goal:** Ship `feedctl`, a `gh`-style CLI over `/api/v1`, whose first use case is finding broken sources, reading their last error, and fixing their flow (selectors, actions). Align `/api/v1` with GitHub's REST conventions while `feature/http-api` is still unmerged. Scoped secrets replace unscoped `UserSecret` tokens in a follow-up after the merge.

**Builds on:** `feature/http-api` (REST `/api/v1`, 27 operations, unmerged). Branch: `feature/feed-ctl`, cut from `feature/http-api`; slice 1's server and CLI work both land here, so this branch supersedes `feature/http-api` and merges in its place. This plan supersedes `docs/superpowers/plans/2026-07-22-fl-http-api-cli.md`: the bash `fl` scaffold and its node:test mock-server suite are replaced by a Go implementation; what carries over is the command conventions and the rule that a token is never sent over plain `http://` unless the host is loopback.

**Sequencing:** delivery is sliced so the broken-source use case works first, not last.

1. **Slice 1 — the use case.** On `feature/http-api`: changes 1–3, because they rename or remove existing paths and `/api/v1` is unreleased, so the merge is the last point where they are not breaking changes; plus changes 4, 5, 7, 9, 10 and 11, which the fix loop needs. Then `feature/http-api` merges. On `feature/fl-cli`, in parallel: the Go skeleton, hosts and credentials, `auth`, `source list|view|update|run`, `harvest list|view`, `api`, distribution via `/cli/**`, and the end-to-end smoke test.
2. **Slice 2 — repositories, records, sources.** `repo list|view|create|update|delete`, `record list|view|create|update|delete`, `source create|delete`, plus the server work those commands need: conditional requests (change 10) on repositories and records, and `GET /repositories` honouring `q`. It lands on `feature/feed-ctl` before the merge, so one final review covers slices 1 and 2.
3. **Slice 3 — the rest of the surface.** `plan`, `group`, `member`, and change 6. All additive, so it can land after the merge.
4. **Slice 4 — scoped secrets** as their own plan and branch, managed in the web UI (no CLI commands), with re-authentication before creating or deleting a secret. Until then, every token that reaches `/api/v1` can touch every resource of its user — accepted for the interim because the API has no external consumers yet.

## Implementation

`feedctl` is a Go module in `packages/cli`, built into a single static binary like `gh`. Its HTTP client is generated from `packages/http-api/src/main/resources/openapi/openapi.yaml`, so a spec change that the CLI does not follow fails its build instead of failing at runtime against a server. Bash was the original choice (modelled on `bb`), but at 35+ commands with async polling, the `$EDITOR` round-trip, auto-pagination and keychain storage a single bash file loses typing and becomes hard to maintain; TypeScript was rejected because it would require Node 24 on every machine that runs the CLI.

`packages/cli` becomes a Gradle module that shells out to Go, the same pattern as `agent` and `app-web` shelling out to yarn: its `lint` task runs `go vet` and `golangci-lint`, its `test` task runs `go test`, so `./gradlew lint test` stays the single Definition of Done and covers the CLI. CI and developer machines need a Go toolchain, pinned via `go.mod`'s `toolchain` directive.

**Distribution:** each Feedless instance serves its own CLI build at `/cli/feedctl-<os>-<arch>` (darwin and linux, amd64 and arm64; Windows is not built — WSL covers it, and a Windows Credential Manager backend would be one more untested keyring path). Next to the binaries it serves `/cli/SHA256SUMS` and `/cli/install.sh`; `curl <host>/cli/install.sh | sh` detects OS and architecture, downloads the binary, and refuses to install on a checksum mismatch. This protects against corrupted or truncated downloads, not against a compromised instance — signing with a project key (cosign/minisign) is the upgrade path if that threat becomes relevant. Because Feedless is self-hosted, instances run different versions; a binary downloaded from the instance it talks to always matches that instance's API, which removes CLI/server compatibility management entirely. `./gradlew buildImages` cross-compiles the four binaries and bakes them into the `server-core` image (~30 MB larger — accepted for guaranteed compatibility). The `/cli/**` path is public: `HttpApiJwtFilter` only guards `/api/v1/**`, and `SecurityConfig` whitelists `/cli/**`. GitHub Releases and Homebrew were rejected because they would require actively maintaining a compatibility matrix between CLI and server versions.

**Hosts and credentials:** `feedctl` knows several instances at once, like `gh` with multiple hostnames — typically a production instance and a local dev server. `feedctl auth login --url <host>` stores one token per host; one host is the default, and `--host` or `FEEDCTL_HOST` selects another per call. Tokens go into the OS keyring (macOS Keychain, Linux Secret Service); where no keyring exists — servers, CI — they fall back to a `0600` config file and `auth login` warns about it. `FEEDCTL_TOKEN` always wins over stored credentials. A binary downloaded from one instance may be pointed at another, so `feedctl` compares its own version with the host's `X-Feedless-Version` response header (change 11) and warns on mismatch instead of failing.

**Testing:** Go tests run the commands against `httptest` mock servers whose responses are validated against `openapi.yaml`, so a mock cannot drift from the spec. A single end-to-end smoke test runs against a real `server-core`, `agent` and PostGIS via Testcontainers, plus a static fixture site: it creates a source with a deliberately broken selector, lets it fail, finds it with `source list --errored`, fixes it with a `--dry-run` and then a real `source run`, and asserts the harvest succeeds. It covers what the spec cannot — throttling, `412` on stale `If-Match`, async harvest completion. Mocks alone were rejected because exactly that server behaviour is what the first use case depends on.

## First use case

```
feedctl source list --errored                               # across all repositories
feedctl harvest list -R <repo> -S <src> --limit 1
feedctl harvest view <id> -R <repo> -S <src> --log
feedctl source run <src> -R <repo> --flow fix.json --dry-run
feedctl source update <src> -R <repo> --flow fix.json       # or --editor
feedctl source run <src> -R <repo>                          # harvest now
```

**What "broken" means is decided in the CLI, not the server.** The API exposes the raw `errorsInSuccession` counter (change 7) and accepts it as a threshold; `feedctl source list --errored[=N]` sends `minErrorsInSuccession=N`, default `N=1`. The default already excludes transient failures: the harvester resets `errorsInSuccession` to 0 on DNS, connection, resumable and no-items errors while still setting `lastErrorMessage`, so `lastErrorMessage` alone over-reports. The trade-off — every client may define "broken" differently — is accepted in exchange for letting the operator tune the threshold without a server release.

Without `-R`, `--errored` uses the cross-repository endpoint `GET /user/sources` (change 9) — one request instead of one per repository, which at a few hundred repositories would also run into `@Throttled`. There is no way to pre-filter repositories instead: `Repository.sourcesCountWithProblems` is always `null` on the REST path, and on GraphQL it counts sources with `lastRecordsRetrieved == 0`, not failing ones.

`source run` is asynchronous (change 5): the CLI receives a harvest id and polls `GET .../harvests/{id}` until the harvest finishes, printing its status, then its log on failure — the `gh run watch` model. `--no-wait` returns the harvest id immediately.

`source update --editor` works like `kubectl edit`: the CLI fetches the source, opens its flow as JSON in `$EDITOR`, and on save validates it locally and sends the PATCH. If the JSON does not parse or the server answers `400`, the editor reopens with the user's text intact and the error as a comment block at the top; saving an unchanged or empty file aborts without a request. Nothing the user typed is lost on a failed attempt.

## CLI conventions

- Shape: `feedctl <entity> <action> [<id>] [flags]`.
- Entities (singular): `repo`, `source`, `harvest`, `record`, `plan`, `group`, `member`; plus `auth` and `api` as in `gh`. Secrets have no CLI commands: they are created and deleted in the web UI only (see Scoped secrets).
- Actions map 1:1 to HTTP: `list` (GET collection), `view` (GET item), `create` (POST), `update` (PATCH; field flags, `--input <file>`, or `--editor`), `delete` (DELETE), `run` (POST action). No other verbs.
- The resource id is positional; everything else is a flag. Parent scope is a flag: `-R/--repo`, `-S/--source`, `-G/--group`, defaulting from `FEEDCTL_REPO` like `GH_REPO`.
- Global flags: `--json [fields]`, `--jq <expr>`, `--limit <n>` (auto-paginates), `--yes` for `delete`.
- Output follows `gh`: on a terminal, aligned tables with colour and truncated columns; when piped, tab-separated rows without colour or truncation, so `cut`/`awk` work without `--json`. `NO_COLOR` is honoured, and progress (polling a harvest, paginating) goes to stderr only.
- `update` after a read sends `If-Match` (change 10): `--editor` always does, because it edits what it fetched; a `412` tells the user the resource changed underneath them and reopens the editor on the fresh version with their edit shown as a comment. A plain field-flag `update` without a prior read sends no `If-Match` and is last-write-wins.
- Exit codes follow `gh`: `0` success, `1` error, `2` cancelled, `4` authentication required. `source run` without `--no-wait` exits `1` when the harvest completes with `ok: false`, so the fix loop is scriptable (`until feedctl source run …; do …; done`).

## Command surface

✅ exists on `feature/http-api` · ⚠️ exists but diverges from GitHub conventions · 🆕 needs a new endpoint

| Command | Key flags | Endpoint | |
|---|---|---|---|
| `auth login` | `--url`, `--with-token` | validates via `GET /user` | ✅ |
| `auth status` | | `GET /user` | ✅ |
| `auth logout` | | (local) | ✅ |
| `repo list` | `--product`, `--visibility`, `--search` | `GET /repositories` | ✅ |
| `repo view <id>` | | `GET /repositories/{id}` | ✅ |
| `repo create` | `--title`, `--product`, `--cron`, `--visibility`, `--input` | `POST /repositories` | ✅ |
| `repo update <id>` | `--title`, `--cron`, `--visibility`, `--editor` | `PATCH /repositories/{id}` | ✅ |
| `repo delete <id>` | `--yes` | `DELETE /repositories/{id}` | ✅ |
| `source list` | `-R` (optional), `--disabled`, `--search`, `--errored[=N]` | `GET /repositories/{r}/sources`, without `-R`: `GET /user/sources` | ✅ / 🆕 |
| `source view <id>` | `-R` | `GET /repositories/{r}/sources/{id}` | ✅ |
| `source create` | `-R`, `--title`, `--tags`, `--input flow.json` | `POST /repositories/{r}/sources` | ✅ |
| `source update <id>` | `-R`, `--title`, `--tags`, `--disabled`, `--flow <file>`, `--editor` | `PATCH /repositories/{r}/sources/{id}` | ✅ |
| `source delete <id>` | `-R`, `--yes` | `DELETE /repositories/{r}/sources/{id}` | ✅ |
| `source run <id>` | `-R`, `--flow <file>`, `--dry-run`, `--no-wait` | `POST /repositories/{r}/sources/{id}/harvests` → `202` | 🆕 |
| `harvest list` | `-R`, `-S` | `GET /repositories/{r}/sources/{s}/harvests` | ✅ |
| `harvest view <id>` | `-R`, `-S`, `--log` | `GET .../harvests/{id}`, `GET .../harvests/{id}/logs` | 🆕 |
| `record list` | `-R` | `GET /records?repositoryId=` | ⚠️ |
| `record view <id>` | | `GET /records/{id}` | ✅ |
| `record create` | `-R`, `--title`, `--url`, `--text`, `--tags` | `POST /records` | ⚠️ |
| `record update <id>` | `--title`, `--url`, `--text`, `--tags`, `--editor` | `PATCH /records/{id}` | ✅ |
| `record delete <id>…` | `-R`, `--yes` | `DELETE /records` with body | ⚠️ |
| `plan list` | | `GET /plans` | ✅ |
| `plan view <id>` | | `GET /plans/{id}` | ✅ |
| `group list` | | `GET /groups` | ✅ |
| `group view <id>` | | `GET /groups/{id}` | ✅ |
| `group create` | `--name` | `POST /groups` | ✅ |
| `group update <id>` | `--name` | `PATCH /groups/{id}` | 🆕 |
| `group delete <id>` | `--yes` | `DELETE /groups/{id}` | ✅ |
| `member list` | `-G` | `GET /groups/{g}/members` | ✅ |
| `member create` | `-G`, `--user`, `--role` | `POST /groups/{g}/members` | ✅ |
| `member delete <userId>` | `-G`, `--yes` | `DELETE /groups/{g}/members/{userId}` | ✅ |
| `api <path>` | `-X`, `-f k=v`, `--input` | any | ✅ |

## HTTP API changes (before merging `feature/http-api`)

1. Nest records under repositories: `/repositories/{r}/records`, like sources.
2. Replace `DELETE /records` with a body by `DELETE /repositories/{r}/records/{id}`; the CLI loops for multiple ids.
3. Drop `/repositories/count`; add `totalCount` to list responses (GitHub's `total_count`).
4. Add `GET .../harvests/{id}` and `GET .../harvests/{id}/logs` (GitHub `actions/runs/{id}/logs`); `includeLogs` becomes obsolete. `Harvest` gains `status` (`queued | running | completed`) and `dryRun`; `ok`, `itemsAdded`, `itemsIgnored` and `finishedAt` are only meaningful once `status` is `completed`.
5. Add `POST .../harvests` to trigger a harvest (GitHub `workflow_dispatch`). It answers `202 Accepted` with the queued `Harvest` and a `Location` header, because a run goes through a headless-Chromium agent and can take minutes — a synchronous response would run into proxy and client timeouts. With `{"dryRun": true, "flow": …}` it is a dry run: the harvest is persisted with `dryRun: true` so it can be polled like any other, but the source's flow is not changed and extracted items are not stored as records. Dry runs are `@Throttled` like the other write endpoints, which protects agent capacity, but they do not count against the plan's quota — iterating on a broken selector must not cost the user their harvest budget. They never touch `errorsInSuccession` or `lastErrorMessage`, are hidden from `GET .../harvests` unless `?dryRun=true` is passed (`feedctl harvest list --dry-run`), and a scheduled job deletes them after 7 days so they do not accumulate in the source's harvest history.
6. Add `PATCH /groups/{id}`.
7. Expose `errorsInSuccession` on `Source`, and add `repositoryId` so a source listed outside its repository can be addressed with `-R`. `GET .../sources` accepts `minErrorsInSuccession`.
8. ~~Pagination: `page`/`per_page` + `Link` header.~~ Dropped: it renames a query parameter on every list endpoint, so it could only land before the merge, and `page`/`pageSize` + `hasMore` is enough for the CLI's auto-pagination.
9. Add `GET /user/sources` — sources across all repositories the caller can access, with the same filters as the per-repository list including `minErrorsInSuccession` (GitHub's `GET /user/issues`).
10. Conditional requests: `GET` on a single resource returns an `ETag`; `PATCH` honours `If-Match` and answers `412 Precondition Failed` on mismatch. A `PATCH` with `flow` replaces the whole action sequence, so without this a CLI edit and a web-UI edit of the same source silently overwrite each other. The ETag is derived from the serialized resource, so no schema change is needed.
11. Every `/api/v1` response carries `X-Feedless-Version`, so clients can detect a version mismatch with the instance they talk to.

## Scoped secrets (follow-up plan, after the merge)

**Today:** `UserSecret` is value, expiry, owner, `lastUsedAt` — no scope. `HttpApiJwtFilter` accepts any signed, non-anonymous JWT — a `UserSecret`-derived token as well as a 48-hour browser-session JWT — and reads capabilities from its claims; it never consults `t_user_secret`, so deleting a secret does not revoke it and `lastUsedAt` is never updated for API use. Creation exists only as GraphQL `createUserSecret` without parameters. This stays as-is until the follow-up lands.

**Target:** long-term, `/api/v1` accepts only scoped secrets; secrets themselves are managed only through the web UI's GraphQL API with a browser session, never through `/api/v1`.

- A secret has a name and a mandatory expiry (capped), a resource scope (one group or selected repositories — never "everything the user owns"), and per-entity permissions: `repo`, `source`, `harvest`, `record`, `group`, `member` each `read` or `write`; `plan` `read` only. There is no `secret` permission: tokens cannot mint tokens.
- Token format: opaque string with a `fdl_` prefix (secret-scanner friendly, like `github_pat_`), stored as a hash; not a JWT. Every request looks the token up, so revocation is immediate and `lastUsedAt` is accurate.
- Enforcement: permissions checked in the security layer; resource scope checked in the existing `require…InRepository` guards, and in `GET /user/sources`, which must only return sources inside the token's scope.
- Bootstrap: the first secret is created in the web UI with a browser session (as on GitHub); the CLI takes it via `feedctl auth login --with-token`. A browser device flow may follow later.
- No `/api/v1` endpoints for secrets: a token must never manage tokens. Secrets are created and deleted in the web UI only (GraphQL, browser session); `feedctl` has no `secret` commands. Secrets are immutable — changing scope or expiry means deleting one and creating a new one, both behind re-authentication.
- **Re-authentication before creating or deleting a secret** — scoped secrets and, until the cutover, the legacy unscoped `UserSecret` (GraphQL `createUserSecret` / `deleteUserSecret`). Feedless users have no password, so the confirmation follows the instance's authentication mode (like GitHub's sudo mode): `root` → re-enter `APP_ROOT_SECRET_KEY`; `mail` → a one-time code sent by email (existing `OneTimePasswordService`); `sso` → a fresh login at the SSO provider (`prompt=login`, `max_age=0`), accepted only if the provider's `auth_time` is fresh. A successful confirmation opens a 10-minute window (configurable) bound to the current session and recorded server-side; within it further create/delete actions need no new confirmation. Outside the window the server refuses with `403` and code `REAUTH_REQUIRED`, and the web UI runs the confirmation and retries. The window never extends itself and ends with the session.
- Cutover: once scoped secrets exist, `/api/v1` stops accepting session JWTs and legacy `UserSecret` JWTs. Agents authenticate via `UserSecret` today (`Agent.secretKeyId`) and need their own `agent` scope before that cutover, or they break.
- Schema changes land as a new additive Flyway migration with the next free `V<n>__` number.

## Slice 1 tasks

Dependencies: T1 first (it is the contract the server and the generated Go client both build from); T3 before T4 and T5; C1 before C2–C5; D1 after C1; E1 last. Each task ends green on its module's tests and is one Conventional Commit.

**Server (Kotlin, `http-api` / `server-core` / `domain` / `jpa-data`)**

- [ ] **T1 Contract.** Edit `openapi.yaml` for changes 1–5, 7, 9, 10 (ETag/If-Match headers, `412`) and 11 (response header). Regenerate and adapt the controllers so everything compiles: records move under `/repositories/{r}/records`, `DELETE /records` becomes per-id, `/repositories/count` is removed and list responses gain `totalCount`.
- [ ] **T2 Access scoping.** `/api/v1` reads today check only the `user` capability: `RepositoryUseCase.findById` and `requireSourceInRepository` never compare owner or group, so any authenticated user can read another user's repository, sources and harvests by UUID. Every repository-scoped endpoint resolves the repository through one guard that requires the caller's user or group to own it and answers `404` otherwise (no existence leak). `GET /user/sources` (T6) uses the same rule as a query predicate.
- [ ] **T3 Harvest model.** New migration adds `status` (`queued | running | completed`, default `completed`), `dry_run` (default `false`) and a nullable `flow` (JSON, the dry-run flow override) to `t_harvest`; domain, entity and mapper follow. Fix the harvester: it never sets `errornous` on a failed harvest (so the API reports every harvest as `ok`) and never sets `itemsAdded`. `findAllBySourceId` orders by `created_at DESC` — it is unordered today. `deleteAllTailingBySourceId` keeps the newest 4 per source *and* `dry_run`, so dry runs cannot push real harvests out; a scheduled job deletes dry runs older than 7 days.
- [ ] **T4 Harvest endpoints.** `GET .../harvests/{id}` and `GET .../harvests/{id}/logs` (`text/plain`); `status` and `dryRun` on `Harvest`; list hides dry runs unless `?dryRun=true`; `includeLogs` removed.
- [ ] **T5 Run endpoint.** `POST .../harvests` validates the optional `flow`, inserts a `queued` harvest and answers `202` with `Location`. The harvest runs in the scheduler layer (`AppLayer.scheduler`), which may be a different process from the API layer: a new executor claims queued harvests with `SELECT … FOR UPDATE SKIP LOCKED`, marks them `running`, and runs them under the repository owner's `RequestContext`. A real run goes through the existing scrape-and-import path and updates the source's error state; a dry run scrapes with the override flow, writes the extracted items into the harvest log, and touches neither records nor the source. `@Throttled`; not counted against the plan quota.
- [ ] **T6 Sources.** `errorsInSuccession` and `repositoryId` on `Source`; `minErrorsInSuccession` on the per-repository list; `GET /user/sources` across the caller's repositories with the same filters.
- [ ] **T7 Conditional requests.** `ETag` on `GET .../sources/{id}` (hash of the serialized source), `If-Match` on `PATCH` with `412` on mismatch. Slice 1 covers sources; other single resources follow in slice 2.
- [ ] **T8 Version header.** `X-Feedless-Version` from `app.version` on every `/api/v1` response.

**CLI (Go, `packages/cli`)**

- [ ] **C1 Skeleton.** Go module with the client generated from `openapi.yaml` (`oapi-codegen`), a Gradle module whose `lint` runs `go vet` and `golangci-lint` and whose `test` runs `go test`, included in `settings.gradle.kts`. The bash `fl` scaffold and its node:test suite are removed.
- [ ] **C2 Hosts and credentials.** `auth login|status|logout`; keyring with `0600` file fallback; `FEEDCTL_TOKEN`, `FEEDCTL_HOST`, `--host`; refuse to send a token over `http://` unless loopback; warn on `X-Feedless-Version` mismatch.
- [ ] **C3 Output, exit codes, `api`.** TTY tables vs. tab-separated when piped, `--json [fields]`, `--jq`, `NO_COLOR`, progress on stderr; exit codes `0/1/2/4`; `feedctl api`.
- [ ] **C4 `source list|view|update`.** `--errored[=N]` (without `-R` via `/user/sources`), `--flow <file>`, `--editor` with the `kubectl edit` loop and `If-Match`/`412` handling.
- [ ] **C5 `source run`, `harvest list|view`.** Async polling, `--dry-run`, `--flow`, `--no-wait`, exit `1` on a failed harvest; `harvest view --log`.

**Delivery**

- [ ] **D1 Distribution.** Cross-compile darwin/linux × amd64/arm64 in the image build, bake into `server-core`, serve `/cli/feedctl-<os>-<arch>`, `/cli/SHA256SUMS`, `/cli/install.sh`; whitelist `/cli/**` in `SecurityConfig`.
- [ ] **E1 End-to-end smoke.** Testcontainers: `server-core`, `agent`, PostGIS and a static fixture site; broken selector → `source list --errored` → `--dry-run` → fix → `source run` succeeds.

## Slice 2 tasks

Dependencies: T9 and T10 before C6/C7; C8 independent of both. Same conventions as slice 1: reuse `client.NewFromConfig`, the `output` layer, `cmd.NewAPIError`, the `-R` helper and the editor loop; every task ends green and is one Conventional Commit.

**Server**

- [ ] **T9 Conditional requests on repositories and records.** `ETag` on `GET /repositories/{id}` and `GET /repositories/{r}/records/{id}`, `If-Match` on their `PATCH` with `412` on mismatch — the same semantics as sources (T7). Generalise `SourceETagCalculator` into one ETag helper used by all three resources, so the rule cannot drift.
- [ ] **T10 `GET /repositories` honours `q`.** The list and its `totalCount` apply the `q` full-text filter the spec already documents (today both ignore it).

**CLI**

- [ ] **C6 `repo list|view|create|update|delete`.** `list` with `--product`, `--visibility`, `--search`; `view <id>`; `create` from `--title`, `--product`, `--cron`, `--visibility` or `--input <file|->`; `update <id>` with field flags or `--editor` (the C4 editor loop, generalised over the resource, with `If-Match`/`412`); `delete <id>` asking for confirmation on a TTY unless `--yes`, refusing without `--yes` when not a TTY.
- [ ] **C7 `record list|view|create|update|delete`.** All under `-R`; `create` from `--title`, `--url`, `--text`, `--tags` or `--input`; `update <id>` with field flags or `--editor`; `delete <id>…` accepts several ids and deletes them one request each, reporting each result; same `--yes` rule as C6.
- [ ] **C8 `source create|delete`.** `create -R` from `--title`, `--tags`, `--flow <file|->` or `--input <file|->` (a full `SourceCreate`); `delete <id> -R` with the `--yes` rule.

## Release notes (slices 1 and 2)

- Every login path (SSO, magic mail, `authUser`) and every new token (`createUserSecret`) now requires the user to own a group; a user who owns none can no longer log in or create a token.
- Session and API tokens carry the user's group, and every request with a cookie or token re-checks that the user still owns it (one extra database lookup per request).
- Tokens issued before this change carry no group: writes made with them answer `403 NO_ACTING_GROUP`, and they must be recreated.
- The `Authorization` header now takes precedence over the `TOKEN` cookie on GraphQL too, as it already did on `/api/v1`.
- GraphQL `Repository.sources` honours `order`, and its default order changed (was per page by `lastRecordsRetrieved`, now `createdAt desc`).
- `/cli/**` downloads (`feedctl` binaries, `SHA256SUMS`, `install.sh`) are public.
- The `server-core` image grows by about 30 MB (the four `feedctl` binaries).
- Migrations V89–V92 take short locks on `t_harvest`.
- During a rolling deploy, the old pods' cleanup job can delete queued, running and dry-run harvests: deploy the scheduler last, or accept the loss.
- `/api/v1` was not functional on a real container before this branch (401 on every suspend endpoint), so no existing client depends on its previous behaviour.
- `authUser` is root-only again: any other account gets `account is not root`.
- Deleting a group, or removing an owner from one, is refused with `409 CONFLICT` when it would leave a member without an owned group or the group without an owner; deleting a group that still owns repositories is refused the same way, and a delete is now one transaction.
- A `500` from `/api/v1` answers the fixed message `unexpected error` with its `corrId`; the internal error text is only logged.
- `GET /api/v1/status` is public and reveals the server version, build commit/date and the number of connected agents.
- The `server-core` image builds `feedctl` itself in a Go stage; building the image needs BuildKit (`--build-context cli=…`), not a local Go.
- The prerender worker is now the `browser-automation-app` module (was `packages/agent`); its image is `damoeb/feedless:browser-automation-app-*` (was `agent-*`), and its k8s Deployment and Service are `feedless-browser-automation-app` (was `feedless-agent`); after the first deploy remove the old ones once with `kubectl delete deployment,service feedless-agent`. The interim name `feedless-browserautomation` never shipped, so only `feedless-agent` needs removal.
- Docker Hub has the `browser-automation-app-*` tags only after the next image push; until then `docker compose pull` with `docker-compose.prod.yml` fails for the `feedless-browser-automation-app` service.
- The Spring profile `agent` is now `browserautomation`: deployments that list profiles explicitly instead of using the `selfHosted`/`saas` groups must rename it.
