# http-api

OpenAPI source: `src/main/resources/openapi/openapi.yaml`

Regenerate with:

    ./gradlew :packages:http-api:openApiGenerate

Base URL: `/api/v1`

Auth header: `Authentication: Bearer <jwt>` from `POST /api/v1/auth/login` or a UserSecret JWT from a logged-in user. Anonymous JWTs are not accepted on `/api/v1/**`.

REST `/records` ≡ GraphQL `Record` ≡ domain `Document`. Harvest and Plan are read-only.
List responses include `hasMore`. Mutating endpoints are `@Throttled`.
