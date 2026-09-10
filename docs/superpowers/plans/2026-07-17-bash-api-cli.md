# Feedless Bash API CLI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a bash CLI (`feedless`) that authenticates against the Feedless API and provides CRUD for repositories and sources, plus a polling-based filtered record stream for repositories.

**Architecture:** A thin bash dispatcher (`bin/feedless`) delegates to `lib/*.sh` modules. GraphQL mutations/queries handle repository and source management at `POST /graphql`. Source CRUD is implemented via `createRepositories` / `updateRepository` (sources have no standalone GraphQL mutations). The `stream` subcommand polls the REST feed endpoint `GET /f/{repositoryId}/json` with `where` / `orderByStr` query params and prints new records as NDJSON on stdout.

**Tech Stack:** Bash 4+, `curl`, `jq`, `bats` (tests), Feedless GraphQL API, Feedless REST JSON feed export.

## Global Constraints

- Implementation language: bash only (no Python/Node wrapper).
- Required runtime deps: `bash`, `curl`, `jq`.
- Default API base URL: `http://localhost:8080` (override via `FEEDLESS_API_URL` or config file).
- GraphQL endpoint: `${FEEDLESS_API_URL}/graphql`
- REST feed endpoint: `${FEEDLESS_API_URL}/f/{repositoryId}/json`
- Auth header name is `Authentication` (not `Authorization`): `Authentication: Bearer <jwt>`
- Cookie auth (`TOKEN=<jwt>`) is supported by the server but the CLI uses the header only.
- Sources are nested under repositories in the API; CLI exposes `source` subcommands that map to `updateRepository(data.sources.*)` and `repository.sources` queries.
- Filter JSON for streaming must match `RecordsWhereInput` from `packages/graphql-api/src/main/resources/schema/schema.graphqls` and always include `"repository": {"id": "<repo-id>"}`.
- CLI stdout for data commands: pretty-printed JSON via `jq`. Stream command: one JSON object per line (NDJSON).
- CLI stderr for errors and progress messages.
- Exit code `0` on success, `1` on user error, `2` on API/transport error.
- Test framework: `bats` with mocked `curl` via a stub executable on `PATH`.

---

## File Structure

```
packages/feedless-cli/
├── bin/
│   └── feedless                 # entrypoint + subcommand router
├── lib/
│   ├── config.sh                # env + ~/.config/feedless/config loader
│   ├── graphql.sh               # GraphQL POST helper, error extraction
│   ├── rest.sh                  # REST feed GET helper
│   ├── json.sh                  # shared jq helpers (source flow builder)
│   ├── auth.sh                  # login, token save, secret create
│   ├── repository.sh            # repo CRUD
│   ├── source.sh                # source CRUD (via repo mutations/queries)
│   └── stream.sh                # filtered polling stream
├── test/
│   ├── test_helper.bash         # bats setup, curl stub, temp HOME
│   ├── config.bats
│   ├── graphql.bats
│   ├── auth.bats
│   ├── repository.bats
│   ├── source.bats
│   └── stream.bats
├── README.md                    # install, auth, examples
└── Makefile                     # test, install targets
```

| File | Responsibility |
|------|----------------|
| `bin/feedless` | Parse global flags (`--api-url`, `--token`, `--json`), dispatch subcommands |
| `lib/config.sh` | Resolve `FEEDLESS_API_URL`, `FEEDLESS_TOKEN` from env then config file |
| `lib/graphql.sh` | `feedless_graphql QUERY VARIABLES_JSON` → response JSON; fail on GraphQL errors |
| `lib/rest.sh` | `feedless_feed_get REPO_ID WHERE_JSON ORDERBY_JSON PAGE` → feed JSON |
| `lib/json.sh` | `feedless_build_url_flow URL` → minimal `ScrapeFlowInput` JSON |
| `lib/auth.sh` | `auth login`, `auth status`, `auth secret create` |
| `lib/repository.sh` | `repo list/get/create/update/delete` |
| `lib/source.sh` | `source list/get/add/update/delete` |
| `lib/stream.sh` | `repo stream` polling loop with dedup by record id |

---

### Task 1: CLI Scaffold, Config, and GraphQL Helper

**Files:**
- Create: `packages/feedless-cli/bin/feedless`
- Create: `packages/feedless-cli/lib/config.sh`
- Create: `packages/feedless-cli/lib/graphql.sh`
- Create: `packages/feedless-cli/test/test_helper.bash`
- Create: `packages/feedless-cli/test/graphql.bats`
- Create: `packages/feedless-cli/Makefile`

**Interfaces:**
- Consumes: nothing
- Produces:
  - `feedless_load_config` — sets `FEEDLESS_API_URL`, `FEEDLESS_TOKEN`
  - `feedless_require_token` — exits 1 if token missing
  - `feedless_graphql "$query" "$variables_json"` — echoes response body; exits 2 on HTTP/GraphQL error
  - `feedless_die "$message" "$code"` — prints to stderr and exits

- [ ] **Step 1: Write the failing test**

