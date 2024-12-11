ALTER TABLE t_user_group_assignment
  ADD COLUMN role character varying(20);

ALTER TABLE IF EXISTS t_user_group_assignment
  ADD CONSTRAINT role_check CHECK (role::text = ANY (ARRAY['owner'::character varying, 'viewer'::character varying, 'editor'::character varying]::text[]));
