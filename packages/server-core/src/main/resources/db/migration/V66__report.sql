-- segmentation
ALTER TABLE t_segment
  ADD COLUMN starting_at timestamp(6) without time zone NOT NULL;

ALTER TABLE IF EXISTS t_segment
  ADD COLUMN time_interval character varying(255) NOT NULL;

-- ALTER TABLE IF EXISTS t_segment
--   ADD COLUMN repository_id uuid not null;
ALTER TABLE t_segment
  DROP CONSTRAINT fk_repository__to__segmentation;

ALTER TABLE t_segment
  ADD CONSTRAINT fk_segmentation__to__repository FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;
ALTER TABLE t_segment
  DROP COLUMN digest;
ALTER TABLE t_segment RENAME COLUMN size TO report_max_size;
ALTER TABLE t_segment RENAME COLUMN starting_at TO time_segment__starting_at;
ALTER TABLE t_segment
  DROP COLUMN sort_by;
ALTER TABLE t_segment
  DROP COLUMN sort_asc;
ALTER TABLE t_segment
  ADD COLUMN report_plugin jsonb NOT NULL;
ALTER TABLE t_segment
  ADD COLUMN filter_latlon geometry;
ALTER TABLE t_segment
  ADD COLUMN filter_latlon_distance double precision;
-- ALTER TABLE t_segment
--   ADD COLUMN filter_tags text[] not null;

-- reports
DELETE FROM t_mail_forward;

ALTER TABLE t_mail_forward RENAME authorizedat TO authorized_at;

ALTER TABLE IF EXISTS t_mail_forward
  ADD COLUMN authorization_attempt integer NOT NULL;

ALTER TABLE IF EXISTS t_mail_forward
  ADD COLUMN last_requested_authorization timestamp(6) without time zone;

-- constraints
ALTER TABLE t_mail_forward
  DROP CONSTRAINT fk_mail__to__repository;

ALTER TABLE IF EXISTS t_mail_forward
  ADD COLUMN user_id uuid; -- nullable
ALTER TABLE t_mail_forward
  ADD CONSTRAINT fk_report__to__user FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS t_mail_forward
  ADD COLUMN segmentation_id uuid not null;
ALTER TABLE t_mail_forward
  ADD CONSTRAINT fk_report__to__segment FOREIGN KEY (segmentation_id) REFERENCES t_segment(id) ON DELETE CASCADE;

ALTER TABLE t_mail_forward RENAME TO t_report;

ALTER TABLE t_report
  ADD COLUMN last_reported_at timestamp(6) without time zone;
ALTER TABLE t_report
  ADD COLUMN next_reported_at timestamp(6) without time zone NOT NULL;

ALTER TABLE t_report
  ADD COLUMN is_disabled boolean NOT NULL DEFAULT false;

ALTER TABLE t_report
  ADD COLUMN disabled_at timestamp(6) without time zone;
