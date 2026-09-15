# Named API secrets with distinct masking, last-used tracking and real revocation

> User secrets get a required name, a mask that shows the distinct end of the token, a last-used timestamp that API calls actually update, and deleting a secret revokes its token.

## Status

- **State:** Approved
- **Type:** feature
- **Review:** in-session
- **Impl:** same branch
- **Approved:** 2026-09-15, damoeb, in-session
- **Started:** 2026-09-15, damoeb, `feature/user-secret-name-last-used`

## Approval

- **Assignee:** damoeb

## Changelog

- API secrets have a name, chosen when you create them.
- The secret list shows the last characters of each token instead of a prefix every token shares.
- "Last used" now updates whenever a token calls the API (GraphQL or `/api/v1`), not only on agent logins.
- Deleting a secret revokes its token immediately.
- "Valid until" shows the token's real expiry.
- **Breaking:** API tokens created before this release are rejected and must be recreated. `createUserSecret` now requires `data: { name }`.

## Motivation

- **No name.** A user with several tokens can't tell which one belongs to which script or machine.
- **Useless mask.** `UserSecretMapper.maskValue` keeps the first 5 characters. Every JWT starts with `eyJhb`, so all tokens look the same.
- **Last used never updates for the API.** `HttpApiJwtFilter` and `JwtRequestFilter` authenticate through `TokenAuthenticator` from the JWT signature alone and never read `t_user_secret`. Only `BrowserAutomationService` calls `updateLastUsed`.
- **No revocation.** Because the database is never consulted, a deleted secret's JWT keeps working until it expires.
- **Wrong expiry.** `createJwtForApi` sets the 356-day `SERVICE` lifetime, but `UserSecretUseCase` stores `validUntil` from the 48-hour `USER` lifetime.

## Design

### Approach

Every API token is checked in `TokenAuthenticator`: a primary-key lookup per request and a throttled write. We rejected an in-memory cache with batched writes because it delays revocation and doesn't hold across instances. We also rejected a separate post-auth filter or event listener, which would need extra wiring to cover both filters.

**Data and token**

- **Migration `V94__user_secret_name.sql`** (additive; raise `spring.flyway.target` to 94 in `application-database.yaml`):
  - add `name varchar(100)`,
  - backfill `'Legacy token (' || to_char(created_at, 'YYYY-MM-DD') || ')'`,
  - then set `NOT NULL`.
- **`name` field** on the domain `UserSecret`, on `UserSecretEntity`, and in the jpa MapStruct mapper.
- **`TokenIssuer.issueApiToken(user, actingGroup, secretId)`** writes a new `secret_id` claim (`JwtParameterNames.SECRET_ID`). The existing `id` claim already holds `"feedless"`.
- **`UserSecretUseCase.createUserSecret(name)`:**
  - trims the name and requires 1–100 characters, otherwise it throws `IllegalArgumentException`,
  - generates the `UserSecretId` first and issues the token with that id,
  - saves the row under the same id,
  - computes `validUntil` from the API token's real lifetime.
- **Masking.** The GraphQL `UserSecretMapper` masks a value as `••••` plus its last 6 characters.

**Auth check**

- **`AuthService.useApiSecret(id: UserSecretId, ownerId: UserId, now: LocalDateTime): Boolean`** returns false when the row is missing or owned by someone else.
  - When the row is valid, it updates `last_used_at` only if the value is null or older than one minute. It's a single conditional `UPDATE` (a new repository method on `UserSecretRepository`/`UserSecretDAO`).
  - `StatefulAuthService` implements it with `UserSecretRepository`. `StatelessAuthService` always returns false, because stateless mode has no secret table.
- **`TokenAuthenticator`** gets `AuthService` injected. For `token_type == API`:
  - it reads `secret_id` and the `UserCapability`,
  - it throws `AccessDeniedException` if the claim is missing (legacy token) or `useApiSecret` returns false,
  - the verdict is cached on the request, as the group verdict already is.
- **How each filter reacts.** `HttpApiJwtFilter` answers 401. `JwtRequestFilter` logs and treats the request as anonymous, so `@PreAuthorize` rejects it.
- **What isn't affected:** user, anonymous and agent tokens, and the value-based root and agent logins (`findBySecretKeyValue`). Agent logins keep updating `last_used_at`.

**GraphQL contract (`schema.graphqls`)**

- `createUserSecret(data: CreateUserSecretInput!): UserSecret!` with `input CreateUserSecretInput { name: String! }`. This replaces the unused `UserSecretCreateInput`.
- `type UserSecret` gains `name: String!`. `lastUsed: Long` stays.
- Both TypeScript clients are regenerated, never hand-edited: `app-web` with `yarn codegen`, and `frontend` `@feedless/graphql-api` with its own codegen. `feedctl` uses `openapi.yaml` and doesn't change. Its note in `openapi.yaml` about creating the token through GraphQL gets a line saying tokens created before this change must be recreated. That means running the `feedctl` generator, so the lint drift check stays green.

**UIs**

- Both `session.graphql` files add `name` to `SecretFragment` and pass `data: { name }` to `createUserSecret`. Both `SessionService.createUserSecret` methods take `name`.
- **`upcoming` security page:**
  - a required name field sits next to Create, and Create is disabled while the name is empty,
  - each row shows the name, the masked value (monospace), "Last used" as a date or "never", "Valid until", and Delete,
  - the newly created secret shows once in full with its name and Copy.
- **`app-web` profile page:** the same fields and name input. `lastUsed` goes through the date pipe; today it prints raw milliseconds. The block keeps its `appDev` gating.

**Testing** (TDD)

- **`domain`, `UserSecretUseCaseTest`:** name validation and trimming; the issued `secret_id` equals the saved id; `validUntil` matches the API lifetime.
- **`jpa-data`** (Testcontainers): the conditional last-used update fires when the value is null or stale and skips a fresh one; the V94 backfill produces the dated names.
- **`server-core`:**
  - `TokenAuthenticatorTest`: a valid API token passes and gets touched; a missing claim, an unknown secret, or an owner mismatch is rejected; non-API tokens never call `useApiSecret`.
  - `HttpApiJwtFilterTest`: a revoked token gets a 401.
  - `JwtTokenIssuerTest`: the `secret_id` claim is present.
  - `StatefulAuthServiceTest`: covers `useApiSecret`.
  - Integration tests that mint API tokens now create the secret row too.
- **`graphql-api`:** a mapper test checks the suffix mask and that `name` is passed through.
- **Frontend:** the security page spec checks the name is required and that name and last-used render.
- **Definition of Done:** `./gradlew lint test`.

### Open Points

- [ ] Whether the `app-web` secrets block should lose its `appDev` gating is a separate decision and out of scope here.
- [ ] Storing the full JWT in plaintext in `t_user_secret.value` is out of scope. The root and agent logins still match on it. Once they stop, only a hash plus the display suffix would need to be kept.

## Branches

- `feature/user-secret-name-last-used` — plan + full implementation in one PR <!-- builds: AuthService.useApiSecret, the secret_id JWT claim, V94__user_secret_name.sql, CreateUserSecretInput -->

## Notes

- 2026-09-15: Design reviewed in-session. Decisions: revoke deleted secrets (yes); reject legacy API tokens without `secret_id` rather than fall back to value lookup; name required; both UIs; backfill dated legacy names.
- Deliverable search: nothing named `useApiSecret`, `CreateUserSecretInput` or `V94__` exists. `secret_id` exists only as the `t_browser_automation` FK column to `t_user_secret`, not as a JWT claim.
