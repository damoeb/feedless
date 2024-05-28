-- feature
ALTER TABLE IF EXISTS t_feature DROP COLUMN IF EXISTS product_id;

ALTER TABLE IF EXISTS t_feature DROP COLUMN IF EXISTS scope;
ALTER TABLE IF EXISTS t_feature
  ADD CONSTRAINT uniquefeaturename UNIQUE (name);
ALTER TABLE IF EXISTS t_feature DROP CONSTRAINT IF EXISTS fkgv8wbrpqnuw2snmkml7s26csv;

-- feature value

ALTER TABLE IF EXISTS t_feature_value DROP COLUMN IF EXISTS plan_id;

ALTER TABLE IF EXISTS t_feature_value
  ADD COLUMN feature_group_id uuid NOT NULL;
ALTER TABLE IF EXISTS t_feature_value
  ADD CONSTRAINT uniquefeatureperplan UNIQUE (feature_group_id, feature_id);
ALTER TABLE IF EXISTS t_feature_value DROP CONSTRAINT IF EXISTS fkr8txka4ino7s5t1eux397j2ln;

ALTER TABLE IF EXISTS t_feature_value
  ADD CONSTRAINT fkh7n02k73onw5cw3afh0qvd1gp FOREIGN KEY (feature_group_id)
    REFERENCES t_plan (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;

-- plan

ALTER TABLE IF EXISTS t_plan DROP COLUMN IF EXISTS availability;

ALTER TABLE IF EXISTS t_plan DROP COLUMN IF EXISTS beforecosts;

ALTER TABLE IF EXISTS t_plan DROP COLUMN IF EXISTS currentcosts;

ALTER TABLE IF EXISTS t_plan DROP COLUMN IF EXISTS name;

ALTER TABLE IF EXISTS t_plan DROP COLUMN IF EXISTS parent_plan_id;

ALTER TABLE ONLY t_plan
  DROP CONSTRAINT fkdti5eeue89brhd7myq69v3klr;

ALTER TABLE IF EXISTS t_plan
  DROP COLUMN product_id;

ALTER TABLE IF EXISTS t_plan
  ADD COLUMN parent_feature_group_id uuid;
ALTER TABLE IF EXISTS t_plan
  ADD COLUMN name character varying(255) NOT NULL DEFAULT '';
ALTER TABLE IF EXISTS t_plan DROP CONSTRAINT IF EXISTS fk707naifuaavfpjkccwxw8jmxh;

ALTER TABLE IF EXISTS t_plan
  ADD CONSTRAINT fkgxqjc0sh95jjftv8id9hqsvth FOREIGN KEY (parent_feature_group_id)
    REFERENCES t_plan (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE NO ACTION;

-- priced product

CREATE TABLE IF NOT EXISTS t_priced_product
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  in_stock integer,
  target_group_enterprise boolean NOT NULL,
  target_group_individual boolean NOT NULL,
  target_group_other boolean NOT NULL,
  price double precision NOT NULL,
  sold_unit character varying(255),
  product_id uuid NOT NULL,
  valid_from timestamp(6) without time zone,
  valid_to timestamp(6) without time zone,
  CONSTRAINT t_priced_product_pkey PRIMARY KEY (id),
  CONSTRAINT fkg7rortdb1i3uf68mltpr5c9ao FOREIGN KEY (product_id)
    REFERENCES t_product (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);

-- product
ALTER TABLE IF EXISTS t_product DROP COLUMN IF EXISTS owner_id;

ALTER TABLE IF EXISTS t_product DROP COLUMN IF EXISTS parent_product_id;

ALTER TABLE IF EXISTS t_product
  ADD COLUMN description character varying(300) NOT NULL DEFAULT ''::character varying;

ALTER TABLE IF EXISTS t_product
  ADD COLUMN feature_group_id uuid;

ALTER TABLE IF EXISTS t_product
  ADD COLUMN is_cloud boolean NOT NULL DEFAULT false;

ALTER TABLE IF EXISTS t_product
  ADD COLUMN is_base_product boolean NOT NULL DEFAULT false;

ALTER TABLE IF EXISTS t_product
  ADD COLUMN part_of character varying(255);
ALTER TABLE IF EXISTS t_product DROP CONSTRAINT IF EXISTS fk5iswe5e30x0hgt9kx5aandina;

ALTER TABLE IF EXISTS t_product DROP CONSTRAINT IF EXISTS fk79ko141pkur3lnuo865vhffdm;

ALTER TABLE IF EXISTS t_product
  ADD CONSTRAINT fkpa5fq82wm7m4aphvboxo4a1on FOREIGN KEY (feature_group_id)
    REFERENCES t_plan (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;

-- feature group
ALTER TABLE t_plan RENAME TO t_feature_group;
ALTER TABLE t_feature_group
  ADD CONSTRAINT uniquename UNIQUE (name);

-- billing

CREATE TABLE IF NOT EXISTS t_billing
(
  id uuid NOT NULL,
  created_at timestamp(6) without time zone NOT NULL,
  is_offer boolean NOT NULL,
  is_paid boolean NOT NULL,
  is_rejected boolean NOT NULL,
  paid_at timestamp(6) without time zone,
  price double precision NOT NULL,
  product_id uuid NOT NULL,
  user_id uuid NOT NULL,
  due_to timestamp(6) without time zone,
  CONSTRAINT t_billing_pkey PRIMARY KEY (id),
  CONSTRAINT fkgq1aivcdo64mvli5dalusnysp FOREIGN KEY (product_id)
    REFERENCES t_product (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE,
  CONSTRAINT fkthobpg5fdar1v4u2lsgxegh5l FOREIGN KEY (user_id)
    REFERENCES t_user (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE
);
