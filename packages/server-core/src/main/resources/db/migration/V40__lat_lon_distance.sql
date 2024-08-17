CREATE OR REPLACE FUNCTION fl_latlon_distance(a_lat_lon geometry, b_lat double precision, b_lon double precision) RETURNS double precision
  LANGUAGE SQL
  IMMUTABLE
  RETURNS NULL ON NULL INPUT
  RETURN (st_distance(st_transform(a_lat_lon, 3857), st_transform(st_point(b_lat, b_lon, 4326), 3857)) * cosd(42.3521)) / 1000.0;
