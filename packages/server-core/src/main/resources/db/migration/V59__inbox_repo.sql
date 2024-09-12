ALTER TABLE t_user RENAME COLUMN notification_repository_id TO inbox_repository_id;

UPDATE t_repository
  SET title='Inbox'
  WHERE t_repository.id IN (
    SELECT inbox_repository_id FROM t_user WHERE inbox_repository_id IS NOT NULL
  );

ALTER TABLE IF EXISTS t_user
  RENAME CONSTRAINT fk_user__to__notifications_repository TO fk_user__to__inbox_repository;

UPDATE t_repository
SET description='Messages from telegram and ops notifications'
WHERE t_repository.id IN (
  SELECT inbox_repository_id FROM t_user WHERE inbox_repository_id IS NOT NULL
);
