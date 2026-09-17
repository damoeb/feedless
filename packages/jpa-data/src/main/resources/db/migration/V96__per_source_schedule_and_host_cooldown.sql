-- Each source carries its own next harvest time, so backpressure can delay one source without delaying its repository.
ALTER TABLE t_source ADD COLUMN next_harvest_at timestamp;

UPDATE t_source s
SET next_harvest_at = r.trigger_scheduled_next_at
FROM t_repository r
WHERE r.id = s.repository_id;

CREATE INDEX idx_source_next_harvest_at ON t_source (next_harvest_at) WHERE is_disabled = false;

-- Shared across core instances and restarts; a 429 or 403 describes the whole host, not one source.
CREATE TABLE t_host_cooldown
(
  host          text PRIMARY KEY,
  blocked_until timestamp NOT NULL,
  strikes       integer   NOT NULL DEFAULT 0,
  last_status   integer,
  updated_at    timestamp NOT NULL
);
