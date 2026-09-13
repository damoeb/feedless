ALTER TABLE t_source ADD COLUMN last_records_retrieved integer;

UPDATE t_source SET last_records_retrieved = 0 WHERE last_records_retrieved IS NULL;

ALTER TABLE t_source ALTER COLUMN last_records_retrieved SET NOT NULL ;