```bash
# packages/feedless-cli/test/graphql.bats
#!/usr/bin/env bats
load test_helper

@test "feedless_graphql posts query with Authentication header" {
  export FEEDLESS_API_URL="http://example.test"
  export FEEDLESS_TOKEN="tok-abc"
  stub_curl 'echo "{\"data\":{\"ping\":true}}"' 

  run feedless_graphql 'query { ping }' '{}'
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | jq -r '.data.ping')" = "true" ]

  assert_curl_called_with "-H" "Authentication: Bearer tok-abc"
  assert_curl_called_with "-X" "POST"
  assert_curl_called_with "http://example.test/graphql"
}

@test "feedless_graphql exits 2 on graphql errors array" {
  export FEEDLESS_API_URL="http://example.test"
  export FEEDLESS_TOKEN="tok-abc"
  stub_curl 'echo "{\"errors\":[{\"message\":\"nope\"}]}"'

  run feedless_graphql 'query { x }' '{}'
  [ "$status" -eq 2 ]
  [[ "$output" == *"nope"* ]]
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && make test`
Expected: FAIL — `feedless_graphql: command not found` or missing files

- [ ] **Step 3: Write minimal implementation**

```bash
# packages/feedless-cli/lib/config.sh
feedless_config_path() {
  echo "${FEEDLESS_CONFIG:-${XDG_CONFIG_HOME:-$HOME/.config}/feedless/config}"
}

feedless_load_config() {
  local cfg
  cfg="$(feedless_config_path)"
  if [[ -f "$cfg" ]]; then
    # shellcheck disable=SC1090
    source "$cfg"
  fi
  FEEDLESS_API_URL="${FEEDLESS_API_URL:-http://localhost:8080}"
}

feedless_require_token() {
  feedless_load_config
  if [[ -z "${FEEDLESS_TOKEN:-}" ]]; then
    feedless_die "Not authenticated. Run: feedless auth login --email you@example.com --secret-key <key>" 1
  fi
}
```

```bash
# packages/feedless-cli/lib/graphql.sh
feedless_die() {
  echo "feedless: $1" >&2
  exit "${2:-1}"
}

feedless_graphql() {
  local query="$1"
  local variables="${2:-{}}"
  feedless_load_config
  feedless_require_token

  local payload response
  payload="$(jq -n --arg q "$query" --argjson v "$variables" '{query: $q, variables: $v}')"

  if ! response="$(curl -sS -X POST "${FEEDLESS_API_URL}/graphql" \
    -H "Content-Type: application/json" \
    -H "Authentication: Bearer ${FEEDLESS_TOKEN}" \
    -d "$payload")"; then
    feedless_die "GraphQL request failed" 2
  fi

  if echo "$response" | jq -e '.errors | length > 0' >/dev/null 2>&1; then
    echo "$response" | jq -r '.errors[].message' >&2
    exit 2
  fi

  echo "$response"
}
```

```bash
# packages/feedless-cli/bin/feedless
#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# shellcheck source=../lib/config.sh
source "${ROOT}/lib/config.sh"
# shellcheck source=../lib/graphql.sh
source "${ROOT}/lib/graphql.sh"

usage() {
  cat <<'EOF'
Usage: feedless [--api-url URL] [--token TOKEN] <command> [args]

Commands:
  auth      Authenticate and manage tokens
  repo      Manage repositories
  source    Manage sources inside repositories
  help      Show help
EOF
}

main() {
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --api-url) FEEDLESS_API_URL="$2"; shift 2 ;;
      --token) FEEDLESS_TOKEN="$2"; shift 2 ;;
      -h|--help|help) usage; exit 0 ;;
      auth|repo|source) break ;;
      *) feedless_die "Unknown argument: $1" 1 ;;
    esac
  done

  local cmd="${1:-}"
  shift || true
  case "$cmd" in
    auth) source "${ROOT}/lib/auth.sh"; feedless_auth "$@" ;;
    repo) source "${ROOT}/lib/repository.sh"; feedless_repo "$@" ;;
    source) source "${ROOT}/lib/source.sh"; feedless_source "$@" ;;
    *) usage; exit 1 ;;
  esac
}

main "$@"
```

```makefile
# packages/feedless-cli/Makefile
SHELL := /bin/bash
export PATH := $(CURDIR)/test/stubs:$(PATH)

test:
	bats test/

install:
	install -d "$(DESTDIR)$(prefix)/bin" "$(DESTDIR)$(prefix)/lib/feedless"
	install -m 755 bin/feedless "$(DESTDIR)$(prefix)/bin/feedless"
	cp -R lib/* "$(DESTDIR)$(prefix)/lib/feedless/"
	sed -i.bak 's|source "${ROOT}/lib/|source "$(prefix)/lib/feedless/|g' "$(DESTDIR)$(prefix)/bin/feedless" && rm -f "$(DESTDIR)$(prefix)/bin/feedless.bak"
```

`test/test_helper.bash` must provide `stub_curl` that writes a `curl` executable to `test/stubs/curl` recording args and returning canned output.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && make test`
Expected: PASS for `graphql.bats` (2 tests)

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/
git commit -m "feat(cli): scaffold bash CLI with config and GraphQL helper"
```

