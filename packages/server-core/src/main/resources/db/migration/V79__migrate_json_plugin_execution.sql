-- migrate DocumentPipelineJobEntity.executorParams
-- old: {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": null, "org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}
-- new: {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}

DO $$
DECLARE
  temp_user_id uuid;
  temp_repository_id uuid;
  temp_source_id uuid;
  temp_document_id uuid;
  temp_pipeline_job_id uuid;
  temp_segment_id uuid;
  temp_scrape_action_id uuid;
BEGIN
  -- Create temporary test entities to ensure migration has data to migrate
  temp_user_id := gen_random_uuid();
  temp_repository_id := gen_random_uuid();
  temp_source_id := gen_random_uuid();
  temp_document_id := gen_random_uuid();
  temp_pipeline_job_id := gen_random_uuid();
  temp_segment_id := gen_random_uuid();
  temp_scrape_action_id := gen_random_uuid();

  -- Create temporary user
  INSERT INTO t_user (
    id, created_at, email, has_validated_email, total_usage_mb, is_root, is_anonymous,
    karma, is_spamming_submissions, is_spamming_votes, is_shaddow_banned, is_banned,
    hasapprovedterms, is_locked
  ) VALUES (
    temp_user_id, NOW(), 'temp_migration_test@example.com', false, 0, false, true,
    0, false, false, false, false, false, false
  );

  -- Create temporary repository with old plugin format
  INSERT INTO t_repository (
    id, created_at, title, description, visibility, scheduler_expression,
    document_count_since_creation, is_archived, for_product, schema_version,
    pulls_per_month, owner_id, plugins, share_key, tags, retention_max_age_days_field,
    push_notifications_enabled
  ) VALUES (
    temp_repository_id, NOW(), 'temp_migration_test', '', 'isPrivate', '0 0 */12 * * *',
    0, false, 'feedless', 0, 0, temp_user_id,
    '[{"id": "org_feedless_fulltext", "params": {"org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}}}]'::jsonb,
    'temp_test', '{}', 'publishedAt', false
  );

  -- Create temporary source
  INSERT INTO t_source (
    id, created_at, title, repository_id, is_disabled, last_records_retrieved,
    errors_in_succession
  ) VALUES (
    temp_source_id, NOW(), 'temp_migration_test', temp_repository_id, false, 0, 0
  );

  -- Create temporary document
  INSERT INTO t_document (
    id, created_at, type, content_text, is_dead, is_flagged, released_at,
    repository_id, score, status, updated_at, url, content_hash
  ) VALUES (
    temp_document_id, NOW(), 'ARTICLE', 'temp_migration_test', false, false, NOW(),
    temp_repository_id, 0, 'released', NOW(), 'https://example.com/temp', ''
  );

  -- Create temporary document pipeline job with old executorParams format
  INSERT INTO t_pipeline_job (
    id, created_at, type, attempt, status, terminated, sequence_id, source_id,
    document_id, executor_id, executor_params
  ) VALUES (
    temp_pipeline_job_id, NOW(), 'd', 0, 'PENDING', false, 0, temp_source_id,
    temp_document_id, 'org_feedless_fulltext',
    '{"org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true}}'::jsonb
  );

  -- Create temporary segmentation with old report_plugin format
  INSERT INTO t_segment (
    id, created_at, report_max_size, time_segment__starting_at, time_segment__increment,
    repository_id, report_plugin
  ) VALUES (
    temp_segment_id, NOW(), 10, NOW(), 'WEEKLY', temp_repository_id,
    '{"id": "org_feedless_feed", "params": {"org_feedless_feed": {"filter": "date(7d)"}}}'::jsonb
  );

  -- Create temporary scrape action (base entity for execute action)
  INSERT INTO t_scrape_action (
    id, created_at, pos, source_id
  ) VALUES (
    temp_scrape_action_id, NOW(), 0, temp_source_id
  );

  -- Create temporary execute action with old executorParams format
  INSERT INTO t_action_execute_plugin (
    id, plugin_id, executor_params
  ) VALUES (
    temp_scrape_action_id, 'org_feedless_filter',
    '{"org_feedless_filter": [{"composite": {"exclude": {"title": {"value": "test", "operator": "contains"}}}}]}'::jsonb
  );

  -- MIGRATION: DocumentPipelineJobEntity.executorParams
  UPDATE t_pipeline_job
  SET executor_params = COALESCE(
    (executor_params -> 'org_feedless_feed'),
    (executor_params -> 'org_feedless_diff_records'),
    (executor_params -> 'org_feedless_filter'),
    (executor_params -> 'jsonData'),
    (executor_params -> 'org_feedless_fulltext'),
    (executor_params -> 'org_feedless_conditional_tag'),
    (executor_params -> 'org_feedless_diff_email_forward'))
  WHERE type = 'd';

  -- MIGRATION: AbstractRepositoryEntity.plugins
  -- old: [{"id": "org_feedless_fulltext", "params": {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": null, "org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}}]

  UPDATE t_repository
  SET plugins = (
    COALESCE(
      (SELECT jsonb_agg(
        jsonb_build_object(
          'id', elem ->> 'id',
          'params', COALESCE(
            elem -> 'params' -> 'org_feedless_feed',
            elem -> 'params' -> 'org_feedless_diff_records',
            elem -> 'params' -> 'org_feedless_filter',
            elem -> 'params' -> 'jsonData',
            elem -> 'params' -> 'org_feedless_fulltext',
            elem -> 'params' -> 'org_feedless_conditional_tag',
            elem -> 'params' -> 'org_feedless_diff_email_forward'
          )
        )
      )
      FROM jsonb_array_elements(plugins) AS elem),
      '[]'::jsonb
    )
  );

  -- MIGRATION: SegmentationEntity.reportPlugin
  UPDATE t_segment
  SET report_plugin = jsonb_build_object(
    'id', report_plugin ->> 'id',
    'params', COALESCE(
      report_plugin -> 'params' -> 'org_feedless_feed',
      report_plugin -> 'params' -> 'org_feedless_diff_records',
      report_plugin -> 'params' -> 'org_feedless_filter',
      report_plugin -> 'params' -> 'jsonData',
      report_plugin -> 'params' -> 'org_feedless_fulltext',
      report_plugin -> 'params' -> 'org_feedless_conditional_tag',
      report_plugin -> 'params' -> 'org_feedless_diff_email_forward'
    )
  )
  WHERE report_plugin IS NOT NULL;

  -- MIGRATION: ExecuteActionEntity.executorParams
  -- old: {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": [{"composite": {"exclude": {"link": null, "index": null, "title": {"value": "ist", "operator": "contains"}, "content": null}, "include": null}, "expression": null}], "org_feedless_fulltext": null, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}
  UPDATE t_action_execute_plugin
  SET executor_params = COALESCE(
    (executor_params -> 'org_feedless_feed'),
    (executor_params -> 'org_feedless_diff_records'),
    (executor_params -> 'org_feedless_filter'),
    (executor_params -> 'jsonData'),
    (executor_params -> 'org_feedless_fulltext'),
    (executor_params -> 'org_feedless_conditional_tag'),
    (executor_params -> 'org_feedless_diff_email_forward'))
  WHERE executor_params IS NOT NULL;

  -- Cleanup temporary test entities (in reverse dependency order)
  DELETE FROM t_action_execute_plugin WHERE id = temp_scrape_action_id;
  DELETE FROM t_scrape_action WHERE id = temp_scrape_action_id;
  DELETE FROM t_segment WHERE id = temp_segment_id;
  DELETE FROM t_pipeline_job WHERE id = temp_pipeline_job_id;
  DELETE FROM t_document WHERE id = temp_document_id;
  DELETE FROM t_source WHERE id = temp_source_id;
  DELETE FROM t_repository WHERE id = temp_repository_id;
  DELETE FROM t_user WHERE id = temp_user_id;
END $$;
