CREATE INDEX document_latlon_idx
  ON t_document
    USING GIST (lat_lon);

CREATE INDEX document_published_at_idx
  ON t_document (released_at);

CREATE INDEX document_starting_at_idx
  ON t_document (starting_at);

CREATE INDEX document_created_at_idx
  ON t_document (created_at);

CREATE INDEX repository_created_at_idx
  ON t_repository (created_at);

CREATE INDEX source_created_at_idx
  ON t_source (created_at);