---

### Task 2: Auth Commands

**Files:**
- Create: `packages/feedless-cli/lib/auth.sh`
- Create: `packages/feedless-cli/test/auth.bats`
- Modify: `packages/feedless-cli/bin/feedless` (wire `auth` — already stubbed)

**Interfaces:**
- Consumes: `feedless_graphql`, `feedless_load_config`, `feedless_config_path`
- Produces:
  - `feedless_auth login --email EMAIL --secret-key KEY` — calls `authUser`, saves token to config
  - `feedless_auth status` — prints masked token + API URL
  - `feedless_auth secret create` — calls `createUserSecret`, prints secret once

- [ ] **Step 1: Write the failing test**

```bash
# packages/feedless-cli/test/auth.bats
#!/usr/bin/env bats
load test_helper

setup() {
  export HOME="$BATS_TEST_TMPDIR/home"
  mkdir -p "$HOME/.config/feedless"
}

@test "auth login saves token to config file" {
  stub_curl 'echo "{\"data\":{\"authUser\":{\"token\":\"jwt-123\",\"corrId\":\"c1\"}}}"'
  export FEEDLESS_API_URL="http://example.test"

  run "${FEEDLESS_BIN}" auth login --email "a@b.c" --secret-key "sekret"
  [ "$status" -eq 0 ]
  grep -q 'FEEDLESS_TOKEN=jwt-123' "$HOME/.config/feedless/config"
  [[ "$output" == *"Logged in"* ]]
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && bats test/auth.bats`
Expected: FAIL — `feedless_auth: command not found`

- [ ] **Step 3: Write minimal implementation**

```bash
# packages/feedless-cli/lib/auth.sh
feedless_save_token() {
  local token="$1"
  local cfg
  cfg="$(feedless_config_path)"
  mkdir -p "$(dirname "$cfg")"
  {
    echo "FEEDLESS_API_URL=${FEEDLESS_API_URL}"
    echo "FEEDLESS_TOKEN=${token}"
  } >"$cfg"
  chmod 600 "$cfg"
}

feedless_auth() {
  local sub="${1:-}"
  shift || true
  case "$sub" in
    login)
      local email="" secret_key=""
      while [[ $# -gt 0 ]]; do
        case "$1" in
          --email) email="$2"; shift 2 ;;
          --secret-key) secret_key="$2"; shift 2 ;;
          *) feedless_die "Unknown login arg: $1" 1 ;;
        esac
      done
      [[ -n "$email" && -n "$secret_key" ]] || feedless_die "Usage: feedless auth login --email EMAIL --secret-key KEY" 1
      feedless_load_config
      local resp token
      resp="$(feedless_graphql \
        'mutation($data: AuthUserInput!){ authUser(data:$data){ token corrId } }' \
        "$(jq -n --arg e "$email" --arg s "$secret_key" '{data:{email:$e, secretKey:$s}}')")"
      token="$(echo "$resp" | jq -r '.data.authUser.token')"
      feedless_save_token "$token"
      echo "Logged in. Token saved to $(feedless_config_path)"
      ;;
    status)
      feedless_load_config
      echo "api_url=${FEEDLESS_API_URL}"
      if [[ -n "${FEEDLESS_TOKEN:-}" ]]; then
        echo "token=$(printf '%s' "$FEEDLESS_TOKEN" | sed 's/./*/g' | head -c 20)..."
      else
        echo "token=<not set>"
      fi
      ;;
    secret)
      local action="${1:-}"
      shift || true
      [[ "$action" == "create" ]] || feedless_die "Usage: feedless auth secret create" 1
      feedless_require_token
      local resp
      resp="$(feedless_graphql \
        'mutation { createUserSecret { id value type } }' \
        '{}')"
      echo "$resp" | jq '.data.createUserSecret'
      echo "Save this secret now; it will not be shown again." >&2
      ;;
    *)
      feedless_die "Usage: feedless auth {login|status|secret create}" 1
      ;;
  esac
}
```

Update `test/test_helper.bash` to set `FEEDLESS_BIN="${BATS_TEST_DIRNAME}/../bin/feedless"` and export `PATH` with stubs **before** sourcing libs.

For login test, `feedless_graphql` calls `feedless_require_token` — login must bypass that. Fix `feedless_graphql` to accept optional third arg `--no-auth`:

```bash
feedless_graphql() {
  local query="$1"
  local variables="${2:-{}}"
  local skip_auth="${3:-}"
  feedless_load_config
  if [[ "$skip_auth" != "--no-auth" ]]; then
    feedless_require_token
  fi
  # ... rest unchanged, but omit Authorization header when no token and --no-auth
}
```

In `auth login`, call: `feedless_graphql '...' '...' --no-auth`

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && bats test/auth.bats`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/lib/auth.sh packages/feedless-cli/test/auth.bats packages/feedless-cli/lib/graphql.sh packages/feedless-cli/test/test_helper.bash
git commit -m "feat(cli): add auth login, status, and secret create"
```

---

### Task 3: Repository List and Get

