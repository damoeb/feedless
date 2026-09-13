CREATE TABLE IF NOT EXISTS t_harvest
(
  errornous boolean NOT NULL,
  items_added integer NOT NULL,
  items_ignored integer NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  finished_at timestamp(6) without time zone,
  started_at timestamp(6) without time zone,
  id uuid NOT NULL,
  repository_id uuid NOT NULL,
  logs character varying(32600) COLLATE pg_catalog."default" NOT NULL,
  CONSTRAINT t_harvest_pkey PRIMARY KEY (id),
  CONSTRAINT fk_harvest__to__repository FOREIGN KEY (repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
)

  TABLESPACE pg_default;
