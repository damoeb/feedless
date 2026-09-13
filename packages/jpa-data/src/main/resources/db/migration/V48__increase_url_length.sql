alter table t_attachment
  alter column original_url type varchar(1500);

alter table t_attachment
  alter column remote_data_url type varchar(1500);

alter table t_pipeline_job
  alter column url type varchar(1500);

alter table t_document
  alter column url type varchar(1500);