**Files:**
- Create: `packages/feedless-cli/lib/repository.sh`
- Create: `packages/feedless-cli/test/repository.bats`

**Interfaces:**
- Consumes: `feedless_graphql`
- Produces:
  - `feedless_repo list [--product VERTICAL] [--page N]`
  - `feedless_repo get <repository-id>`

- [ ] **Step 1: Write the failing test**

```bash
@test "repo list prints repositories array" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"repositories\":[{\"id\":\"r1\",\"title\":\"News\"}]}}"'
  run "${FEEDLESS_BIN}" --token t repo list
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | jq -r '.[0].id')" = "r1" ]
}

@test "repo get fetches single repository" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"repository\":{\"id\":\"r1\",\"title\":\"News\",\"sourcesCount\":2}}}"'
  run "${FEEDLESS_BIN}" --token t repo get r1
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | jq -r '.id')" = "r1" ]
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && bats test/repository.bats -f "repo list"`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```bash
# packages/feedless-cli/lib/repository.sh
feedless_repo() {
  local sub="${1:-}"
  shift || true
  case "$sub" in
    list) feedless_repo_list "$@" ;;
    get) feedless_repo_get "$@" ;;
    *) feedless_die "Usage: feedless repo {list|get|create|update|delete|stream}" 1 ;;
  esac
}

feedless_repo_list() {
  local product="" page=0
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --product) product="$2"; shift 2 ;;
      --page) page="$2"; shift 2 ;;
      *) feedless_die "Unknown list arg: $1" 1 ;;
    esac
  done
  local where='null'
  if [[ -n "$product" ]]; then
    where="$(jq -n --arg p "$product" '{product:{eq:$p}}')"
  fi
  local vars
  vars="$(jq -n --argjson page "$page" --argjson where "$where" \
    '{data:{cursor:{page:$page, pageSize:50}, where:$where}}')"
  local resp
  resp="$(feedless_graphql \
    'query($data: RepositoriesInput!){ repositories(data:$data){ id title description product visibility sourcesCount createdAt lastUpdatedAt } }' \
    "$vars")"
  echo "$resp" | jq '.data.repositories'
}

feedless_repo_get() {
  local id="${1:-}"
  [[ -n "$id" ]] || feedless_die "Usage: feedless repo get <repository-id>" 1
  local vars
  vars="$(jq -n --arg id "$id" '{repository:{where:{id:$id}}, cursor:{page:0, pageSize:50}}')"
  local resp
  resp="$(feedless_graphql \
    'query($repository: RepositoryWhereInput!, $cursor: Cursor!){
      repository(data:$repository){
        id title description product visibility sourcesCount createdAt lastUpdatedAt refreshCron tags
        sources(cursor:$cursor){ id title disabled tags recordCount lastRefreshedAt }
      }
    }' \
    "$vars")"
  echo "$resp" | jq '.data.repository'
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && bats test/repository.bats`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/lib/repository.sh packages/feedless-cli/test/repository.bats
git commit -m "feat(cli): add repository list and get commands"
```

---

### Task 4: Repository Create, Update, Delete

**Files:**
- Modify: `packages/feedless-cli/lib/repository.sh`
- Modify: `packages/feedless-cli/test/repository.bats`

**Interfaces:**
- Consumes: `feedless_graphql`, `feedless_build_url_flow` (from Task 6 — implement inline here first, extract in Task 6)
- Produces:
  - `feedless_repo create --title T --description D [--product feedless] [--url URL]`
  - `feedless_repo update <id> [--title T] [--description D] [--refresh-cron CRON]`
  - `feedless_repo delete <id>`

- [ ] **Step 1: Write the failing test**

```bash
@test "repo create sends createRepositories mutation" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"createRepositories\":[{\"id\":\"new-r\",\"title\":\"My Repo\"}]}}"'
  run "${FEEDLESS_BIN}" --token t repo create --title "My Repo" --description "d" --url "https://example.com"
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | jq -r '.[0].id')" = "new-r" ]
}

@test "repo delete sends deleteRepository mutation" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"deleteRepository\":true}}"'
  run "${FEEDLESS_BIN}" --token t repo delete r1
  [ "$status" -eq 0 ]
  [[ "$output" == *"deleted"* ]]
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && bats test/repository.bats -f "repo create"`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```bash
feedless_repo_create() {
  local title="" description="" product="feedless" url=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --title) title="$2"; shift 2 ;;
      --description) description="$2"; shift 2 ;;
      --product) product="$2"; shift 2 ;;
      --url) url="$2"; shift 2 ;;
      *) feedless_die "Unknown create arg: $1" 1 ;;
    esac
  done
  [[ -n "$title" && -n "$description" ]] || feedless_die "Usage: feedless repo create --title T --description D [--url URL]" 1

  local sources='[]'
  if [[ -n "$url" ]]; then
    local flow
    flow="$(jq -n --arg u "$url" '{
      sequence: [{ fetch: { get: { url: { literal: $u } } } }]
    }')"
    sources="$(jq -n --arg t "$title" --argjson flow "$flow" '[{title:$t, flow:$flow}]')"
  fi

  local vars
  vars="$(jq -n --arg title "$title" --arg desc "$description" --arg product "$product" --argjson sources "$sources" \
    '{data:[{title:$title, description:$desc, product:$product, sources:$sources}]}')"

  local resp
  resp="$(feedless_graphql \
    'mutation($data: [RepositoryCreateInput!]!){ createRepositories(data:$data){ id title description product } }' \
    "$vars")"
  echo "$resp" | jq '.data.createRepositories'
}

