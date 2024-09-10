ALTER TABLE t_repository
  ADD COLUMN tags text[];

UPDATE t_repository SET tags='{}' WHERE tags IS NULL;
ALTER TABLE t_repository ALTER COLUMN tags SET NOT NULL;

CREATE OR REPLACE FUNCTION fl_array_contains(bodyOfTags text[], search text[], matchEvery boolean) RETURNS boolean
  LANGUAGE SQL
  IMMUTABLE
  RETURNS NULL ON NULL INPUT
  RETURN
    CASE WHEN matchEvery IS TRUE THEN bodyOfTags @> search ELSE bodyOfTags && search END;
--
-- ALTER TABLE t_repository
--   ADD COLUMN description_search tsvector;
-- UPDATE t_repository SET description_search = to_tsvector(description) where description_search IS NULL;
--
-- ALTER TABLE t_repository ALTER COLUMN description_search SET GENERATED ALWAYS AS (to_tsvector(description)) STORED;
--
-- ALTER TABLE t_repository
--   ADD COLUMN title_search tsvector
--     GENERATED ALWAYS AS (to_tsvector(title)) STORED;
--
-- CREATE OR REPLACE FUNCTION fl_fulltext_search(body text, query text) RETURNS boolean
--   LANGUAGE SQL  IMMUTABLE
--   RETURNS NULL ON NULL INPUT
--   RETURN
--     to_tsvector(body) @@ websearch_to_tsquery(query);


ALTER TABLE t_repository
  ADD COLUMN pulls_per_month int;

UPDATE t_repository
  SET pulls_per_month = 0 where pulls_per_month IS NULL;

ALTER TABLE t_repository
  ALTER COLUMN pulls_per_month SET NOT NULL;

ALTER TABLE t_repository
  ADD COLUMN last_pull_sync timestamp(6) without time zone;
