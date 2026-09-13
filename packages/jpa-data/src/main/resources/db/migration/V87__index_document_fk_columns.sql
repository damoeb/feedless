-- deleting a document seq-scanned every referencing table per row; FKs are not indexed automatically
CREATE INDEX IF NOT EXISTS document_parent_id_idx
  ON t_document (parent_id) WHERE parent_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS pipeline_job_document_id_idx
  ON t_pipeline_job (document_id) WHERE document_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS attachment_document_id_idx
  ON t_attachment (document_id);

CREATE INDEX IF NOT EXISTS annotation_document_id_idx
  ON t_annotation (document_id) WHERE document_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS annotation_text_comment_id_idx
  ON t_annotation_text (comment_id);