feedless_repo_update() {
  local id="${1:-}"; shift || true
  [[ -n "$id" ]] || feedless_die "Usage: feedless repo update <id> [--title T] [--description D]" 1
  local title="" description="" refresh_cron=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --title) title="$2"; shift 2 ;;
      --description) description="$2"; shift 2 ;;
      --refresh-cron) refresh_cron="$2"; shift 2 ;;
      *) feedless_die "Unknown update arg: $1" 1 ;;
    esac
  done
  local data='{}'
  if [[ -n "$title" ]]; then data="$(echo "$data" | jq --arg v "$title" '.title={set:$v}')"; fi
  if [[ -n "$description" ]]; then data="$(echo "$data" | jq --arg v "$description" '.description={set:$v}')"; fi
  if [[ -n "$refresh_cron" ]]; then data="$(echo "$data" | jq --arg v "$refresh_cron" '.refreshCron={set:$v}')"; fi

  local vars
  vars="$(jq -n --arg id "$id" --argjson data "$data" '{data:{where:{id:$id}, data:$data}}')"
  feedless_graphql \
    'mutation($data: RepositoryUpdateInput!){ updateRepository(data:$data) }' \
    "$vars" >/dev/null
  echo "Repository $id updated"
}

feedless_repo_delete() {
  local id="${1:-}"
  [[ -n "$id" ]] || feedless_die "Usage: feedless repo delete <repository-id>" 1
  local vars
  vars="$(jq -n --arg id "$id" '{data:{id:$id}}')"
  feedless_graphql \
    'mutation($data: RepositoryUniqueWhereInput!){ deleteRepository(data:$data) }' \
    "$vars" >/dev/null
  echo "Repository $id deleted"
}
```

Wire `create|update|delete` cases in `feedless_repo` dispatcher.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && bats test/repository.bats`
Expected: PASS (all repo tests)

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/lib/repository.sh packages/feedless-cli/test/repository.bats
git commit -m "feat(cli): add repository create, update, and delete"
```

---

### Task 5: Source List, Get, Add, Update, Delete

**Files:**
- Create: `packages/feedless-cli/lib/source.sh`
- Create: `packages/feedless-cli/lib/json.sh`
- Create: `packages/feedless-cli/test/source.bats`

**Interfaces:**
- Consumes: `feedless_graphql`, `feedless_build_url_flow`
- Produces:
  - `feedless_source list <repository-id>`
  - `feedless_source get <repository-id> <source-id>`
  - `feedless_source add <repository-id> --title T --url URL`
  - `feedless_source update <repository-id> <source-id> [--title T] [--disabled true|false]`
  - `feedless_source delete <repository-id> <source-id>`

- [ ] **Step 1: Write the failing test**

```bash
@test "source add calls updateRepository with sources.add" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"updateRepository\":true}}"'
  run "${FEEDLESS_BIN}" --token t source add r1 --title "Site" --url "https://news.example"
  [ "$status" -eq 0 ]
  [[ "$output" == *"added"* ]]
  assert_curl_body_contains '"sources"'
  assert_curl_body_contains '"add"'
}

@test "source list returns sources array" {
  export FEEDLESS_TOKEN="t"
  stub_curl 'echo "{\"data\":{\"repository\":{\"sources\":[{\"id\":\"s1\",\"title\":\"A\"}]}}}"'
  run "${FEEDLESS_BIN}" --token t source list r1
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | jq -r '.[0].id')" = "s1" ]
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && bats test/source.bats`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```bash
# packages/feedless-cli/lib/json.sh
feedless_build_url_flow() {
  local url="$1"
  jq -n --arg u "$url" '{
    sequence: [{ fetch: { get: { url: { literal: $u } } } }]
  }'
}
```

```bash
# packages/feedless-cli/lib/source.sh
feedless_source() {
  local sub="${1:-}"
  shift || true
  case "$sub" in
    list) feedless_source_list "$@" ;;
    get) feedless_source_get "$@" ;;
    add) feedless_source_add "$@" ;;
    update) feedless_source_update "$@" ;;
    delete) feedless_source_delete "$@" ;;
    *) feedless_die "Usage: feedless source {list|get|add|update|delete}" 1 ;;
  esac
}

feedless_source_list() {
  local repo_id="${1:-}"
  [[ -n "$repo_id" ]] || feedless_die "Usage: feedless source list <repository-id>" 1
  local vars
  vars="$(jq -n --arg id "$repo_id" '{repository:{where:{id:$id}}, cursor:{page:0, pageSize:100}}')"
  local resp
  resp="$(feedless_graphql \
    'query($repository: RepositoryWhereInput!, $cursor: Cursor!){
      repository(data:$repository){ sources(cursor:$cursor){ id title disabled tags recordCount lastRefreshedAt } }
    }' \
    "$vars")"
  echo "$resp" | jq '.data.repository.sources'
}

