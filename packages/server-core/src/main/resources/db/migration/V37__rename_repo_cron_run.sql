ALTER TABLE t_repository_cron_run RENAME TO t_source_pipeline_job;
ALTER TABLE t_source_pipeline_job RENAME CONSTRAINT fk_repository_run__to__source TO fk_source_job__to__source;

ALTER TABLE t_pipeline_job RENAME TO t_document_pipeline_job;
