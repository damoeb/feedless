UPDATE t_user
  SET email = concat(id, '@feedless.com')
  WHERE email IS NULL OR email = '';

ALTER TABLE t_user
  ALTER COLUMN email set NOT NULL;
