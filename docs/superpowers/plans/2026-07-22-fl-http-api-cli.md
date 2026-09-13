# `fl` — Feedless HTTP API CLI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a single-file bash CLI (`fl`) that wraps the Feedless HTTP API (`/api/v1`) with `gh`-style UX, covering all 27 operations in `packages/http-api/src/main/resources/openapi/openapi.yaml` plus a raw `fl api` escape hatch.

**Model:** `/Users/markus.ruepp/.agents/skills/working-with-bitbucket-api/bin/bb` — a 2276-line single-file bash script with a `cmd_<group>_<verb>` dispatcher, keychain-backed auth, `--json` output mode, and a mock-server integration test suite. This plan clones that architecture against the Feedless spec.

**Supersedes:** `docs/superpowers/plans/2026-07-17-bash-api-cli.md` (GraphQL-based `feedless` CLI with `lib/*.sh` modules). That plan predates the `http-api` package; the REST API now covers everything it reached for via GraphQL except UserSecret minting.

**Tech Stack:** Bash 4+ (POSIX-portable except macOS Keychain/`open`), `curl`, `jq`, `column`; tests in `node:test` + `tsx` + `express` mock server (same harness as `bb`).

---

## Global Constraints

- Implementation language: bash only, **one file** (`packages/cli/bin/fl`) — no `lib/` split, matching `bb`. Rationale: single file is trivially symlinkable/copyable to `~/bin`, no `source` path resolution, and the whole surface is ~1400 lines.
- Required runtime deps: `bash`, `curl`, `jq`. `column` for table output (degrade gracefully if absent).
- **Base path is `/api/v1`** (`servers[0].url` in the spec). `FL_API` = `${FL_API_URL}/api/v1`.
- **Base URL is required config, not a constant** — Feedless is self-hosted. Unlike `bb` (which pins `api.bitbucket.org` and restricts overrides to loopback), `fl` must talk to arbitrary hosts. Security compensation: refuse to send a token over plain `http://` unless the host is loopback (`localhost`, `127.0.0.1`, `[::1]`) — see Task 1.
- **Auth:** `Authorization: Bearer <UserSecret.value>` (GitHub-PAT style). The legacy `Authentication: Bearer` header is deprecated — do not emit it. The `TOKEN` cookie is browser-only — do not emit it.
- **There is no REST login endpoint.** `UserSecret` lifecycle lives on GraphQL (`createUserSecret` / `deleteUserSecret`). `fl auth login` therefore stores a token the user pastes (minted in the web UI or via the GraphQL mutation); it does not mint one. Verified by calling `GET /api/v1/user`.
- Anonymous JWTs are rejected on `/api/v1/**` — every route requires auth, including `GET /plans`.
- **Creates are singular.** `POST /repositories` and `POST /repositories/{id}/sources` each take one object and return one object.
- **Every `PATCH` returns `200` with the updated resource**; `DELETE` returns `204` empty. So `fl repo edit` can print the updated record without a follow-up `GET`, and only `DELETE` responses must skip `jq`.
- **`DELETE /records` takes both a required `?repositoryId=` query param and a `{"ids":[...]}` body** — curl needs `-X DELETE --data`.
- List envelopes are `{items: [...], hasMore: bool}` with `page` (0-based) / `pageSize` (max 100, default 20) params. `hasMore` is trustworthy (server fetches `pageSize + 1`), so the paginator can loop on it — but still stop on an empty page as a belt-and-braces guard.
- **Timestamps are RFC 3339 UTC strings**, so `fl` formats them for display rather than converting from epoch millis.
- Errors always carry `ApiError {code, message, corrId, path, errors?}`, including 404s. `429` responses carry `Retry-After` — `fl_api_call` should honour it with a single bounded retry.
- **Enum values on the wire**: `visibility` is `private`/`public`; `--product` accepts `all` only as a *filter* (`fl repo list`, `fl repo count`), never on create.
- **Optional fields are absent, not zero** — `shareKey` (owner-only), `sourcesCount`, `recordCount`. Table formatters must render `null` as `-`, not `0`.
- CLI stdout: human-readable tables by default, raw JSON under `--json`. stderr: errors and progress.
- Exit codes: `0` success, `1` user error (bad flags, missing token), `2` API/transport error.
- Defer command syntax to `fl --help` / `fl <group> --help` — the README documents *when/why*, not every flag (the `bb` skill's "defer to `--help`" rule).

---

## File Structure

```
packages/cli/
├── bin/
│   └── fl                       # single-file entrypoint: helpers + cmd_* + dispatcher
├── install.sh                   # dep check + symlink to ~/bin/fl
├── README.md                    # install, auth, examples, decision notes
└── tests/
    ├── package.json             # node:test + tsx + express
    ├── tsconfig.json
    ├── helpers/
    │   ├── server.ts            # mock API: records requests, replays fixtures
    │   └── run.ts               # spawn `fl` with FL_API_URL/FL_TOKEN, capture stdio
    ├── fixtures/
    │   ├── bin/security         # macOS Keychain shim (never touch the real keychain)
    │   ├── user.json
    │   ├── repositories.json
    │   ├── repository.json
    │   ├── sources.json
    │   ├── records.json
    │   ├── harvests.json
    │   ├── plans.json
    │   └── groups.json
    ├── global-flags.test.ts
    ├── auth.test.ts
    ├── repo.test.ts
    ├── source.test.ts
    ├── harvest.test.ts
    ├── record.test.ts
    ├── plan.test.ts
    ├── group.test.ts
    └── api.test.ts
```

`packages/cli` is **not** a Gradle module — do not add it to `settings.gradle.kts`.

---

## Command Surface (all 27 spec operations + escape hatch)

| Command | HTTP |
|---|---|
| `fl auth login` | local (verifies via `GET /user`) |
| `fl auth status` | `GET /user` |
| `fl auth logout` | local |
| `fl repo list` | `GET /repositories` (`--page --page-size --product --visibility -q --limit`) |
| `fl repo count --product <v>` | `GET /repositories/count` (product **required**) |
| `fl repo view <id>` | `GET /repositories/{id}` |
| `fl repo create` | `POST /repositories` (single object) |
| `fl repo edit <id>` | `PATCH /repositories/{id}` → 200 + body |
| `fl repo delete <id>` | `DELETE /repositories/{id}` → 204 |
| `fl source list [repoId]` | `GET /repositories/{id}/sources` (`--disabled --like`) |
| `fl source view [repoId] <srcId>` | `GET …/sources/{sid}` |
| `fl source create [repoId]` | `POST …/sources` (single object) |
| `fl source edit [repoId] <srcId>` | `PATCH …/sources/{sid}` → 200 + body |
| `fl source delete [repoId] <srcId>` | `DELETE …/sources/{sid}` → 204 |
| `fl harvest list [repoId] <srcId>` | `GET …/sources/{sid}/harvests` (`--include-logs`) |
| `fl record list [--repo <id>]` | `GET /records?repositoryId=` |
| `fl record view <id>` | `GET /records/{id}` |
| `fl record create` | `POST /records` |
| `fl record edit <id>` | `PATCH /records/{id}` → 200 + body |
| `fl record delete <id...>` | `DELETE /records?repositoryId=` + `{ids}` body → 204 |
| `fl plan list` | `GET /plans` |
| `fl plan view <id>` | `GET /plans/{id}` |
| `fl group list` | `GET /groups` (returns `GroupAssignment`s) |
| `fl group create --name <n>` | `POST /groups` |
| `fl group view <id>` | `GET /groups/{id}` |
| `fl group delete <id>` | `DELETE /groups/{id}` → 204 |
| `fl group members <id>` | `GET /groups/{id}/members` |
| `fl group add-member <id> --user <uid> --role <r>` | `POST /groups/{id}/members` |
| `fl group remove-member <id> <userId>` | `DELETE /groups/{id}/members/{uid}` → 204 |
| `fl api <path> [--method M] [--data J]` | raw, correct auth |

`[repoId]` is optional when `-R <id>` / `FL_REPO` is set — the analogue of `bb -R workspace/repo`.

---

## Global Flags & Environment

```
-R, --repo <uuid>   Default repository for source/record/harvest commands
    --json          Raw JSON output
    --limit <n>     Max items to collect across pages (default 50)
-v, --version       Print version
FL_API_URL          API base, e.g. https://feedless.example.com (no /api/v1 suffix)
FL_TOKEN            UserSecret value (overrides keychain)
FL_REPO             Default repository id
```

---

### Task 1: Scaffold, Config, Auth Core, and `fl api`

**Files:**
- Create: `packages/cli/bin/fl`
- Create: `packages/cli/tests/package.json`, `tsconfig.json`, `helpers/server.ts`, `helpers/run.ts`
- Create: `packages/cli/tests/api.test.ts`, `global-flags.test.ts`
- Create: `packages/cli/tests/fixtures/user.json`, `empty-204.json`

**Interfaces produced:**
- `fl_api_url` / `fl_token` — resolve from env → keychain → config file; die(1) with a `fl auth login` hint
- `fl_api_call METHOD PATH [curl-args…]` — echoes body; returns 2 on HTTP ≥400 after printing `error: HTTP <code> — <CODE>: <message> (corrId=…)`
- `fl_paginate PATH LIMIT [query…]` — walks `page=0,1,…` while `.hasMore`, concatenating `.items`, capped at 10 pages
- `fl_die MSG CODE`
- `fl_require_repo` — resolves `-R`/`FL_REPO`/positional

**Security guard (differs from `bb` — document the divergence in a comment):**
```bash
# bb pins api.bitbucket.org and only allows loopback overrides. fl must reach
# arbitrary self-hosted instances, so instead we refuse to put a bearer token
# on the wire in cleartext unless the host is loopback.
if [[ "$FL_API_URL" == http://* ]] && [[ ! "$FL_API_URL" =~ ^http://(localhost|127\.0\.0\.1|\[::1\])(:[0-9]+)?(/|$) ]]; then
  fl_die "refusing to send token over plain http to a non-loopback host: $FL_API_URL" 1
fi
```

- [x] **Step 1: Write the failing tests**

`tests/helpers/server.ts` — express app that records `{method, path, query, headers, body}` into an array and replays a queued fixture; `listen(0)` for a random port. `tests/helpers/run.ts` — `spawn(binPath, args, {env: {FL_API_URL, FL_TOKEN, PATH: fixtures/bin + PATH, HOME: tmpdir}})`.

```ts
// tests/api.test.ts
test('fl api sends Authorization Bearer and hits /api/v1', async () => {
  const s = await mockServer({ body: { id: 'u1' } })
  const r = await runFl(['api', '/user'], { FL_API_URL: s.url, FL_TOKEN: 'tok-abc' })
  assert.equal(r.code, 0)
  assert.equal(s.requests[0].path, '/api/v1/user')
  assert.equal(s.requests[0].headers.authorization, 'Bearer tok-abc')
  assert.equal(s.requests[0].headers.authentication, undefined) // deprecated header must NOT be sent
})

test('fl api --method POST --data sends a JSON body', async () => { /* … */ })

test('surfaces ApiError code/message/corrId on 404', async () => {
  const s = await mockServer({ status: 404, body: { code: 'NOT_FOUND', message: 'no such repo', corrId: 'c1' } })
  const r = await runFl(['api', '/repositories/x'], { FL_API_URL: s.url, FL_TOKEN: 't' })
  assert.equal(r.code, 2)
  assert.match(r.stderr, /HTTP 404 — NOT_FOUND: no such repo \(corrId=c1\)/)
})

test('refuses plain http to a non-loopback host', async () => {
  const r = await runFl(['api', '/user'], { FL_API_URL: 'http://feedless.example.com', FL_TOKEN: 't' })
  assert.equal(r.code, 1)
  assert.match(r.stderr, /refusing to send token over plain http/)
})

test('exits 1 with a login hint when no token is configured', async () => { /* … */ })
```

- [x] **Step 2: Run tests, verify they fail**
- [x] **Step 3: Implement `bin/fl` header, config resolution, `fl_api_call`, `fl_paginate`, `cmd_api`, global-flag parser, `usage()` stub, dispatcher skeleton**
- [x] **Step 4: Run tests, verify they pass**
- [x] **Step 5: `shellcheck packages/cli/bin/fl` clean** — no findings.

---

### Task 2: `fl auth` (login / status / logout)

**Files:** modify `bin/fl`; create `tests/auth.test.ts`, `tests/fixtures/bin/security`

**Behavior:**
- `fl auth login [--url URL] [--token-stdin]` — prompt for base URL and token (or read token from stdin non-interactively, the CI/agent path), verify with `GET /api/v1/user`, then persist. Storage: macOS `security add-generic-password -s org.migor.feedless.cli -a token|url -w`; on non-Darwin, `~/.config/feedless/cli.env` with `chmod 600`. Print the resolved email on success.
- `fl auth status` — `GET /user`; prints `Logged in to <url> as <email> (id, N groups)`; exit 2 on 401.
- `fl auth logout` — delete keychain entries / config file.
- Because there is no REST token endpoint, `login` must print a hint on failure: *"Mint a token with the GraphQL `createUserSecret` mutation or in the web UI → Settings → Secrets."*

- [ ] **Step 1: Write failing tests** — non-interactive login stores via the `security` shim and calls `GET /user`; login with a bad token exits 2 and stores nothing; `auth status` output snapshot; `FL_TOKEN` env overrides the keychain.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement `cmd_auth_login`, `cmd_auth_status`, `cmd_auth_logout`**
- [ ] **Step 4: Verify tests pass**

---

### Task 3: `fl repo` (list, count, view, create, edit, delete)

**Files:** modify `bin/fl`; create `tests/repo.test.ts`, fixtures `repositories.json`, `repository.json`

**Notes:**
- `repo create` accepts either `--from-file <json>` (a `RepositoryCreate` object) or flags `--title --description --product --visibility --refresh-cron --source-url <url>` (repeatable), assembling a single-element array. `product`, `title`, `description`, `sources` are required by the schema — validate before the call and fail with `exit 1`.
- `repo count` requires `--product`; error clearly if omitted.
- `edit` returns 200 + the updated repository → print it (or `--json`). `delete` returns 204 → print `✓ deleted <id>`, no `jq`.
- Table columns for `list`: `ID  TITLE  PRODUCT  VISIBILITY  SOURCES  DOCS  LAST UPDATED`.

- [ ] **Step 1: Write failing tests** — query-param passthrough (`product`, `visibility`, `q`, `page`, `pageSize`); `--limit` paginates while `hasMore` and truncates; create sends a **single object**, not an array; `--product all` is rejected on create but accepted on list/count; edit prints the repository returned by `PATCH`; a `null` counter renders as `-`, not `0`; `--json` emits raw JSON.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement `cmd_repo_*` + `fl_format_repo_list`**
- [ ] **Step 4: Verify tests pass**

---

### Task 4: `fl source` (list, view, create, edit, delete)

**Files:** modify `bin/fl`; create `tests/source.test.ts`, fixture `sources.json`

**Notes:**
- The `ScrapeFlow` schema is deeply nested; a CLI must not require hand-written JSON for the common case. Provide `fl_build_url_flow URL`:
  ```json
  {"sequence":[{"fetch":{"get":{"url":{"literal":"<URL>"}}}},
               {"execute":{"pluginId":"org_feedless_feed","params":{}}}]}
  ```
  (plugin id verified against `FeedlessPlugins.org_feedless_feed` in `packages/server-core/.../pipeline/plugins/FeedPlugin.kt`).
- `source create [repoId] --url <u> [--title <t>] [--tag <t>]…` uses the builder; `--from-file <json>` takes a full `SourceCreate` object for anything more complex. Body is a **single object**.
- `source edit` sends `SourceUpdate` (`--title --tag --disabled/--enabled --flow-file`) → 200 + the updated source.
- Repo id resolution: positional → `-R` → `FL_REPO`, else exit 1.

- [ ] **Step 1: Write failing tests** — `--url` produces exactly the flow JSON above; `--from-file` posts the object as-is; `--disabled`/`--enabled` map to the boolean; missing repo id exits 1 with a `-R` hint; `--like`/`--disabled` query passthrough on list; a flow action with two kinds set is rejected by the server with `400 VALIDATION_ERROR` and `fl` surfaces the field list.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement `cmd_source_*`, `fl_build_url_flow`**
- [ ] **Step 4: Verify tests pass**

---

### Task 5: `fl harvest list` and `fl record` (list, view, create, edit, delete)

**Files:** modify `bin/fl`; create `tests/harvest.test.ts`, `tests/record.test.ts`, fixtures `harvests.json`, `records.json`

**Notes:**
- `harvest list [repoId] <sourceId> [--include-logs]` — table `OK  ADDED  IGNORED  STARTED  FINISHED`; logs only printed when `--include-logs` (they are large).
- `record list` requires `repositoryId` — take it from `--repo`/`-R`/`FL_REPO`.
- `record create --repo <id> --title --url --published-at <epochMillis> [--text --tag]` — `title`, `url`, `publishedAt`, `repositoryId` are required.
- `record delete <id…> --repo <id>` — `DELETE /records?repositoryId=<id>` with body `{"ids":[…]}`. Add `--yes` to skip an interactive confirmation prompt (destructive, plural).
- `record edit` returns 200 + body — print the updated record (the lone PATCH that does).
- Timestamps in output: epoch millis → ISO-8601 via `date -r $((ms/1000))` (BSD) with a GNU `date -d @` fallback.

- [ ] **Step 1: Write failing tests** — delete sends both query param and body and prompts without `--yes`; create rejects a missing `--published-at`; edit prints the returned record; harvest `--include-logs` toggles the query param.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement `cmd_harvest_list`, `cmd_record_*`**
- [ ] **Step 4: Verify tests pass**

---

### Task 6: `fl plan` and `fl group` (+ members)

**Files:** modify `bin/fl`; create `tests/plan.test.ts`, `tests/group.test.ts`, fixtures `plans.json`, `groups.json`

**Notes:**
- `GET /groups` returns `GroupAssignment {id, role, name}` (the caller's memberships), while `GET /groups/{id}` returns `Group {id, name, ownerId}` — different shapes, different table columns. Do not share a formatter.
- `group add-member --role` is constrained to `owner|viewer|editor` — validate client-side and list valid values on error.
- `group delete` / `remove-member` are destructive → confirm unless `--yes`.

- [ ] **Step 1: Write failing tests** — role validation rejects `admin` with exit 1; `group list` vs `group view` formatters; members pagination.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement `cmd_plan_*`, `cmd_group_*`**
- [ ] **Step 4: Verify tests pass**

---

### Task 7: Usage Text, Dispatcher Completion, Install Script, README

**Files:** modify `bin/fl`; create `packages/cli/install.sh`, `packages/cli/README.md`

- [ ] **Step 1: Write failing tests** (`global-flags.test.ts`) — `fl --version` prints `fl <semver>`; `fl` with no args prints usage and exits 0; unknown command exits 1 with usage on stderr; unknown subcommand names the group; every group responds to `--help`.
- [ ] **Step 2: Verify tests fail**
- [ ] **Step 3: Implement**
  - `usage()` with sections: Setup / Auth / Repositories / Sources / Harvests / Records / Plans / Groups / Raw API / Global flags / Environment / Examples — mirroring `bb`'s layout.
  - `install.sh`: check `curl`/`jq` (offer `brew install jq`), `mkdir -p ~/bin`, symlink `bin/fl`, run `fl --version` and `fl auth status`. Idempotent, no `sudo`.
  - `README.md`: install, token minting (GraphQL `createUserSecret` — the one thing REST cannot do), `FL_API_URL` for self-hosted, worked examples, and a Design Decisions section recording: single-file bash, `gh`-flag conventions, no loopback pin (with the cleartext-token guard as compensation), defer syntax to `--help`.
- [ ] **Step 4: Verify tests pass**
- [ ] **Step 5: Full suite green + `shellcheck` clean**

---

### Task 8: End-to-End Smoke Against a Local Server

**Files:** modify `packages/cli/README.md` (record the procedure and results)

- [ ] **Step 1:** Start feedless locally (`docker-compose.yml`), mint a `UserSecret` via the GraphQL `createUserSecret` mutation.
- [ ] **Step 2:** `fl auth login --url http://localhost:8080 --token-stdin` → `fl auth status`.
- [ ] **Step 3:** Exercise one command per group against the real server: `repo create --source-url <feed>` → `repo list` → `source list` → `harvest list` → `record list` → `record delete` → `repo delete`; plus `plan list`, `group list`.
- [ ] **Step 4:** Fix any spec-vs-implementation drift found (the mock server proves request shape, not server acceptance — this step is where real 400s surface). If the server disagrees with `openapi.yaml`, fix the CLI to match the server and file the spec discrepancy in the README.

---

## Open Questions / Risks

1. **Token minting stays on GraphQL.** `fl auth login` can only accept a pasted token. If that friction matters, a follow-up could add `fl auth token create` posting the `createUserSecret` mutation to `/graphql` with a session cookie — deliberately out of scope here.
2. **`GET /repositories/count` requires `product`**, so there is no unfiltered count. Surfaced as a required flag rather than a silent default.
3. **`ScrapeFlow` coverage.** `--url` handles the feed case; everything else needs `--from-file`. Adding flags for click/type/waitFor/extract would double the CLI's size for marginal benefit — revisit only if used.
4. **`hasMore`-only pagination** gives no total count, so `--limit` truncation cannot report "of N". Page cap is 10 (as in `bb`) to bound runaway loops.
5. **`packages/cli` is untested by CI** as specified. If it should run in the pipeline, add a `cli-test` job invoking `npm --prefix packages/cli/tests test` — not included above because no existing workflow runs Node tests for a non-Gradle package.
