ALTER TABLE t_repository RENAME COLUMN push_notifications_muted TO push_notifications_enabled;
UPDATE t_repository SET push_notifications_enabled = TRUE WHERE push_notifications_enabled IS FALSE;
