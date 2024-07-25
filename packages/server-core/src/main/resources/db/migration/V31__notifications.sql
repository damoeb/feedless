ALTER TABLE ONLY t_notification
  ADD COLUMN source_id uuid;
ALTER TABLE ONLY t_notification
  ADD CONSTRAINT fk_notification__to__source FOREIGN KEY (source_id) REFERENCES t_source(id) ON DELETE CASCADE;

ALTER TABLE ONLY t_notification
  ADD COLUMN repository_id uuid;
ALTER TABLE ONLY t_notification
  ADD CONSTRAINT fk_notification__to__repository FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;
