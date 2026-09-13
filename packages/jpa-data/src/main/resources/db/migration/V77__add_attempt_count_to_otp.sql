ALTER TABLE t_otp
  ADD COLUMN attempts_left int;

UPDATE t_otp
SET attempts_left = 0
WHERE attempts_left is null;

ALTER TABLE t_otp
  ALTER COLUMN attempts_left set not null;
