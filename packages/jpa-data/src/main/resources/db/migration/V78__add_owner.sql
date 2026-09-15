ALTER TABLE t_group
  ADD COLUMN owner_id uuid;

UPDATE t_group
SET owner_id = (SELECT id from t_user u where u.is_root is true);

ALTER TABLE t_group
  ADD
    CONSTRAINT fk_group__to__user FOREIGN KEY (owner_id)
      REFERENCES t_user (id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE CASCADE;

ALTER TABLE t_group
  DROP CONSTRAINT uniquegroupname;

ALTER TABLE t_group
  ADD CONSTRAINT uniquegroupname UNIQUE (name, owner_id);
