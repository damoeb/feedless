ALTER TABLE t_document ADD COLUMN content_hash character varying(50);

UPDATE t_document SET content_hash = '' where content_hash IS NULL;

ALTER TABLE t_document ALTER COLUMN content_hash SET NOT NULL ;
