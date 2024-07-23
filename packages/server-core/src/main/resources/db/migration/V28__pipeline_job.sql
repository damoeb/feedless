DROP TABLE IF EXISTS t_document_pipeline_job CASCADE;
DROP TABLE IF EXISTS t_source_pipeline_job CASCADE;

CREATE TABLE IF NOT EXISTS t_pipeline_job
(
  type character varying(31) NOT NULL,
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  attempt integer NOT NULL,
  cool_down_until timestamp(6) without time zone,
  logs text,
  status character varying(255) NOT NULL,
  terminated boolean,
  terminated_at timestamp(6) without time zone,
  document_id uuid,
  executor_id character varying(255),
  executor_params jsonb,
  sequence_id integer,
  source_id uuid NOT NULL,
  CONSTRAINT t_pipeline_job_pkey PRIMARY KEY (id),
  CONSTRAINT fk_pipeline_job__to__document FOREIGN KEY (document_id)
    REFERENCES t_document (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT fk_pipeline_job__to__source FOREIGN KEY (source_id)
    REFERENCES t_source (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS t_feature_group RENAME CONSTRAINT t_plan_pkey TO t_feature_group_pkey;

ALTER TABLE IF EXISTS t_order RENAME CONSTRAINT t_billing_pkey TO t_order_pkey;

ALTER TABLE IF EXISTS t_order RENAME CONSTRAINT t_billing_price_check TO t_order_price_check;

ALTER TABLE IF EXISTS t_plan RENAME CONSTRAINT t_user_plan_subscription_pkey TO t_plan_pkey;

ALTER TABLE IF EXISTS t_scrape_action RENAME CONSTRAINT t_browser_action_pkey TO t_scrape_action_pkey;

ALTER TABLE IF EXISTS t_user DROP COLUMN IF EXISTS subscription_id;
ALTER TABLE IF EXISTS t_user DROP CONSTRAINT IF EXISTS fk_user__to__subscription;
