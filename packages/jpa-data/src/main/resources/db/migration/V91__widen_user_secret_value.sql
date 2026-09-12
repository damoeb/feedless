-- A user secret's value is the API JWT itself. Since the token carries the user's acting group next
-- to the user, it no longer fits in 400 characters. Widening a varchar is metadata-only in Postgres.
ALTER TABLE t_user_secret ALTER COLUMN value TYPE character varying(2048);
