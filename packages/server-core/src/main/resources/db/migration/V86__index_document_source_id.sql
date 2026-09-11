-- count per source seq-scanned t_document; FKs are not indexed automatically
CREATE INDEX IF NOT EXISTS document_source_id_idx
  ON t_document (source_id);

CREATE INDEX IF NOT EXISTS source_repository_id_idx
  ON t_source (repository_id);
