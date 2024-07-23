ALTER TABLE t_repository_cron_run DROP CONSTRAINT fk_repository_run__to__repository;
ALTER TABLE t_repository_cron_run RENAME COLUMN repository_id TO source_id;

ALTER TABLE t_repository_cron_run ADD CONSTRAINT fk_repository_run__to__source FOREIGN KEY (source_id)
    REFERENCES t_source (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;
