UPDATE t_repository AS r
SET group_id = (SELECT g.id
                FROM t_group AS g
                WHERE g.owner_id = r.owner_id
                LIMIT 1)
WHERE r.group_id IS NULL;

ALTER TABLE t_repository
  ALTER COLUMN group_id SET NOT NULL;
