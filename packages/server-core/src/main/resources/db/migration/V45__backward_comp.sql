-- feedspecification
-- {
--       "selectors": {
--         "count": null,
--         "score": null,
--         "contexts": null,
--         "dateXPath": "./a[1]/section[1]/ul[1]/li[2]/time[1]",
--         "linkXPath": "./a[1]",
--         "contextXPath": "//div[1]/div[3]/main[1]/div[2]/article",
--         "extendContext": "NONE",
--         "paginationXPath": null,
--         "dateIsStartOfEvent": false
--       },
--       "fetchOptions": {
--         "emit": "markup",
--         "baseXpath": "",
--         "prerender": false,
--         "websiteUrl": "https://telepolis.de",
--         "prerenderScript": "",
--         "prerenderWaitUntil": "load"
--       },
--       "parserOptions": {
--         "version": "0.1",
--         "strictMode": false,
--         "minLinkGroupSize": 2,
--         "minWordCountOfLink": 1
--       },
--       "refineOptions": {
--         "filter": null
--       }
--     }

ALTER TABLE t_action_fetch ALTER COLUMN url TYPE varchar(900);

with source as (
    select ts.id as id, gen_random_uuid() as fetch_action_id, gen_random_uuid() as plugin_action_id, now() as created_at, ts.feedurl, tfg.feedspecification as gen_feed_params
    from t_source ts
    left join t_feed_generic tfg on ts.generic_feed_id = tfg.id
  ),
  parent_fetch_action as (
    insert into t_scrape_action (id, created_at, source_id, pos)
    SELECT fetch_action_id, created_at, id, 0 from source RETURNING id
  ),
  fetch_action as (
    insert into t_action_fetch (id, url, force_prerender, is_variable, width, height, is_mobile, is_landscape)
    SELECT fetch_action_id, case when ts.gen_feed_params is null then ts.feedurl else (ts.gen_feed_params ->> 'fetchOptions')::json ->> 'websiteUrl' end, case when ts.gen_feed_params is null then false else jsonb_path_query_first(ts.gen_feed_params, '$.fetchOptions.prerender')::boolean end, false, 1024, 768, false, false from source ts
  ),
  parent_plugin_action as (
    insert into t_scrape_action (id, created_at, source_id, pos)
    SELECT plugin_action_id, created_at, id, 1 from source RETURNING id
  )
--   plugin_action
  insert into t_action_execute_plugin (id, plugin_id, executor_params)
  SELECT plugin_action_id, 'org_feedless_feed', case when ts.gen_feed_params is null then '{"org_feedless_feed": {}}'::jsonb else concat('{"org_feedless_feed": {"generic": {"dateXPath":', jsonb_path_query_first(ts.gen_feed_params, '$.selectors.dateXPath'), ', "linkXPath":', jsonb_path_query_first(ts.gen_feed_params, '$.selectors.linkXPath'), ', "contextXPath":', jsonb_path_query_first(ts.gen_feed_params, '$.selectors.contextXPath'), ', "extendContext": ', jsonb_path_query_first(ts.gen_feed_params, '$.selectors.extendContext'), ', "paginationXPath": "", "dateIsStartOfEvent": ', jsonb_path_query_first(ts.gen_feed_params, '$.selectors.dateIsStartOfEvent'), '}}}')::jsonb end from source ts
;

with importer as (
  select ti.feedid as source_id, gen_random_uuid() as filter_action_id, ti.createdat as created_at, ti.filter
  from t_importer ti where ti.filter IS NOT NULL AND ti.filter != ''
),
     parent_plugin_action as (
       insert into t_scrape_action (id, created_at, source_id, pos)
         SELECT filter_action_id, created_at, source_id, 2 from importer RETURNING id
     )
--   plugin_action
insert into t_action_execute_plugin (id, plugin_id, executor_params)
SELECT filter_action_id, 'org_feedless_filter', concat('{"org_feedless_filter": [{"expression": "', ti.filter,'"}]}')::jsonb from importer ti
;
DROP TABLE t_importer;

ALTER TABLE t_source DROP CONSTRAINT fk_native_feed__generic_feed;
ALTER TABLE t_source DROP COLUMN feedurl;
ALTER TABLE t_source DROP COLUMN generic_feed_id;
ALTER TABLE t_source DROP COLUMN lastcheckedat;
ALTER TABLE t_source DROP COLUMN harvestratefixed;
-- ALTER TABLE t_source RENAME CONSTRAINT fkpdn9867c5spvfb785hkvuh71o TO fk_source__to__repository;
drop table t_feed_generic;

ALTER TABLE t_scrape_action RENAME CONSTRAINT fk_source__to__browser_action TO fk_scrape_action__to__source;
ALTER TABLE t_segment RENAME CONSTRAINT fk64dblt0y6ne0i6eyeilrhslx7 TO fk_repository__to__segmentation;
ALTER TABLE t_repository DROP CONSTRAINT fk_bucket__user;
UPDATE t_repository SET plugins = '[]'::jsonb where plugins = '{}'::jsonb;
UPDATE t_repository SET type = 'repository' where type = '';
UPDATE t_repository SET last_updated_at = now() where last_updated_at is null;


-- free feedless subscription
INSERT INTO t_product (id, created_at, name, description, feature_group_id, is_cloud, is_base_product, part_of) VALUES (gen_random_uuid(), '2024-08-18 13:54:56.624000', 'feedless Free', 'Getting started', null, true, true, 'feedless');

insert into t_plan(
  id, created_at, product_id, started_at, user_id
)
SELECT gen_random_uuid(), u.created_at, (
  select p.id as id
  from t_product p
  where p.part_of = 'feedless' and p.is_base_product = true
), created_at, u.id from t_user u
;

ALTER TABLE t_pipeline_job ALTER COLUMN source_id DROP NOT NULL;
UPDATE t_repository SET visibility = 'isPrivate' where visibility != 'isPrivate';
