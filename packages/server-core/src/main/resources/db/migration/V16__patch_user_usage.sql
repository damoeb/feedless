ALTER TABLE t_user DROP COLUMN product;
ALTER TABLE t_user ADD COLUMN total_usage_mb double precision;
UPDATE t_user SET total_usage_mb = 0.0 WHERE total_usage_mb IS NULL;
ALTER TABLE t_user ALTER COLUMN total_usage_mb SET NOT NULL;
