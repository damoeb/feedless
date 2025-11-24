-- migrate DocumentPipelineJobEntity.executorParams
-- old: {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": null, "org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}
-- new:
UPDATE t_pipeline_job
SET executor_params = COALESCE(
  (executor_params -> 'org_feedless_feed'),
  (executor_params -> 'org_feedless_diff_records'),
  (executor_params -> 'org_feedless_filter'),
  (executor_params -> 'jsonData'),
  (executor_params -> 'org_feedless_fulltext'),
  (executor_params -> 'org_feedless_conditional_tag'))
WHERE type = 'd';

-- migrate AbstractRepositoryEntity.plugins
-- old: [{"id": "org_feedless_fulltext", "params": {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": null, "org_feedless_fulltext": {"summary": false, "readability": false, "inheritParams": true, "onErrorRemove": null}, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}}]
-- new:
update t_repository
set plugins = (COALESCE((SELECT jsonb_agg(
                                  jsonb_build_object(
                                    'id', elem ->> 'id',
                                    'params',
                                    jsonb_build_object( -- Start of the nested 'params' object
                                      'paramsJsonString',
                                      COALESCE(
                                        elem -> 'params' ->> 'org_feedless_feed',
                                        elem -> 'params' ->> 'org_feedless_diff_records',
                                        elem -> 'params' ->> 'org_feedless_filter',
                                        elem -> 'params' ->> 'jsonData',
                                        elem -> 'params' ->> 'org_feedless_fulltext',
                                        elem -> 'params' ->> 'org_feedless_conditional_tag',
                                        elem -> 'params' ->> 'org_feedless_diff_email_forward'
                                      )
                                    )
                                  )
                                )
                         FROM jsonb_array_elements(plugins) AS elem),
                        '[]'::jsonb));

-- migrate SegmentationEntity.reportPlugin
UPDATE t_segment
SET report_plugin = jsonb_build_object(
  'id', report_plugin ->> 'id',
  'paramsJsonString',
  COALESCE(
    report_plugin -> 'params' ->> 'org_feedless_feed',
    report_plugin -> 'params' ->> 'org_feedless_diff_records',
    report_plugin -> 'params' ->> 'org_feedless_filter',
    report_plugin -> 'params' ->> 'jsonData',
    report_plugin -> 'params' ->> 'org_feedless_fulltext',
    report_plugin -> 'params' ->> 'org_feedless_conditional_tag',
    report_plugin -> 'params' ->> 'org_feedless_diff_email_forward'
  )
                    )
WHERE report_plugin IS NOT NULL;

-- migrate ExecuteActionEntity.executorParams
-- old: {"jsonData": null, "org_feedless_feed": null, "org_feedless_filter": [{"composite": {"exclude": {"link": null, "index": null, "title": {"value": "ist", "operator": "contains"}, "content": null}, "include": null}, "expression": null}], "org_feedless_fulltext": null, "org_feedless_conditional_tag": null, "org_feedless_diff_email_forward": null}
-- new:
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
