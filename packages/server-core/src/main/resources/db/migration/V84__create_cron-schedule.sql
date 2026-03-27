CREATE TABLE t_cron_schedule
(
  id                        uuid primary key,
  created_at                timestamp(6) without time zone NOT NULL,
  scheduler_expression      character varying(50)          not null,
  trigger_scheduled_next_at timestamp(6) without time zone
); -- nullable

ALTER TABLE t_report
  ADD COLUMN cron_schedule_id uuid,
  ADD CONSTRAINT fk_report__to__cron_schedule FOREIGN KEY (cron_schedule_id) REFERENCES t_cron_schedule (id) ON DELETE CASCADE;

-- create chron schedules for existing reports
WITH inserted AS (SELECT r.id              AS report_id,
                         gen_random_uuid() AS cron_id,
                         r.next_reported_at
                  FROM t_report r),
     created AS (
       INSERT INTO t_cron_schedule (
                                    id,
                                    created_at,
                                    scheduler_expression,
                                    trigger_scheduled_next_at
         )
         SELECT cron_id,
                NOW(),
                '0 0 12 * * 0',
                next_reported_at
         FROM inserted
         RETURNING id)
-- link cron_scheduled entity
UPDATE t_report r
SET cron_schedule_id = i.cron_id
FROM inserted i
WHERE r.id = i.report_id;

ALTER TABLE t_report
  DROP COLUMN next_reported_at;
