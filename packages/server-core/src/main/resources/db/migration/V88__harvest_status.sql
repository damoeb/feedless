ALTER TABLE t_harvest
  ADD COLUMN status varchar(16) NOT NULL DEFAULT 'completed',
  ADD COLUMN dry_run boolean NOT NULL DEFAULT false,
  ADD COLUMN flow jsonb;

ALTER TABLE t_harvest
  ADD CONSTRAINT t_harvest_status_check CHECK (status IN ('queued', 'running', 'completed'));
