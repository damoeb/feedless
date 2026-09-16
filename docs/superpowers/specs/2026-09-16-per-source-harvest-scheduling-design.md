# Per-source harvest scheduling and host backpressure

Date: 2026-09-16 · Status: approved design, awaiting spec review

## Problem

Harvests are scheduled per repository: `RepositoryHarvesterExecutor` picks due repositories by `t_repository.trigger_scheduled_next_at`, `RepositoryHarvester.harvestRepository` scrapes their sources in sequence, and the repository's next time comes from `sources_sync_cron`. A source has no schedule of its own, so backpressure from a remote host has nowhere to go:

```
16:09:01.230  handleFetch FetchAction(url=https://www.bueron.ch/index.php?apid=1659677497, forcePrerender=false, …)
16:09:01.230  render static
16:09:01.487  scrape failed 429 received
```

`HttpService` maps the 429 to `HostOverloadingException` with a fixed 5 minutes, `Retry-After` is never read, and the duration is discarded: the source is recorded as interrupted and retried on the next repository tick. Blocks (401/403) are worse: they become `SiteNotFoundException`, increment `errorsInSuccession`, and delete documents in plugin pipelines.

## Goals

1. Parse `Retry-After` (delta-seconds or HTTP-date) on 429 and 503; fall back to 5 minutes.
2. Throttling and blocking back off the whole host, persistently and across core instances.
3. Blocks (401/403) retry on an escalating ladder instead of failing.
4. The repository cron seeds each source's own next harvest time; one scheduler works over sources.

## Non-goals

- Status-code backpressure from browser-automation (prerender) fetches; the agent does not report the origin status. Prerender fetches only respect existing cooldowns.
- Replacing the in-memory Bucket4j buckets in `HttpService`; they remain a local flood guard and never write cooldowns.
- A give-up threshold for permanently blocked hosts; they settle at one attempt per 24 h.
- Changes to `schema.graphqls` or `openapi.yaml`.

## Design

### Data model

**V96** (raises `spring.flyway.target` to 96):

- `t_source.next_harvest_at timestamp null`, backfilled from the parent repository's `trigger_scheduled_next_at`.
- `t_source.host text null`, lower-cased host of the source's first `FetchAction` URL; backfilled, and set on every source save.
- Partial index on `t_source (next_harvest_at) where is_disabled = false`.
- `t_host_cooldown`:

| Column | Type | Notes |
|---|---|---|
| `host` | `text` | primary key, lower-cased |
| `blocked_until` | `timestamp not null` | |
| `strikes` | `int not null default 0` | block strikes, reset by success |
| `last_status` | `int` | status that caused the cooldown |
| `updated_at` | `timestamp not null` | |

**V97** drops `t_repository.trigger_scheduled_next_at`. It is committed in this change but **pinned out**: `spring.flyway.target` stays at 96. The file starts with a comment stating the pin, so the next migration's author does not raise the target past it unknowingly. `AbstractRepositoryEntity` stops mapping the column and no domain code reads it, so raising the target to 97 later is safe. This knowingly deviates from the AGENTS.md rule that a new migration raises the target.

### Scheduling

`RepositoryHarvesterExecutor` is replaced by `SourceHarvesterExecutor` (same `repository & scheduler` profiles, same `fixedDelay`). Each tick claims up to 50 due sources and harvests at most 10 concurrently via `RepositoryHarvester.harvestSource`.

A source is due when:

