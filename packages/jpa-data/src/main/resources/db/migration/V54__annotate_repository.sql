ALTER TABLE t_annotation ALTER COLUMN document_id DROP NOT NULL;
ALTER TABLE t_annotation ADD COLUMN repository_id uuid;

ALTER TABLE ONLY t_annotation
  ADD CONSTRAINT fk_annotation__to__repository FOREIGN KEY (id) REFERENCES t_annotation(id);
