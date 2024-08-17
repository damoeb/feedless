CREATE TABLE IF NOT EXISTS t_repository_cron_run
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  executed_at timestamp(6) without time zone NOT NULL,
  successful boolean NOT NULL,
  message oid NOT NULL,
  repository_id uuid NOT NULL,
  CONSTRAINT t_repository_cron_run_pkey PRIMARY KEY (id),
  CONSTRAINT fk_repository_run__to__repository FOREIGN KEY (repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);
