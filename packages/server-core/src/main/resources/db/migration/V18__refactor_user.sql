ALTER TABLE ONLY t_user
  DROP CONSTRAINT uniqueuser;
ALTER TABLE ONLY t_user
  ADD CONSTRAINT uniqueuser UNIQUE (email);

ALTER TABLE t_user
  ADD COLUMN first_name character varying(255);
ALTER TABLE t_user
  ADD COLUMN last_name character varying(255);
ALTER TABLE t_user
  ADD COLUMN country character varying(255);
