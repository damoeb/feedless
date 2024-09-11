ALTER TABLE t_user_secret RENAME lastusedat to last_used_at;
ALTER TABLE t_user_secret RENAME validuntil to valid_until;
ALTER TABLE t_agent RENAME openinstance to open_instance;
ALTER TABLE t_agent RENAME lastsyncedat to last_synced_at;
