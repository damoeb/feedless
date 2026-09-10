-- Hibernate runs this after building the test schema (ddl-auto=create), which replaces the tables Flyway created, indexes included.
-- It re-adds the partial unique index of V89__harvest_one_real_run_per_source.sql: a JPA 3.1 mapping cannot express a partial index.
CREATE UNIQUE INDEX IF NOT EXISTS uq_harvest_one_running_real_run_per_source ON t_harvest (source_id) WHERE status = 'running' AND dry_run = false;
