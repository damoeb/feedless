ALTER TABLE t_repository ADD COLUMN share_key VARCHAR(10);
UPDATE t_repository SET share_key='' WHERE share_key IS NULL;
ALTER TABLE t_repository ALTER COLUMN share_key SET NOT NULL;
