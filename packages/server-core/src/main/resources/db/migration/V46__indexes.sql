create index url__idx on t_document (url);
create index repository_id__idx on t_document (repository_id);

create index feature_group_id__idx on t_feature_value (feature_group_id);
create index feature_id__idx on t_feature_value (feature_id);

create index name__idx on t_feature (name);
