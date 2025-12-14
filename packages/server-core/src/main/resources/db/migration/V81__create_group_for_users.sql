-- remove all group memberships and groups, cause they had no purpose until now
DELETE
FROM t_user_group_assignment;
DELETE
FROM t_group;

-- create groups
DO
$$
  DECLARE
    user_record RECORD;
    group_id    UUID;
  BEGIN
    FOR user_record IN SELECT id FROM t_user
      LOOP
        -- Insert a new group for each user (name based on user ID)
        INSERT INTO t_group (id, name, owner_id, created_at)
        VALUES (gen_random_uuid(), 'user_default', user_record.id, current_timestamp)
        RETURNING id INTO group_id;

        -- Create a user_group_assignment with the user as the owner
        INSERT INTO t_user_group_assignment (id, role, user_id, group_id, created_at)
        VALUES (gen_random_uuid(), 'owner', user_record.id, group_id, current_timestamp);
      END LOOP;
  END
$$;
