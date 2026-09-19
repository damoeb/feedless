# Tree-shaped grants and group-based event listings for upcoming

> Replace the repository visibility flag, owner-only writes, the group-member read rule and share keys with explicit `<subject> <action> <object> [via]` grants over a resource tree, add invite-and-accept group membership in which the member chooses which repositories to attach, and let upcoming serve events from a listing group instead of one hard-coded repository.

## Status

- **State:** Draft
- **Type:** feature
- **Review:** pr
- **Impl:** own branches

## Changelog

- A group can act as an event listing: its members attach their own repositories to it, and upcoming shows the events of every attached repository.
- Joining a group is now invite and accept; on accepting, the member chooses which of their repositories the group may list, and can detach them again at any time.
- Repository visibility is now a grant: "public" means anyone may read. Existing repositories keep exactly the access they had.

## Motivation

upcoming serves events from one repository, `eventRepositoryId`, read from a static `config.json` (`packages/frontend/apps/upcoming/public/config.json`, `docker/app/app-config.json`, `k8s/feedless/upcoming-config.yaml`) and used by the events resolver, the calendar page, the email abo and the event-sources page. Every organiser's sources therefore have to live in that one repository, owned by one user. The goal is a listing that is assembled from many users' repositories, where each user decides explicitly what the listing may show.

The current permission model cannot express that:

- Memberships are direct inserts (`GroupUseCase.addUserToGroup`) with no invite, pending state or consent from the added user.
- Access is four hard-coded rules in `RepositoryGuard` (`packages/domain/.../repository/RepositoryGuard.kt`, `RepositoryAccessRule.kt`). Read is allowed when the repository is public, when the caller holds its share key, or when the caller is its owner or any member of `repository.groupId`. Write is owner only.
- Every document read is keyed to one repository. The GraphQL `records` query has a required `RecordsWhereInput.repository`, `DocumentJpaRepository.findAllFiltered` filters on `repositoryId`, the `/f/{repositoryId}/{format}` feed takes the repository from the path, and the email-abo `Segmentation` is saved with a single `repositoryId`.

## Design

### Approach

**Grants in Postgres, evaluated in `domain`.** An external ReBAC engine (OpenFGA, SpiceDB) was rejected because every self-hosted install would need to run another service, and keeping it consistent with Postgres on revoke would become our problem. A plain link table on top of the existing flags was rejected because it keeps public/private as a flag, which contradicts the requirement.

#### Vocabulary

| Part | Values | Meaning |
|---|---|---|
| subject | `anyone`, `user:<id>`, `group:<id>`, `share_key` | Who. `anyone` matches every caller, anonymous included. `group:<id>` matches active members of that group. `share_key` matches a caller presenting the object's share key. |
| action | `read`, `write`, `manage` | `manage` means editing the object's grants and edges. `execute` is left out: it is `TODO()` in most guards today. |
| object | `repository:<id>`, `group:<id>` | The resource. |
| via | nullable `group:<id>` | The path constraint: the grant applies only when the object is reached through this node. |

#### Tables (additive migrations, next free numbers from V98)

- `t_grant(id, subject_type, subject_id NULL, action, object_type, object_id, via_type NULL, via_id NULL, created_by, created_at)`, unique over all identifying columns.
- `t_resource_edge(id, parent_type, parent_id, child_type, child_id, consented_by, created_at)`, unique on (parent, child). For now the only edge is group ⊃ repository.
- `t_user_group_assignment` gains `status` (`invited`, `active`, `declined`) and `invited_by`. Existing rows are backfilled as `active`.

#### Evaluation rule

`AccessPolicy.may(caller, action, object, via = null)` is a `domain` port with a `jpa-data` adapter. The guards call it, and the `ResourceGuard` interface is unchanged.

1. **Direct:** some grant on `object` with `via = null` matches the caller and action.
2. **Via a listing** (`via = X`):
   - an edge `X ⊃ object` exists,
   - `may(caller, read, X)` holds,
   - and the action is `read`.
