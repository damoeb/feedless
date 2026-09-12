ALTER TABLE IF EXISTS t_segment
  DROP COLUMN time_interval;
ALTER TABLE IF EXISTS t_segment
  ADD COLUMN time_segment__increment character varying(255);
UPDATE t_segment SET time_segment__increment='WEEKLY' where time_segment__increment is null;
ALTER TABLE t_segment ALTER COLUMN time_segment__increment SET NOT NULL;

ALTER TABLE t_report
  RENAME COLUMN email TO recipient_email;
ALTER TABLE IF EXISTS t_report
  ADD COLUMN recipient_name character varying(255) NOT NULL;
ALTER TABLE t_report DROP COLUMN repository_id;
