-- t_action_click_position
CREATE TABLE IF NOT EXISTS t_action_click_position
(
  x integer NOT NULL,
  y integer NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_click_position_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT t_action_click_position_x_check CHECK (x >= 0),
  CONSTRAINT t_action_click_position_y_check CHECK (y >= 0)
);

-- t_action_click_xpath
CREATE TABLE IF NOT EXISTS t_action_click_xpath
(
  xpath character varying(255) NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_click_xpath_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);


-- t_action_dom
CREATE TABLE IF NOT EXISTS t_action_dom
(
  data character varying(255) COLLATE pg_catalog."default",
  event character varying(50) COLLATE pg_catalog."default",
  xpath character varying(255) COLLATE pg_catalog."default",
  id uuid NOT NULL,
  CONSTRAINT t_action_dom_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);


-- t_action_execute_plugin
CREATE TABLE IF NOT EXISTS t_action_execute_plugin
(
  executor_params jsonb NOT NULL,
  plugin_id character varying(255) NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_execute_plugin_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);


-- t_action_extract_bbox
CREATE TABLE IF NOT EXISTS t_action_extract_bbox
(
  fragment_name character varying(255) NOT NULL,
  h integer NOT NULL,
  w integer NOT NULL,
  x integer NOT NULL,
  y integer NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_extract_bbox_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT t_action_extract_bbox_h_check CHECK (h >= 0),
  CONSTRAINT t_action_extract_bbox_w_check CHECK (w >= 0),
  CONSTRAINT t_action_extract_bbox_x_check CHECK (x >= 0),
  CONSTRAINT t_action_extract_bbox_y_check CHECK (y >= 0)
);


-- t_action_extract_xpath
CREATE TABLE IF NOT EXISTS t_action_extract_xpath
(
  emit text[] NOT NULL,
  fragment_name character varying(255) NOT NULL,
  xpath character varying(255) NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_extract_xpath_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);


-- t_action_fetch
CREATE TABLE IF NOT EXISTS t_action_fetch
(
  additional_wait_sec integer,
  force_prerender boolean NOT NULL,
  is_landscape boolean NOT NULL,
  is_mobile boolean NOT NULL,
  is_variable boolean NOT NULL,
  language character varying(255) COLLATE pg_catalog."default",
  timeout integer,
  url character varying(255) NOT NULL,
  height integer NOT NULL,
  width integer NOT NULL,
  wait_until character varying(255) COLLATE pg_catalog."default",
  id uuid NOT NULL,
  CONSTRAINT t_action_fetch_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT t_action_fetch_height_check CHECK (height >= 768 AND height <= 1440),
  CONSTRAINT t_action_fetch_width_check CHECK (width >= 1024 AND width <= 2560)
);


-- t_action_header
CREATE TABLE IF NOT EXISTS t_action_header(
  name character varying(255) NOT NULL,
  value character varying(255) NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_header_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);


-- t_action_wait
CREATE TABLE IF NOT EXISTS t_action_wait
(
  xpath character varying(255) NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT t_action_wait_pkey PRIMARY KEY (id),
  CONSTRAINT fk_base_entity FOREIGN KEY (id)
    REFERENCES t_scrape_action (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

-- t_browser_action
DROP TABLE IF EXISTS t_browser_action CASCADE;

-- t_scrape_action
CREATE TABLE IF NOT EXISTS t_scrape_action
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  pos integer NOT NULL,
  source_id uuid NOT NULL,
  CONSTRAINT t_scrape_action_pkey PRIMARY KEY (id),
  CONSTRAINT fk_source__to__browser_action FOREIGN KEY (source_id)
    REFERENCES t_source (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS action_type;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS x;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS y;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS data;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS event;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS xpath;

ALTER TABLE IF EXISTS t_scrape_action DROP COLUMN IF EXISTS header;


-- t_source
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS additional_wait_sec;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS debug_console;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS debug_cookies;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS debug_html;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS debug_network;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS debug_screenshot;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS emit;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS prerender;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS schema_version;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS timeout;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS url;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS viewport;
ALTER TABLE IF EXISTS t_source DROP COLUMN IF EXISTS wait_until;