3. **Only `read` flows along edges.** `write` and `manage` never inherit.
4. **Decision, path-scoped inheritance:** a grant on X never makes R readable directly. An anonymous visitor can read a private R's events through listing X, but not through `/f/R` or `records(repository: R)`. This matches what the member consented to: "show R in X", not "make R public". It was decided without a preference from the user and can be overridden in review.
5. **Depth:** the tree is stored generically, but the evaluator follows one edge (group → repository). Deeper nesting waits until something needs it.

#### Mapping today's access onto grants (backfill migration)

| Today | Grant |
|---|---|
| `visibility = 'isPublic'` | `anyone read repository:R` |
| `owner_id = U` | `user:U read, write, manage repository:R` |
| `group_id = G` (member read rule) | `group:G read repository:R` |
| `share_key` | `share_key read repository:R` (the key value stays in `t_repository.share_key`) |

`owner_id` and `group_id` stay on `t_repository`, because they still carry tenancy: plan limits, retention and the plan audit. Only their *access* meaning moves to grants. `visibility` is dual-written until the drop slice (see Slices).

#### Membership handshake

- **Invite:** a group `owner` invites a user. This creates an assignment with status `invited`, and nothing becomes visible yet.
- **Accept:** the invitee accepts and, in the same mutation, chooses repositories they `manage`. Each chosen repository gets an edge `group ⊃ repository` with `consented_by = invitee`.
- **Decline:** sets status `declined`.
- **Later changes:** the member can attach or detach repositories at any time. Detaching removes the edge, and every inherited read through that group goes with it.
- **Leaving:** removes the member's edges into the group.
- **Scope:** request-to-join is out of scope, and the model does not preclude it.

#### Listing reads

- **GraphQL:**
  - `RecordsWhereInput.repository` becomes optional, and a new `listing: GroupUniqueWhereInput` is added. Exactly one of the two is required, validated in the resolver.
  - For a listing, the document query filters on `repository_id IN (children of X the caller may read via X)`. The query is built in the adapter, not by post-filtering.
  - `recordsFrequency` follows the same rule.
- **Feed:** a new route, `/l/{groupId}/{format}`, sits next to `/f/{repositoryId}/{format}` in `http-api`'s `RepositoryController` and reuses the same `where` parsing.
- **Abo:**
  - `Segmentation` gains a nullable `listing_group_id`, with a check constraint that exactly one of `repository_id` and `listing_group_id` is set.
  - `createReport` takes either a repository or a listing.
  - `ReportUseCase.resolveSegment` evaluates access at delivery time, so a detached repository stops appearing in mails.
- **upcoming:**
  - `eventRepositoryId` is replaced by `eventListingGroupId` in all three config files and the five call sites.
  - The account page `event-sources` shows the user's repositories attached to the listing, with attach and detach actions.

#### Rollout for the existing upcoming data (ops step, documented in the listing slice)

1. Create the listing group, owned by the current `eventRepositoryId` owner.
2. Attach the existing event repository to it.
3. Grant `anyone read group:X`.
4. Switch the config.

The result shows exactly today's data, and other organisers can then be invited.

#### Repo rules that apply

- **Profiles:** every new bean is profile-gated, and tests enumerate the same profiles (`docs/rules/kotlin-spring.md`).
- **Generated code:**
  - `schema.graphqls` changes are followed by regenerating the Kotlin DGS types and the TS clients (`yarn codegen` in `app-web` and `browser-automation-app`, plus the `frontend` graphql-api lib).
  - `openapi.yaml` changes to the group endpoints are followed by `go generate` for `feedctl`.
  - Generated output is never hand-edited.
- **Flyway:** migrations are additive only.
- **Security tests:**
  - Every rule of `RepositoryGuardTest` is replayed against the grant-backed guard before the old rules are removed. This is the parity gate of slice 1.
  - Leak tests cover:
    - an unattached private repository is not in the listing,
    - a detached repository disappears from the listing and the abo,
    - a private repository attached to a public listing is still denied on `/f/R` and `records(repository: R)`,
    - an `invited` member (not yet `active`) gets nothing through the group.

