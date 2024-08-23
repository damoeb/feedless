DROP TABLE IF EXISTS t_notification CASCADE;

ALTER TABLE IF EXISTS t_pipeline_job
  ALTER COLUMN sequence_id SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN notification_repository_id uuid;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN notifications_last_viewed_at timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_user
  ADD CONSTRAINT fk_user__to__notifications_repository FOREIGN KEY (notification_repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

ALTER TABLE IF EXISTS t_source
  ALTER COLUMN title TYPE character varying(255) COLLATE pg_catalog."default";

UPDATE t_source SET title = 'empty title' where title is null;
ALTER TABLE IF EXISTS t_source ALTER COLUMN title SET NOT NULL;
