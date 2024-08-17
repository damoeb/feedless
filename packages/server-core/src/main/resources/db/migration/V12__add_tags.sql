ALTER TABLE t_document
  ADD COLUMN tags text[];
ALTER TABLE t_scrape_source
  ADD COLUMN tags text[];