### Open Points

- [ ] **Group read and execute:** `GroupGuard` read and execute are `TODO()`. Listing reads need `may(caller, read, group:X)`, so group read moves onto grants in slice 1. Confirm that no current caller depends on the `TODO()` throwing.
- [ ] **Group roles vs grants:** should `RoleInGroup` (`owner`, `editor`, `viewer`) become grants on `group:X`, or stay as the membership role? The plan keeps roles for managing the group itself and uses grants for resources. Revisit if they diverge.
- [ ] **Editor writes:** editors in `repository.groupId` still cannot write (owner-only parity). Granting them `write` is a separate decision.
- [ ] **Document-level grants:** `DocumentGuard` keeps delegating to the repository. No per-document grants.
- [ ] **Query performance:** index `t_grant` on `(object_type, object_id, action)` and `t_resource_edge` on `(parent_type, parent_id)`, and check the listing query plan against the production-sized upcoming repository.
- [ ] **Visibility on the wire:** keep exposing `Repository.visibility` in GraphQL, derived from the `anyone read` grant, so `app-web` needs no change until a grants UI exists.

## Slices

### Tracer

- `feature/grant-model-core` — tables, backfill, `AccessPolicy` port and JPA adapter, `RepositoryGuard` (read, write, `mayReadConfiguration`) and group read on grants, full `RepositoryGuardTest` parity plus the evaluation-rule tests <!-- builds: AccessPolicy, t_grant, t_resource_edge -->
  Layers: Flyway → jpa-data adapter → domain policy → guards
  Proves: grants reproduce today's access exactly, so every later slice builds on a model that already carries production

### Implementation

- `feature/grant-model-visibility` — setting a repository public or private writes and removes the `anyone read` grant, and GraphQL `visibility` is derived from the grant; `visibility` column is still dual-written <!-- builds: visibility-as-grant write path -->
- `feature/grant-model-membership` — `status` and `invited_by` on assignments; invite, accept with repository choice, decline, attach, detach and leave in the domain use case, GraphQL mutations and the `/api/v1/groups` endpoints; `feedctl` regenerated <!-- builds: group invitation and repository attachment -->

### Wave 3

- `feature/grant-model-listing-reads` — `records` and `recordsFrequency` by listing, the `/l/{groupId}/{format}` feed, the listing abo (`Segmentation.listing_group_id`), leak tests, the rollout runbook <!-- builds: listing records query, /l feed route -->
- `feature/grant-model-upcoming` — upcoming switches to `eventListingGroupId` (config files, resolver, calendar, abo, event-sources page with attach and detach); TS clients regenerated <!-- builds: upcoming listing consumption -->

### Wave 4

- `feature/grant-model-drop-visibility` — once every reader uses grants and one release has shipped, a new migration drops `t_repository.visibility` <!-- builds: drop of the visibility column -->

## Notes

- 2026-09-19: brainstormed in session. The user asked for the plan without further questions and will continue later, so these calls were made by the agent and are open for review:
  - path-scoped inheritance (user: no preference),
  - invite then accept (user: no preference),
  - Type `feature` (Plot normally asks),
  - review switched from `in-session` to `pr` at the user's request,
  - Postgres grants over OpenFGA/SpiceDB.
- The user's stated requirements:
  - a group X is designated the event-listing group, and its members' repositories feed it;
  - a membership defines visibility to an explicit set of repositories, chosen when the user accepts it;
  - public and private are represented as grants, not a flag;
  - tree-like grants resolve what a listing may show.
- The plan was written on `idea/grant-model` in the worktree `../feedless-grant-model`, branched from `origin/develop`, because the main checkout had unrelated uncommitted work on `infra/upgrade-js-deps`.
