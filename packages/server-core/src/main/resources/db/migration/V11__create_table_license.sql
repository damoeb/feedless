CREATE TABLE IF NOT EXISTS t_license
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  billing_id uuid NOT NULL,
  payload character varying(1000) NOT NULL,
  CONSTRAINT t_license_pkey PRIMARY KEY (id),
  CONSTRAINT fkmj0osl4etytha4j8ytxake8jb FOREIGN KEY (billing_id)
    REFERENCES t_billing (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);
