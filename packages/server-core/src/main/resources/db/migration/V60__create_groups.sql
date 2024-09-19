CREATE TABLE IF NOT EXISTS t_group
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  name character varying(255) NOT NULL,
  CONSTRAINT t_group_pkey PRIMARY KEY (id),
  CONSTRAINT uniquegroupname UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS t_user_group_assignment
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  group_id uuid NOT NULL,
  user_id uuid NOT NULL,
  CONSTRAINT t_group_membership_pkey PRIMARY KEY (id),
  CONSTRAINT uniquegroupmembership UNIQUE (user_id, group_id),
  CONSTRAINT fk_user_group_assignment__to__group FOREIGN KEY (group_id)
    REFERENCES t_group (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE SET NULL ,
  CONSTRAINT fk_user_group_assignment__to__user FOREIGN KEY (user_id)
    REFERENCES t_user (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN group_id uuid;

ALTER TABLE IF EXISTS t_repository
  ADD CONSTRAINT fk_repository__to__group FOREIGN KEY (group_id)
    REFERENCES t_group (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE SET NULL;
