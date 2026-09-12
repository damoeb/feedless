ALTER TABLE t_cron_schedule
  RENAME COLUMN trigger_scheduled_next_at TO scheduled_next_at;

UPDATE t_cron_schedule
set scheduled_next_at = NOW()
where scheduled_next_at is null;

ALTER TABLE t_cron_schedule
  ALTER COLUMN scheduled_next_at set NOT NULL;
ALTER TABLE t_cron_schedule
  ADD COLUMN executed_last_at timestamp(6) without time zone;


-- move report_plugin to t_report
alter table t_report
  add column reporter_plugin jsonb NOT NULL default '{}';

WITH inserted AS (SELECT r.id            AS report_id,
                         s.report_plugin as report_plugin
                  FROM t_report r
                         inner join t_segment s
                                    on s.id = r.segmentation_id)
UPDATE t_report r
SET reporter_plugin = i.report_plugin
FROM inserted i
WHERE r.id = i.report_id;

--

ALTER TABLE t_segment
  DROP COLUMN report_plugin;