feedless_source_get() {
  local repo_id="${1:-}" source_id="${2:-}"
  [[ -n "$repo_id" && -n "$source_id" ]] || feedless_die "Usage: feedless source get <repository-id> <source-id>" 1
  local vars
  vars="$(jq -n --arg rid "$repo_id" --arg sid "$source_id" \
    '{repository:{where:{id:$rid}}, cursor:{page:0}, where:{id:{eq:$sid}}}')"
  local resp
  resp="$(feedless_graphql \
    'query($repository: RepositoryWhereInput!, $cursor: Cursor!, $where: SourcesWhereInput){
      repository(data:$repository){ sources(cursor:$cursor, where:$where){ id title disabled tags flow { sequence { fetch { get { url { literal } } } } } } }
    }' \
    "$vars")"
  echo "$resp" | jq '.data.repository.sources[0]'
}

feedless_source_add() {
  local repo_id="${1:-}"; shift || true
  local title="" url=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --title) title="$2"; shift 2 ;;
      --url) url="$2"; shift 2 ;;
      *) feedless_die "Unknown add arg: $1" 1 ;;
    esac
  done
  [[ -n "$repo_id" && -n "$title" && -n "$url" ]] || feedless_die "Usage: feedless source add <repository-id> --title T --url URL" 1
  local flow add_json vars
  flow="$(feedless_build_url_flow "$url")"
  add_json="$(jq -n --arg t "$title" --argjson flow "$flow" '[{title:$t, flow:$flow}]')"
  vars="$(jq -n --arg id "$repo_id" --argjson add "$add_json" '{data:{where:{id:$id}, data:{sources:{add:$add}}}}')"
  feedless_graphql \
    'mutation($data: RepositoryUpdateInput!){ updateRepository(data:$data) }' \
    "$vars" >/dev/null
  echo "Source added to repository $repo_id"
}

feedless_source_update() {
  local repo_id="${1:-}" source_id="${2:-}"; shift 2 || true
  local title="" disabled=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --title) title="$2"; shift 2 ;;
      --disabled) disabled="$2"; shift 2 ;;
      *) feedless_die "Unknown update arg: $1" 1 ;;
    esac
  done
  local src_data='{}'
  if [[ -n "$title" ]]; then src_data="$(echo "$src_data" | jq --arg v "$title" '.title={set:$v}')"; fi
  if [[ -n "$disabled" ]]; then src_data="$(echo "$src_data" | jq --argjson v "$disabled" '.disabled={set:$v}')"; fi
  local update_json vars
  update_json="$(jq -n --arg sid "$source_id" --argjson data "$src_data" '[{where:{id:$sid}, data:$data}]')"
  vars="$(jq -n --arg id "$repo_id" --argjson upd "$update_json" '{data:{where:{id:$id}, data:{sources:{update:$upd}}}}')"
  feedless_graphql \
    'mutation($data: RepositoryUpdateInput!){ updateRepository(data:$data) }' \
    "$vars" >/dev/null
  echo "Source $source_id updated"
}

