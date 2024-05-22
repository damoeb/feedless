alter table t_pipeline_job
  add column schema_version int not null default 0;
alter table t_source
  add column schema_version int not null default 0;
alter table t_repository
  add column schema_version int not null default 0;

