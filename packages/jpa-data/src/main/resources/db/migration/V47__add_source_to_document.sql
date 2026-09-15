ALTER TABLE t_document ADD COLUMN source_id uuid;
ALTER TABLE t_document ADD
  CONSTRAINT fk_document__to__source FOREIGN KEY (source_id)
  REFERENCES t_source (id) MATCH SIMPLE
  ON UPDATE NO ACTION
     ON DELETE CASCADE
