ALTER TABLE t_action_extract_xpath
  ADD COLUMN unique_by character varying(255);

UPDATE t_action_extract_xpath
SET unique_by = 'text'
WHERE unique_by is null;

ALTER TABLE t_action_extract_xpath
  ALTER COLUMN unique_by set not null;
