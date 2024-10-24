ALTER TABLE t_priced_product DROP COLUMN target_group_enterprise;
ALTER TABLE t_priced_product DROP COLUMN target_group_individual;
ALTER TABLE t_priced_product DROP COLUMN target_group_other;

ALTER TABLE t_product ADD COLUMN self_hosting_enterprise boolean;
UPDATE t_product SET self_hosting_enterprise = false where self_hosting_enterprise is null;
ALTER TABLE t_product ALTER COLUMN self_hosting_enterprise SET NOT NULL;

ALTER TABLE t_product ADD COLUMN self_hosting_individual boolean;
UPDATE t_product SET self_hosting_individual = false where self_hosting_individual is null;
ALTER TABLE t_product ALTER COLUMN self_hosting_individual SET NOT NULL;

ALTER TABLE t_product ADD COLUMN self_hosting_other boolean;
UPDATE t_product SET self_hosting_other = false where self_hosting_other is null;
ALTER TABLE t_product ALTER COLUMN self_hosting_other SET NOT NULL;

ALTER TABLE t_product ADD COLUMN is_available boolean;
UPDATE t_product SET is_available = true where is_available is null;
ALTER TABLE t_product ALTER COLUMN is_available SET NOT NULL;

ALTER TABLE IF EXISTS t_priced_product
  ADD COLUMN recurring_interval character varying(255);
UPDATE t_priced_product SET recurring_interval='MONTHLY' where recurring_interval is null;
ALTER TABLE t_priced_product ALTER COLUMN recurring_interval SET NOT NULL;

