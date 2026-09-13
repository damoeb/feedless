alter table t_attachment
  alter column original_url type varchar(1000);

alter table t_attachment
  alter column remote_data_url type varchar(1000);
