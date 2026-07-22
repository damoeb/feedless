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

REST `/records` ≡ GraphQL `Record` ≡ domain `Document`. Harvest and Plan are read-only.
List responses include `hasMore`. Mutating endpoints are `@Throttled`.
