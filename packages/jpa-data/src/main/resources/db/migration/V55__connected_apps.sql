-- t_user_secret
ALTER TABLE t_user_secret
  ALTER COLUMN type TYPE character varying(255) COLLATE pg_catalog."default";

ALTER TABLE IF EXISTS t_user_secret DROP CONSTRAINT IF EXISTS t_user_secret_type_check;

-- t_user
ALTER TABLE t_user
  ALTER COLUMN country TYPE character varying(255) COLLATE pg_catalog."default";

ALTER TABLE t_user
  ALTER COLUMN first_name TYPE character varying(255) COLLATE pg_catalog."default";

ALTER TABLE t_user
  ALTER COLUMN last_name TYPE character varying(255) COLLATE pg_catalog."default";
-- ALTER TABLE IF EXISTS t_user
--   ADD CONSTRAINT uk_i6qjjoe560mee5ajdg7v1o6mi UNIQUE (email);

-- t_source
ALTER TABLE IF EXISTS t_source
  RENAME COLUMN erroneous TO is_disabled;

UPDATE t_source
  SET is_disabled = false WHERE is_disabled IS NULL;

ALTER TABLE IF EXISTS t_source
  ALTER COLUMN is_disabled SET NOT NULL;

-- t_connected_app
CREATE TABLE IF NOT EXISTS t_connected_app
(
  is_authorized boolean NOT NULL,
  authorized_at timestamp(6) without time zone,
  chat_id bigint,
  created_at timestamp(6) without time zone NOT NULL,
  id uuid NOT NULL,
  user_id uuid,
  app character varying(31) COLLATE pg_catalog."default" NOT NULL,
  github_id character varying(255) COLLATE pg_catalog."default",
  CONSTRAINT t_connected_app_pkey PRIMARY KEY (id),
  CONSTRAINT fk_connected_app__to__user FOREIGN KEY (user_id)
    REFERENCES t_user (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
)

  TABLESPACE pg_default;


ALTER TABLE IF EXISTS t_action_dom
  DROP CONSTRAINT IF EXISTS t_action_dom_event_check;

ALTER TABLE IF EXISTS t_action_dom
  ADD CONSTRAINT t_action_dom_event_check CHECK (event::text = ANY (ARRAY['purge'::character varying, 'type'::character varying, 'select'::character varying]::text[]));

ALTER TABLE IF EXISTS t_action_dom
  DROP CONSTRAINT IF EXISTS t_action_fetch_wait_until_check;

ALTER TABLE IF EXISTS t_action_fetch
  ADD CONSTRAINT t_action_fetch_wait_until_check CHECK (wait_until::text = ANY (ARRAY['networkidle0'::character varying, 'networkidle2'::character varying, 'load'::character varying, 'domcontentloaded'::character varying]::text[]));

ALTER TABLE IF EXISTS t_annotation
  ALTER COLUMN document_id DROP NOT NULL;

ALTER TABLE IF EXISTS t_annotation
  ALTER COLUMN repository_id DROP NOT NULL;

ALTER TABLE IF EXISTS t_annotation DROP CONSTRAINT IF EXISTS fk_annotation__to__document;

ALTER TABLE IF EXISTS t_annotation DROP CONSTRAINT IF EXISTS fk_annotation__to__repository;

ALTER TABLE IF EXISTS t_annotation
  ADD CONSTRAINT fk_annotation__to__document FOREIGN KEY (document_id)
    REFERENCES t_document (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE SET NULL;

ALTER TABLE IF EXISTS t_annotation
  ADD CONSTRAINT fk_annotation__to__repository FOREIGN KEY (repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE SET NULL;

ALTER TABLE IF EXISTS t_action_dom
  DROP CONSTRAINT IF EXISTS t_action_dom_event_check;

ALTER TABLE IF EXISTS t_action_fetch
  ADD CONSTRAINT t_action_dom_event_check CHECK (wait_until::text = ANY (ARRAY['networkidle0'::character varying, 'networkidle2'::character varying, 'load'::character varying, 'domcontentloaded'::character varying]::text[]));
--
ALTER TABLE IF EXISTS t_action_dom
  DROP CONSTRAINT IF EXISTS t_action_dom_event_check;

ALTER TABLE IF EXISTS t_action_dom
  ADD CONSTRAINT t_action_dom_event_check CHECK (event::text = ANY (ARRAY['purge'::character varying, 'type'::character varying, 'select'::character varying]::text[]));

