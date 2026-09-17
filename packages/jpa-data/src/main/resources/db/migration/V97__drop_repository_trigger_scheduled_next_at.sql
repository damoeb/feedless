-- flyway:pinned-out
-- Raise spring.flyway.target to 97 only once per-source scheduling (V96) has run cleanly in production.
ALTER TABLE t_repository DROP COLUMN trigger_scheduled_next_at;
