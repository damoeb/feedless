UPDATE t_repository SET share_key = substring(gen_random_uuid()::varchar, 0, 9) where t_repository.visibility = 'isPrivate' and share_key=='';