feedless_source_delete() {
  local repo_id="${1:-}" source_id="${2:-}"
  [[ -n "$repo_id" && -n "$source_id" ]] || feedless_die "Usage: feedless source delete <repository-id> <source-id>" 1
  local vars
  vars="$(jq -n --arg id "$repo_id" --arg sid "$source_id" \
    '{data:{where:{id:$id}, data:{sources:{remove:[$sid]}}}}')"
  feedless_graphql \
    'mutation($data: RepositoryUpdateInput!){ updateRepository(data:$data) }' \
    "$vars" >/dev/null
  echo "Source $source_id deleted from repository $repo_id"
}
```

Add `assert_curl_body_contains` helper to `test_helper.bash` that inspects the recorded curl `-d` payload.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && bats test/source.bats`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/lib/source.sh packages/feedless-cli/lib/json.sh packages/feedless-cli/test/source.bats packages/feedless-cli/test/test_helper.bash
git commit -m "feat(cli): add source CRUD via repository mutations"
```

---

### Task 6: REST Helper and Filtered Repository Stream

**Files:**
- Create: `packages/feedless-cli/lib/rest.sh`
- Create: `packages/feedless-cli/lib/stream.sh`
- Create: `packages/feedless-cli/test/stream.bats`
- Modify: `packages/feedless-cli/lib/repository.sh` (add `stream` subcommand)

**Interfaces:**
- Consumes: `feedless_load_config`, `feedless_require_token`
- Produces:
  - `feedless_feed_get "$repo_id" "$where_json" "$order_json" "$page"` → JSON feed body
  - `feedless_repo_stream "$repo_id"` with flags:
    - `--where JSON` full `RecordsWhereInput` (repository id injected if missing)
    - `--tag TAG` shorthand → `tags.eq`
    - `--source-id ID` shorthand → `source.id`
    - `--since EPOCH_MS` shorthand → `startedAt.after`
    - `--interval SEC` poll interval (default `30`)
    - `--once` single poll then exit (for tests)

**API note:** The server has no SSE/WebSocket feed stream for repositories. This command implements a **polling stream**: repeated `GET /f/{id}/json` calls, emitting only records not yet seen.

- [ ] **Step 1: Write the failing test**

```bash
@test "stream prints only new items as ndjson" {
  export FEEDLESS_TOKEN="t"
  export FEEDLESS_API_URL="http://example.test"
  # First poll: item a1; second poll: a1 + a2
  stub_curl_sequence \
    '{"items":[{"id":"a1","title":"One","url":"u1"}]}' \
    '{"items":[{"id":"a1","title":"One","url":"u1"},{"id":"a2","title":"Two","url":"u2"}]}'

  run feedless_repo_stream "r1" --interval 0 --once
  [ "$status" -eq 0 ]
  [ "$(echo "$output" | wc -l | tr -d ' ')" -eq 1 ]
  [ "$(echo "$output" | jq -r '.id')" = "a1" ]

  run feedless_repo_stream "r1" --interval 0 --once
  [ "$(echo "$output" | jq -r '.id')" = "a2" ]
}
```

Implement `stub_curl_sequence` in test helper returning different bodies per invocation. Use a state file `~/.cache/feedless/stream-r1.ids` for dedup (test uses `$BATS_TEST_TMPDIR` via `FEEDLESS_CACHE_DIR`).

- [ ] **Step 2: Run test to verify it fails**

Run: `cd packages/feedless-cli && bats test/stream.bats`
Expected: FAIL

- [ ] **Step 3: Write minimal implementation**

```bash
# packages/feedless-cli/lib/rest.sh
feedless_feed_get() {
  local repo_id="$1"
  local where_json="${2:-}"
  local order_json="${3:-}"
  local page="${4:-0}"
  feedless_load_config

  local args=(-sS -G "${FEEDLESS_API_URL}/f/${repo_id}/json")
  args+=(--data-urlencode "page=${page}")
  if [[ -n "$where_json" && "$where_json" != "null" ]]; then
    args+=(--data-urlencode "where=${where_json}")
  fi
  if [[ -n "$order_json" && "$order_json" != "null" ]]; then
    args+=(--data-urlencode "orderByStr=${order_json}")
  fi
  if [[ -n "${FEEDLESS_TOKEN:-}" ]]; then
    args+=(-H "Authentication: Bearer ${FEEDLESS_TOKEN}")
  fi

  curl "${args[@]}"
}
```

```bash
# packages/feedless-cli/lib/stream.sh
feedless_cache_dir() {
  echo "${FEEDLESS_CACHE_DIR:-${XDG_CACHE_HOME:-$HOME/.cache}/feedless}"
}

feedless_build_where_filter() {
  local repo_id="$1"
  shift
  local where='{}' tag="" source_id="" since=""
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --where) where="$(echo "$2" | jq -c .)"; shift 2 ;;
      --tag) tag="$2"; shift 2 ;;
      --source-id) source_id="$2"; shift 2 ;;
      --since) since="$2"; shift 2 ;;
      *) shift ;;
    esac
  done
  where="$(echo "$where" | jq -c --arg id "$repo_id" '.repository = (.repository // {id:$id}) | .repository.id = $id')"
  if [[ -n "$tag" ]]; then where="$(echo "$where" | jq -c --arg t "$tag" '.tags = {eq:$t}')"; fi
  if [[ -n "$source_id" ]]; then where="$(echo "$where" | jq -c --arg s "$source_id" '.source = {id:$s}')"; fi
  if [[ -n "$since" ]]; then where="$(echo "$where" | jq -c --argjson ms "$since" '.startedAt = (.startedAt // {}) | .startedAt.after = $ms')"; fi
  echo "$where"
}

