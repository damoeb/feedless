INSERT INTO t_connected_app(id, created_at, is_authorized, authorized_at, chat_id, user_id, github_id, app)
SELECT gen_random_uuid(), created_at, true, created_at, null, id, githubid, 'github' FROM t_user
WHERE githubid IS NOT NULL;

ALTER TABLE t_user DROP COLUMN githubid;
