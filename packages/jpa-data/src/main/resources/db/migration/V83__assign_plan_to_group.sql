ALTER TABLE t_plan
  ADD COLUMN group_id uuid;

-- assign group_id as the default group of user
UPDATE t_plan as plan
SET group_id = (SELECT group_id from t_user_group_assignment uga where uga.user_id = plan.user_id);

-- enforce not-null constraint
ALTER TABLE t_plan
  ALTER COLUMN group_id SET NOT NULL;
