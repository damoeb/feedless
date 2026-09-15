DELETE FROM t_harvest;
ALTER TABLE t_harvest DROP CONSTRAINT fk_harvest__to__repository;
ALTER TABLE t_harvest RENAME repository_id TO source_id;
ALTER TABLE t_harvest ADD CONSTRAINT fk_harvest__to__source FOREIGN KEY (source_id)
    REFERENCES t_source (id) MATCH SIMPLE
    ON UPDATE NO ACTION
       ON DELETE CASCADE;
