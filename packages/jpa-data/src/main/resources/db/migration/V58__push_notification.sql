ALTER TABLE t_repository
  ADD COLUMN push_notifications_muted boolean;

UPDATE t_repository SET push_notifications_muted=false WHERE push_notifications_muted IS NULL;

ALTER TABLE t_repository
  ALTER COLUMN push_notifications_muted SET NOT NULL;
