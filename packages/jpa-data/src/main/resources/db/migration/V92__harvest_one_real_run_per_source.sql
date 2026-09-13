-- At most one real (non-dry-run) harvest of a source runs at any time, across processes: the
-- scheduler and the API may run apart, so the database is the only place to enforce it. The
-- scheduled loop records its run as `running` with INSERT ... ON CONFLICT DO NOTHING and skips the
-- source when the insert is refused; the queued-harvest executor only claims a real run whose source
-- has none running. Completing a run, or the stale sweep completing a lost one, frees the slot.

-- The index cannot be built over duplicates, so first complete all but the newest of any real runs
-- of one source that are running together (possible only while the executor was unguarded).
UPDATE t_harvest h
SET status = 'completed',
    errornous = true,
    finished_at = now(),
    logs = concat_ws(E'\n', nullif(h.logs, ''), 'harvest aborted: another real harvest of this source was running')
WHERE h.status = 'running'
  AND h.dry_run = false
  AND EXISTS (
    SELECT 1 FROM t_harvest o
    WHERE o.source_id = h.source_id
      AND o.status = 'running'
      AND o.dry_run = false
      AND (coalesce(o.started_at, o.created_at), o.id) > (coalesce(h.started_at, h.created_at), h.id)
  );

CREATE UNIQUE INDEX uq_harvest_one_running_real_run_per_source
  ON t_harvest (source_id)
  WHERE status = 'running' AND dry_run = false;
