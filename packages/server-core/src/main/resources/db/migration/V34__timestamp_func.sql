CREATE OR REPLACE FUNCTION fl_trunc_timestamp_as_millis(ts timestamp without time zone) RETURNS bigint
  LANGUAGE SQL
  IMMUTABLE
  RETURNS NULL ON NULL INPUT
  RETURN EXTRACT(EPOCH FROM date_trunc('day', ts)) * 1000;
