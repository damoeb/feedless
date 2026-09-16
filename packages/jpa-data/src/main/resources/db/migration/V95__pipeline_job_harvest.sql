-- A document job remembers the harvest that queued it, so its plugin outcome lands in that harvest's log.
-- Harvests are pruned to the newest few per source; a job outlives its harvest without a log to write to.
ALTER TABLE t_pipeline_job ADD COLUMN harvest_id uuid;
ALTER TABLE t_pipeline_job
  ADD CONSTRAINT fk_pipeline_job__to__harvest FOREIGN KEY (harvest_id) REFERENCES t_harvest (id) ON DELETE SET NULL;
CREATE INDEX idx_pipeline_job_harvest_id ON t_pipeline_job (harvest_id);
