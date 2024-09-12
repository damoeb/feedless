CREATE TABLE t_system_settings (
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  name character varying(255) NOT NULL,
  value_bool boolean,
  value_int integer,
  value_text character varying(255)
);

ALTER TABLE ONLY t_system_settings
  ADD CONSTRAINT unique_system_settin_name UNIQUE (name);
