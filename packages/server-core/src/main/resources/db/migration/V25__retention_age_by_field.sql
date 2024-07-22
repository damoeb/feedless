ALTER TABLE t_repository ADD COLUMN retention_max_age_days_field character varying(50);
UPDATE t_repository SET retention_max_age_days_field = 'publishedAt' WHERE retention_max_age_days_field IS NULL;
ALTER TABLE t_repository ALTER retention_max_age_days_field SET NOT NULL;
