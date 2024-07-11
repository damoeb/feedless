
ALTER TABLE t_browser_action RENAME TO t_scrape_action;

ALTER TABLE t_scrape_action ADD COLUMN pos INT;
UPDATE t_scrape_action SET pos = 0 WHERE pos is null;
ALTER TABLE t_scrape_action ALTER COLUMN pos SET not null;

ALTER TABLE t_source DROP COLUMN timeout;
ALTER TABLE t_source DROP COLUMN url;
ALTER TABLE t_source DROP COLUMN viewport;
ALTER TABLE t_source DROP COLUMN wait_until;
ALTER TABLE t_source DROP COLUMN additional_wait_sec;
ALTER TABLE t_source DROP COLUMN emit;