feedless_repo_stream() {
  local repo_id="${1:-}"; shift || true
  [[ -n "$repo_id" ]] || feedless_die "Usage: feedless repo stream <repository-id> [--where JSON] [--tag TAG] [--interval 30]" 1
  feedless_require_token

  local interval=30 once=0
  local filter_args=()
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --interval) interval="$2"; shift 2 ;;
      --once) once=1; shift ;;
      --where|--tag|--source-id|--since)
        filter_args+=("$1" "$2"); shift 2 ;;
      *) feedless_die "Unknown stream arg: $1" 1 ;;
    esac
  done

  local where order='{"startedAt":"desc"}'
  where="$(feedless_build_where_filter "$repo_id" "${filter_args[@]}")"

  local cache state
  cache="$(feedless_cache_dir)"
  mkdir -p "$cache"
  state="${cache}/stream-${repo_id}.ids"

  local emit_new() {
    local feed
    feed="$(feedless_feed_get "$repo_id" "$where" "$order" 0)"
    echo "$feed" | jq -c '.items[]?' | while read -r item; do
      local id
      id="$(echo "$item" | jq -r '.id')"
      if ! grep -qxF "$id" "$state" 2>/dev/null; then
        echo "$id" >>"$state"
        echo "$item"
      fi
    done
  }

  if [[ "$once" -eq 1 ]]; then
    emit_new
    return 0
  fi

  echo "Streaming repository $repo_id (poll every ${interval}s). Ctrl-C to stop." >&2
  while true; do
    emit_new
    sleep "$interval"
  done
}
```

Add to `feedless_repo` dispatcher: `stream) feedless_repo_stream "$@" ;;` and source `stream.sh` at top of `repository.sh`.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd packages/feedless-cli && bats test/stream.bats`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/lib/rest.sh packages/feedless-cli/lib/stream.sh packages/feedless-cli/lib/repository.sh packages/feedless-cli/test/stream.bats
git commit -m "feat(cli): add filtered repository record stream via REST polling"
```

---

### Task 7: README, Install Target, and End-to-End Smoke Script

**Files:**
- Create: `packages/feedless-cli/README.md`
- Create: `packages/feedless-cli/test/smoke.sh` (manual/integration, not bats)
- Modify: `packages/feedless-cli/Makefile` (`install`, `smoke` targets)
- Modify: root `README.md` (add CLI section with link)

**Interfaces:**
- Consumes: all prior modules
- Produces: documented user workflow from zero to streaming

- [ ] **Step 1: Write README with exact commands**

```markdown
# Feedless CLI

Bash client for the Feedless GraphQL and REST APIs.

## Requirements

- bash 4+
- curl
- jq

## Install

```bash
cd packages/feedless-cli
make install prefix=$HOME/.local
export PATH="$HOME/.local/bin:$PATH"
```

## Quick start

```bash
# 1. Create API secret in the web UI or via GraphQL, then:
feedless auth login --email you@example.com --secret-key "<secret>"

# 2. Create a repository with one URL source
feedless repo create --title "News" --description "Example" \
  --url "https://example.com/feed.xml"

# 3. List repositories
feedless repo list

# 4. Add another source
feedless source add <repo-id> --title "Blog" --url "https://example.com/blog"

# 5. Stream new records (filtered)
feedless repo stream <repo-id> --tag important --interval 15
```

## Environment

| Variable | Default | Description |
|----------|---------|-------------|
| `FEEDLESS_API_URL` | `http://localhost:8080` | API base URL |
| `FEEDLESS_TOKEN` | from config file | JWT bearer token |
| `FEEDLESS_CONFIG` | `~/.config/feedless/config` | Config file path |
| `FEEDLESS_CACHE_DIR` | `~/.cache/feedless` | Stream dedup state |
```

- [ ] **Step 2: Add smoke script (skipped in CI by default)**

```bash
#!/usr/bin/env bash
# packages/feedless-cli/test/smoke.sh
set -euo pipefail
: "${FEEDLESS_API_URL:=http://localhost:8080}"
: "${FEEDLESS_EMAIL:?}"
: "${FEEDLESS_SECRET_KEY:?}"

BIN="$(cd "$(dirname "$0")/.." && pwd)/bin/feedless"
"$BIN" auth login --email "$FEEDLESS_EMAIL" --secret-key "$FEEDLESS_SECRET_KEY"
"$BIN" repo list | jq .
echo "Smoke OK"
```

- [ ] **Step 3: Run full test suite**

Run: `cd packages/feedless-cli && make test`
Expected: all bats tests PASS

- [ ] **Step 4: Add root README pointer**

Add under Local Development Setup in `/Users/markus.ruepp/dev2/feedless/README.md`:

```markdown
### CLI

See [packages/feedless-cli/README.md](packages/feedless-cli/README.md) for the bash API client.
```

- [ ] **Step 5: Commit**

```bash
git add packages/feedless-cli/README.md packages/feedless-cli/test/smoke.sh packages/feedless-cli/Makefile README.md
git commit -m "docs(cli): add README, smoke script, and root README link"
```

---

## Self-Review

### Spec coverage

| Requirement | Task |
|-------------|------|
| Bash implementation | Tasks 1–7 |
| CRUD repositories | Tasks 3–4 |
| CRUD sources | Task 5 |
| Subscribe with filters as stream | Task 6 (`repo stream` with `--where`, `--tag`, `--source-id`, `--since`) |
| API interaction | Tasks 1–6 (GraphQL + REST) |

### Placeholder scan

No TBD/TODO/similar-to placeholders present.

### Type consistency

- GraphQL variable shapes match `schema.graphqls` (`RepositoryCreateInput`, `RepositoryUpdateInput`, `SourcesUpdateInput`, `RecordsWhereInput`).
- Auth header consistently `Authentication: Bearer`.
- `feedless_graphql` / `feedless_feed_get` used across all data commands.

### Known limitation (documented in plan)

Sources have no standalone GraphQL CRUD; the CLI maps user-facing `source` commands to `updateRepository` / `repository.sources` as the API requires.

---

Plan complete and saved to `docs/superpowers/plans/2026-07-17-bash-api-cli.md`. Two execution options:

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?
