begin;
alter table t_web_document
  add column executed_plugins jsonb NOT NULL default '[]';

alter table t_web_document
  rename column plugins to pending_plugins;

alter table t_importer
  add column plugins jsonb NOT NULL default '[]';

commit;
