ALTER TABLE t_repository DROP COLUMN for_product;
ALTER TABLE t_repository ADD COLUMN for_product varchar(100);

UPDATE t_repository SET for_product ='rssProxy' WHERE for_product IS NULL;
ALTER TABLE t_repository ALTER COLUMN for_product SET NOT NULL;