- `next_harvest_at` is null or before now;
- `is_disabled = false`;
- no `t_host_cooldown` row for its host has `blocked_until > now`;
- it has no running real harvest (the V92 unique index remains the authoritative lock via `startRun`);
- its repository is not archived, has a non-empty `sources_sync_cron`, `disabled_from` is null or in the future, and its owner is not locked, banned, missing terms acceptance, or scheduled for purge (the checks of today's `findAllWhereNextHarvestIsDue`).

Order: `next_harvest_at asc nulls first`.

Next harvest time after a run, computed per source:

```
cronNext   = coerceMinScheduledNextAt(nextCronDate(sourcesSyncCron, now), plan)
retryAt    = now + e.nextRetryAfter          (resumable failures only)
nextHarvest = max(cronNext, retryAt, host.blocked_until)
```

`RepositoryUseCase`:

- `nextUpdateAt` (read) = `min(next_harvest_at)` over the repository's enabled sources.
- Writing `nextUpdateAt` or `scheduleNextUpdateNow` sets that time on all the repository's sources.
- Changing `sourcesSyncCron` recomputes `next_harvest_at` for all its sources.

Removed: `RepositoryHarvester.harvestRepository`, `RepositoryDAO.findAllWhereNextHarvestIsDue`. The harvest-lateness metric moves to the executor, measured against `next_harvest_at`.

### Host cooldown

Outbound port `HostCooldown` in `domain`, JPA adapter in `jpa-data`. Every write is a single `insert … on conflict (host) do update` statement.

| Operation | Effect |
|---|---|
| `blockedUntil(host)` | `blocked_until` if in the future, else null |
| `recordThrottled(host, status, retryAfter)` | `blocked_until = greatest(existing, now + retryAfter)`; strikes unchanged |
| `recordBlocked(host, status)` | `strikes + 1`; `blocked_until = greatest(existing, now + ladder(strikes))` |
| `recordSuccess(host)` | delete the row |

Block ladder by strike: 1 → 5 min, 2 → 30 min, 3 → 2 h, 4 → 12 h, ≥5 → 24 h.

### HttpService

In `execute`, keyed by the lower-cased request host:

1. **Before the request:** if `blockedUntil(host)` is in the future, throw `HostOverloadingException(blockedUntil − now)`. No network call. This covers the scheduler, pagination jobs, plugins, and dry runs.
2. **429 / 503:** delay = `RetryAfter.parse(header)`; `recordThrottled`; throw `HostOverloadingException(delay)`.
3. **401 / 403:** `recordBlocked`; throw new `HostBlockedException(host, status, strikes, delay)`, a `ResumableHarvestException`.
4. **2xx:** `recordSuccess` only if the host is in a process-local set of hosts known to have a row (populated on steps 1–3), so successful fetches cost no database write.
5. All other statuses keep today's mapping (404/410 → `SiteNotFoundException`, 500 → 5 min, other 5xx → 5 h).

`RetryAfter.parse`: delta-seconds or IMF-fixdate HTTP-date (RFC 9110 §10.2.3). Missing, unparseable, zero, negative, or past dates → 5 min. Result capped at 24 h.

`@Cacheable` on `httpGetCaching` is unchanged; cache hits touch neither the host nor the cooldown.

### Handling in callers

`RepositoryHarvester.harvestSource` (scheduled and queued runs):

| Outcome | Source record | Harvest | `next_harvest_at` |
|---|---|---|---|
| success | `recordHarvestSucceeded` | ok | `cronNext` |
| `ResumableHarvestException` (incl. `HostOverloadingException`, `HostBlockedException`) | `recordHarvestInterrupted`, `lastErrorMessage` e.g. `throttled by bueron.ch (429), retry in 10m` / `blocked by bueron.ch (403, strike 2), retry in 30m` | not `errornous`; log line `delayed until <time>` | `max(cronNext, now + nextRetryAfter)` |
| `TooManyConnectionsPerHostException` | interrupted | not `errornous` | `max(cronNext, now + 2 min)` |
| `UnknownHostException`, `ConnectException` | interrupted (unchanged) | not `errornous` | `max(cronNext, now + 5 min)` |
| `NoItemsRetrievedException` | interrupted (unchanged) | not `errornous` | `cronNext` |
| anything else | `recordHarvestFailed` (unchanged) | `errornous` | `cronNext` |

- `SourceUseCase.processSourceJobs`: unchanged; its existing `coolDownUntil = now + nextRetryAfter` now receives parsed delays.
- `DocumentUseCase.processDocumentPlugins`: unchanged; `HostBlockedException` is resumable, so a 403 inside a plugin delays the job instead of deleting the document.
- `QueuedHarvestExecutor` ("harvest now", dry runs): during a cooldown the run completes immediately without a network call, logging `host cooling down until <time>`. Dry runs never write `next_harvest_at` or cooldown rows but respect existing cooldowns.

## Testing

Unit (no Docker):

- `RetryAfterTest`: seconds, HTTP-date, past date, `0`, negative, garbage, missing, above cap.
- `HttpServiceTest` (mock server): 429 with/without header → `recordThrottled` with parsed/fallback delay; 503 with HTTP-date; 403 → `recordBlocked` + `HostBlockedException`; 2xx on a cooled-down host → `recordSuccess`; active cooldown throws before any request.
- `RepositoryHarvesterTest`: each row of the caller table above, including that throttling neither increments `errorsInSuccession` nor marks the harvest `errornous`; existing error-count tests still pass.
- `SourceHarvesterExecutorTest` (replaces `RepositoryHarvesterExecutorTest`): `@Scheduled` presence, concurrency bound of 10, child correlation ids.
- `RepositoryUseCaseTest`: `nextUpdateAt` read is the min over sources; write and cron change re-seed sources.

Integration (jpa-data, Testcontainers/PostGIS):

- `HostCooldownRepositoryIntTest`: upsert semantics, ladder, `greatest` never shortening a block, delete on success.
- `SourceDAO` due query: excludes cooled-down hosts, disabled sources, running harvests, archived repositories, blocked owners.
- V96 backfill: `next_harvest_at` copied from the repository, `host` derived from the fetch URL (applied directly, since tests run with Flyway disabled).

Definition of Done: `./gradlew lint test`.

## Rollout

- V96 seeds every source with its repository's current next time, so cutover causes no harvest burst.
- V97 stays pinned out until this has run cleanly in production; then raise the target to 97.
- One PR against `develop`:
  1. `feat(jpa-data): per-source schedule and host cooldown`
  2. `feat(server-core): honour Retry-After and host blocks`
  3. `feat(domain): schedule harvests per source`

## Known drift to fix alongside

- `AGENTS.md` critical rule 5 says the pinned target is `V93`; it is `V95` today and becomes `V96`.
