ALTER TABLE ONLY t_user_plan_subscription
  DROP CONSTRAINT IF EXISTS fk8ncewprxstppim67lx6e7ysn4;

ALTER TABLE ONLY t_user_plan_subscription
  RENAME plan_id TO product_id;

ALTER TABLE ONLY t_user_plan_subscription
  ADD CONSTRAINT fk8ncewprxstppim67lx6e7ysn4 FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE CASCADE;

ALTER TABLE t_user_plan_subscription
  DROP COLUMN recurring;

ALTER TABLE t_user_plan_subscription
  DROP COLUMN paiduntil;

ALTER TABLE t_user_plan_subscription
  RENAME COLUMN startedat TO started_at;

ALTER TABLE t_user_plan_subscription
  ADD COLUMN terminated_at timestamp(6) without time zone;

ALTER TABLE t_user_plan_subscription
  RENAME TO t_cloud_subscription;

ALTER TABLE ONLY t_user
  DROP CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2ty;

ALTER TABLE ONLY t_user
  RENAME COLUMN plan_id TO subscription_id;

ALTER TABLE ONLY t_user
  ADD CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2tz FOREIGN KEY (subscription_id) REFERENCES t_cloud_subscription(id) ON DELETE CASCADE;


--     paiduntil timestamp(6) without time zone,
--     plan_id uuid NOT NULL,
--     recurring boolean NOT NULL,
--     startedat timestamp(6) without time zone,


--     paiduntil timestamp(6) without time zone,
--     plan_id uuid NOT NULL,
--     recurring boolean NOT NULL,
--     startedat timestamp(6) without time zone,
