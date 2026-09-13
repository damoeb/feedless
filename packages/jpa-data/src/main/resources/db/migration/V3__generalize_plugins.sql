alter table t_feed_native
  add column plugins jsonb NOT NULL default '[]',
  drop column inlineImages,
  drop column harvestItems;
