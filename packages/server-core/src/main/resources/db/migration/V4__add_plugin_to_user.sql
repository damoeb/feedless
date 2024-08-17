begin;
alter table t_user
  add column plugins jsonb NOT NULL default '{}';
commit;
