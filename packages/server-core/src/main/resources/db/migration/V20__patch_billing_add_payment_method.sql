ALTER TABLE t_billing
  ADD COLUMN IF NOT EXISTS payment_method character varying(50);
ALTER TABLE t_billing
  ADD COLUMN IF NOT EXISTS invoice_recipient_name character varying(150);
ALTER TABLE t_billing
  ADD COLUMN IF NOT EXISTS invoice_recipient_email character varying(150);
ALTER TABLE t_billing
  ADD COLUMN IF NOT EXISTS callback_url character varying(300);

UPDATE t_billing B
  SET invoice_recipient_name = (SELECT concat(U.first_name, ' ', U.last_name) FROM t_user U where U.id = B.user_id)
  WHERE invoice_recipient_name IS NULL OR invoice_recipient_name = '';

UPDATE t_billing B
  SET invoice_recipient_email = (SELECT U.email FROM t_user U where U.id = B.user_id)
  WHERE invoice_recipient_email IS NULL OR invoice_recipient_email = '';

ALTER TABLE t_billing
  ALTER COLUMN invoice_recipient_name SET NOT NULL ;
ALTER TABLE t_billing
  ALTER COLUMN invoice_recipient_email SET NOT NULL ;

ALTER TABLE t_billing
  ADD COLUMN target_group_enterprise boolean;
UPDATE t_billing SET target_group_enterprise = false WHERE target_group_enterprise IS NULL;
ALTER TABLE t_billing
  ALTER COLUMN target_group_enterprise SET NOT NULL;

ALTER TABLE t_billing
  ADD COLUMN target_group_individual boolean;
UPDATE t_billing SET target_group_individual = false WHERE target_group_individual IS NULL;
ALTER TABLE t_billing
  ALTER COLUMN target_group_individual SET NOT NULL;

ALTER TABLE t_billing
  ADD COLUMN target_group_other boolean;
UPDATE t_billing SET target_group_other = false WHERE target_group_other IS NULL;
ALTER TABLE t_billing
  ALTER COLUMN target_group_other SET NOT NULL;
