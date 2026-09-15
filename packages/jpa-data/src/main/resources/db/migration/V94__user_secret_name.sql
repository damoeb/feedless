-- Secrets get a user-chosen name; rows from before names existed are labelled by their creation date.
ALTER TABLE t_user_secret ADD COLUMN name character varying(100);
UPDATE t_user_secret SET name = 'Legacy token (' || to_char(created_at, 'YYYY-MM-DD') || ')';
ALTER TABLE t_user_secret ALTER COLUMN name SET NOT NULL;
