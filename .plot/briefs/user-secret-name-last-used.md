## Implementation brief — user-secret-name-last-used

- **Plan (canonical):** `docs/plans/2026-09-15-user-secret-name-last-used.md` on `feature/user-secret-name-last-used`
- **Approved:** 2026-09-15, damoeb, in-session
- **Branch:** `feature/user-secret-name-last-used` (base: `develop`)
- **Ends as:** one PR to `develop` carrying plan + code; body links the plan and mirrors its approval record
- **Review of the code:** per repo convention (PR review, CI runs on PRs to `develop`)

### What to build

API tokens (`t_user_secret` rows of type `SecretKey` holding an API JWT) have four visible defects:

- There's no name.
- Every masked value reads `eyJhb****`, because `UserSecretMapper.maskValue` keeps the first 5 characters and every JWT header starts with those.
- `last_used_at` never moves for API calls.
- Deleting a secret doesn't revoke its token.

The fix, on top of the existing pieces:

- a `name` column (V94),
- a `secret_id` claim written by `JwtTokenIssuer.createJwtForApi`,
- a check in `TokenAuthenticator` through a new `AuthService.useApiSecret` that rejects missing, deleted, or foreign secrets and bumps `last_used_at` at most once a minute,
- a mask that shows the last 6 characters,
- a required name in both UIs.

The plan is canonical; this brief is orientation.

### Decisions the plan settles — do not re-derive them

- **The check lives in `TokenAuthenticator`, not in a filter or listener.** Both `HttpApiJwtFilter` and `JwtRequestFilter` already call `tokenAuthenticator.authenticate(jwt, request)`, so one check covers `/api/v1` and GraphQL. A separate filter would need wiring into both chains.
- **No cache.** A cache delays revocation and diverges across core instances. A primary-key read per API request is cheap. Writes are bounded by a conditional `UPDATE … WHERE last_used_at IS NULL OR last_used_at < :threshold`. Don't read then decide in Kotlin; let the `WHERE` clause throttle.
- **Go through `AuthService`, not `UserSecretRepository` directly.** `TokenAuthenticator` is profiled `session & service`, while `UserSecretRepository` is `secrets & repository`. Injecting the repository would break every test context that loads the filters without the `secrets` profile, and the failure shows up as "no such bean" (AGENTS.md rule 1). `AuthService` already has the stateful/stateless split: `StatefulAuthService` holds the repository, and `StatelessAuthService` (`@ConditionalOnMissingBean`) returns false.
- **Legacy tokens without `secret_id` are rejected.** There's no value-lookup fallback; the user chose this explicitly. It applies only when `token_type == API`. USER, ANON and AGENT tokens must never reach `useApiSecret`.
- **New claim name `secret_id`, not `id`.** `encodeJwt` always sets `id = "feedless"`, and `jwtToOAuth2AuthenticationToken` requires it as the client registration id.
- **`validUntil` is computed from `getExpiration(AuthTokenType.SERVICE)`**, the lifetime `createJwtForApi` actually uses. Today it's computed from `USER` (48h), which is the bug.
- **Owner must match.** The row's `ownerId` must equal the token's `UserCapability`; a mismatch is rejected like a missing row.
- **Value-based logins stay untouched.** `findBySecretKeyValue` (root login in `StatefulAuthService.authenticateUser`, agent login in `BrowserAutomationService`) keeps working.

Repo invariants to carry over:

- Never hand-edit generated code. Regenerate `schema.graphqls` → Kotlin DGS + `app-web` `yarn codegen` + `frontend` `@feedless/graphql-api` codegen. An `openapi.yaml` change means `go generate` in `packages/cli`.
- Never edit a shipped migration. Add `V94` and raise `spring.flyway.target` to 94 in the same commit.
- Every new bean needs a `@Profile`, and every test needs the matching `@ActiveProfiles`.
- Comments are one line and say only why.

### Done when

The plan's Design → Testing list is the specification. These assertions exist because a naive implementation would pass without them:

- **`useApiSecret` is never called for non-API tokens.** Catches a check that fires on every JWT and locks out browser sessions, anonymous feeds and agents.
- **Owner mismatch is rejected.** Catches a lookup by `secret_id` alone, which would let a forged or confused claim ride another user's secret.
- **The issued `secret_id` equals the saved row id.** Catches generating the id twice (once in the token, once by the entity default).
- **The last-used update skips a fresh value.** Catches an unthrottled write on every request.
- **`validUntil` matches the API lifetime.** Catches keeping `AuthTokenType.USER`.
- **The mask is the suffix and differs between two tokens.** Catches a mask that is still a shared prefix.

Plus: `./gradlew lint test` green (`jpa-data` and `server-core` need Docker).

### Bookkeeping

- Commit in Conventional Commits with a module scope (`feat(server-core): …`) and push the first real commit as soon as it exists.
- Open the PR with `plot-open-pr.sh`, not `gh pr create`. Append `→ #<number>` to the branch line in the plan's `## Branches`.

### Scope guard

This branch owns:

- `packages/domain` `userSecret/`, `secrets/`, `session/` (`TokenIssuer`, `JwtParameterNames`)
- `packages/jpa-data` `userSecret/` and `V94`
- `packages/graphql-api` `schema.graphqls`, `SecretsResolver`, `api/mapper/UserSecretMapper`
- `packages/server-core` `session/`
- both `session.graphql` files and their generated clients
- `app-web` profile page and `SessionService`
- `frontend` `upcoming` security page and `@feedless/components` `SessionService`
- the `openapi.yaml` note

No other plans are in flight (`docs/plans/active/` holds only this one). If you find something the plan didn't anticipate, report it rather than improvising outside scope.
