SELECT * FROM pg_extension;

ALTER TABLE t_document ADD COLUMN IF NOT EXISTS lat_lon geometry;
UPDATE t_document d SET lat_lon = ST_Point(d.lat, d.lon) WHERE lat IS NOT NULL AND lon IS NOT NULL;
ALTER TABLE t_document DROP COLUMN IF EXISTS lat;
ALTER TABLE t_document DROP COLUMN IF EXISTS lon;

ALTER TABLE t_source ADD COLUMN IF NOT EXISTS lat_lon geometry;
UPDATE t_source d SET lat_lon = ST_Point(d.lat, d.lon) WHERE lat IS NOT NULL AND lon IS NOT NULL;
ALTER TABLE t_source DROP COLUMN IF EXISTS lat;
ALTER TABLE t_source DROP COLUMN IF EXISTS lon;

ALTER TABLE t_source DROP COLUMN lat_lon_caption;
