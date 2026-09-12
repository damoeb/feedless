ALTER TABLE t_source ADD COLUMN errors_in_succession integer;

UPDATE t_source SET errors_in_succession = 0 WHERE errors_in_succession IS NULL;

ALTER TABLE t_source ALTER COLUMN errors_in_succession SET NOT NULL ;
