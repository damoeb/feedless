-- Migrate from org.migor.feedless.generated.types.PluginExecutionParams JSON
-- to org.migor.feedless.data.jpa.repository.PluginExecution
-- Example: {"org_feedless_feed":{"generic":{"contextXPath":"","dateIsStartOfEvent":false,"dateXPath":"","paginationXPath":"","extendContext":"NONE","linkXPath":""}}}

UPDATE t_pipeline_job
SET executor_params = COALESCE(
  (executor_params -> 'org_feedless_feed')::text,
  (executor_params -> 'org_feedless_diff_records')::text,
  (executor_params -> 'org_feedless_filter')::text,
  (executor_params ->> 'jsonData'), -- already text
  (executor_params -> 'org_feedless_fulltext')::text,
  (executor_params -> 'org_feedless_conditional_tag')::text)
WHERE type = 'd';

update t_repository
set plugins = (SELECT jsonb_agg(
                        jsonb_build_object(
                          'id', elem ->> 'id',
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
               FROM jsonb_array_elements(plugins) AS elem);

PluginExecutionJson
