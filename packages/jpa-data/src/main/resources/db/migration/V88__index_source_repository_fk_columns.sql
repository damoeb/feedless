-- source/repository/user deletes seq-scanned these tables per row, and hot queries filter on them
CREATE INDEX IF NOT EXISTS pipeline_job_source_id_idx
  ON t_pipeline_job (source_id);

CREATE INDEX IF NOT EXISTS harvest_source_id_created_at_idx
  ON t_harvest (source_id, created_at DESC);

CREATE INDEX IF NOT EXISTS scrape_action_source_id_idx
  ON t_scrape_action (source_id);

CREATE INDEX IF NOT EXISTS repository_owner_id_idx
  ON t_repository (owner_id);

CREATE INDEX IF NOT EXISTS repository_group_id_idx
  ON t_repository (group_id) WHERE group_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS user_inbox_repository_id_idx
  ON t_user (inbox_repository_id) WHERE inbox_repository_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS annotation_repository_id_idx
  ON t_annotation (repository_id) WHERE repository_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS annotation_owner_id_idx
  ON t_annotation (owner_id);

CREATE INDEX IF NOT EXISTS segment_repository_id_idx
  ON t_segment (repository_id);
