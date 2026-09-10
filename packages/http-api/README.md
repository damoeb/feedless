# http-api

OpenAPI source: `src/main/resources/openapi/openapi.yaml`

Regenerate with:

    ./gradlew :packages:http-api:openApiGenerate

Base URL: `/api/v1`

Auth: GitHub-PAT style. Mint a `UserSecret` via the GraphQL `createUserSecret` mutation, then send its
`value` (a JWT) as `Authorization: Bearer <UserSecret.value>` on every request. There is no REST login,
session, or logout endpoint — `UserSecret` lifecycle (`createUserSecret` / `deleteUserSecret`) stays on
GraphQL. The legacy `Authentication: Bearer <…>` header is still accepted but deprecated, and the `TOKEN`
cookie is still accepted for browser clients. Anonymous JWTs are not accepted on `/api/v1/**`; all
`/api/v1/**` routes require authentication. `GET /user` returns the authenticated user's `id`, `email`,
and `groups`.

REST `/repositories/{repositoryId}/records` ≡ GraphQL `Record` ≡ domain `Document`. Harvest and Plan are
read-only.

## Conventions

- **Creates are singular.** `POST /repositories` and `POST /repositories/{id}/sources` each take one
  object and return one object. (They used to take bare arrays, which left partial failure —
  "3 of 5 created" — unrepresentable.)
- **`PATCH` returns `200` with the updated resource**, so callers never need a follow-up `GET`.
  `DELETE` returns `204`.
- **Timestamps are RFC 3339 UTC strings** (`2026-07-22T09:15:00Z`), not epoch millis.
- **Pagination** is `{items, hasMore}` with 0-based `page` and `pageSize` (max 100, default 20).
  `hasMore` is computed by fetching `pageSize + 1` and checking for the overflow, so a last page
  that happens to be exactly full reports `hasMore: false`.
- **Every error carries an `ApiError` body** — `{code, message, corrId, path, errors?}`. Operations
  declare `default: ApiError`; throttled ones also declare `429` explicitly. Validation errors
  (`VALIDATION_ERROR`) list *every* failing field in `errors[]`.
- **Rate limits** answer `429 TOO_MANY_REQUESTS` with a `Retry-After` header. Mutating endpoints
  are `@Throttled`.
- **`Vertical` vs `VerticalFilter`**: a repository's `product` is always a concrete vertical;
  `all` exists only as a query-filter value.
- **`Visibility`** is `private` / `public` on the wire (the domain enum is `isPrivate` / `isPublic`).
- **Optional means absent, not zero.** `shareKey` is returned only to the owner; counters that a
  given path does not compute (`sourcesCount`, `recordCount`, …) are omitted rather than sent as `0`.
- **`ScrapeAction` is a union.** Exactly one of `fetch`/`extract`/`execute`/`click`/`type`/`waitFor`/
  `select`/`header`/`purge` must be set; anything else is rejected with `400`. OpenAPI 3.0 cannot
  express this well here, so it is `minProperties: 1` + `maxProperties: 1` plus a runtime check.

## Known gaps

- `GET /repositories/{repositoryId}/records` has no filtering or sorting (no `publishedAfter`, tag,
  or query), so "records since my last poll" means paging the whole collection. Offset paging over
  an append-heavy collection also skips/duplicates rows as new records arrive — records want keyset
  pagination.
- `GroupMember` exposes only `userId`, and there is no `/users/{id}` to resolve it.
- Only `RepositoryListResponse` carries `totalCount`; other list envelopes have `hasMore` only.
- No `ETag`/`If-Match` (concurrent `PATCH`es clobber) and no `Idempotency-Key` (a retried create
  after a timeout duplicates).
- `PluginExecutionParams` hardcodes three plugin ids as fixed properties, so adding a plugin is a
  breaking spec change.
- `Record.rawBase64` inlines an unbounded blob instead of exposing `GET /records/{id}/raw`.
- Token minting stays on GraphQL, so a REST-only client cannot bootstrap itself.
