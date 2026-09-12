DROP TABLE map_plan_to_feature;
DROP TABLE t_feature;
DROP TABLE t_user_plan_subscription;
DROP TABLE t_article;
DROP TABLE t_hyperlink;
DROP TABLE t_web_document;

UPDATE t_user set plan_id = null where plan_id is not null;
DROP TABLE t_plan CASCADE ;


--
-- Name: t_agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_agent (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    connection_id character varying(255) NOT NULL,
    lastsyncedat timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    openinstance boolean NOT NULL,
    owner_id uuid NOT NULL,
    secret_id uuid NOT NULL,
    version character varying(255) NOT NULL
);


--
-- Name: t_annotation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_annotation (
    type character varying(31) NOT NULL,
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    document_id uuid NOT NULL,
    owner_id uuid NOT NULL
);


--
-- Name: t_annotation_text; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_annotation_text (
    comment_id uuid NOT NULL,
    from_char integer,
    to_char integer NOT NULL,
    id uuid NOT NULL,
    CONSTRAINT t_annotation_text_to_char_check CHECK ((to_char >= 0))
);


--
-- Name: t_annotation_vote; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_annotation_vote (
    is_downvote boolean NOT NULL,
    is_flag boolean NOT NULL,
    is_upvote boolean NOT NULL,
    id uuid NOT NULL
);


--
-- Name: t_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_attachment (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    content_type character varying(255) NOT NULL,
    data bytea,
    document_id uuid NOT NULL,
    has_data boolean NOT NULL,
    name character varying(255),
    original_url character varying(255),
    remote_data_url character varying(255)
);


--
-- Name: t_browser_action; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_browser_action (
    action_type character varying(31) NOT NULL,
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    source_id uuid NOT NULL,
    x integer,
    y integer,
    data character varying(255),
    event character varying(50),
    xpath character varying(255),
    header character varying(255)
);


--
-- Name: t_document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_document (
    type character varying(31) NOT NULL,
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    content_html text,
    content_raw bytea,
    content_raw_mime character varying(50),
    content_text text NOT NULL,
    content_title character varying(256),
    image_url character varying(1000),
    is_dead boolean NOT NULL,
    is_flagged boolean NOT NULL,
    parent_id uuid,
    released_at timestamp(6) without time zone NOT NULL,
    repository_id uuid NOT NULL,
    score integer NOT NULL,
    scored_at timestamp(6) without time zone,
    starting_at timestamp(6) without time zone,
    status character varying(50) NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    url character varying(1000) NOT NULL,
    is_original_poster boolean
);


--
-- Name: t_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_feature (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(50) NOT NULL,
    product_id uuid NOT NULL,
    scope character varying(50)
);


--
-- Name: t_feature_value; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_feature_value (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    feature_id uuid NOT NULL,
    plan_id uuid NOT NULL,
    value_bool boolean,
    value_int integer,
    value_type character varying(50) NOT NULL
);


--
-- Name: t_mail_forward; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_mail_forward (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    authorized boolean NOT NULL,
    authorizedat timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    repository_id uuid NOT NULL
);


--
-- Name: t_notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_notification (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    message character varying(255) NOT NULL,
    owner_id uuid NOT NULL
);


--
-- Name: t_otp; Type: TABLE; Schema: public; Owner: -
--

-- CREATE TABLE t_otp (
--     id uuid NOT NULL,
--     created_at timestamp(6) without time zone NOT NULL,
--     password character varying(255) NOT NULL,
--     user_id uuid NOT NULL,
--     valid_until timestamp(6) without time zone NOT NULL
-- );

ALTER TABLE t_otp
  RENAME COLUMN createdat to created_at;

ALTER TABLE t_otp
  RENAME COLUMN userid to user_id;

ALTER TABLE t_otp
  RENAME COLUMN validuntil to valid_until;

-- ALTER TABLE t_otp
--   RENAME CONSTRAINT fk36b6qk1g90ucc651dole1w4et TO fk3s2gywtxnvtcjvbkh52jyawnc;

--
-- Name: t_pipeline_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_pipeline_job (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    attempt integer NOT NULL,
    cool_down_until timestamp(6) without time zone,
    document_id uuid NOT NULL,
    executorid character varying(255) NOT NULL,
    executorparams jsonb,
    sequence_id integer NOT NULL,
    terminated boolean NOT NULL,
    terminatedat timestamp(6) without time zone
);


--
-- Name: t_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_plan (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    availability character varying(50) NOT NULL,
    beforecosts double precision,
    currentcosts double precision NOT NULL,
    name character varying(50) NOT NULL,
    parent_plan_id uuid,
    product_id uuid NOT NULL
);


--
-- Name: t_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_product (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    name character varying(50) NOT NULL,
    owner_id uuid NOT NULL,
    parent_product_id uuid
);


--
-- Name: t_repository; Type: TABLE; Schema: public; Owner: -
--

-- CREATE TABLE t_repository (
--     type character varying(31) NOT NULL,
--     id uuid NOT NULL,
--     created_at timestamp(6) without time zone NOT NULL,
--     is_archived boolean NOT NULL,
--     description character varying(1024) NOT NULL,
--     disabled_from timestamp(6) without time zone,
--     document_count_since_creation integer,
--     last_updated_at timestamp(6) without time zone,
--     owner_id uuid NOT NULL,
--     plugins jsonb NOT NULL,
--     for_product smallint NOT NULL,
--     retention_max_age_days integer,
--     retention_max_items integer,
--     segmentation_id uuid,
--     scheduler_expression character varying(255) NOT NULL,
--     sunset_after timestamp(6) without time zone,
--     sunset_after_total_document_count integer,
--     title character varying(50) NOT NULL,
--     trigger_scheduled_next_at timestamp(6) without time zone,
--     visibility character varying(50) NOT NULL
-- );

ALTER TABLE t_bucket
  RENAME TO t_repository;

ALTER TABLE IF EXISTS t_repository DROP COLUMN IF EXISTS imageurl;

ALTER TABLE IF EXISTS t_repository DROP COLUMN IF EXISTS tags;

ALTER TABLE IF EXISTS t_repository DROP COLUMN IF EXISTS webhookurl;

ALTER TABLE t_repository
  RENAME COLUMN createdat to created_at;

ALTER TABLE t_repository
  RENAME COLUMN ownerid to owner_id;

ALTER TABLE t_repository
  RENAME COLUMN lastupdatedat to last_updated_at;

ALTER TABLE t_repository
  ALTER COLUMN visibility TYPE character varying(50);

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN type character varying(31) NOT NULL DEFAULT '';

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN is_archived boolean NOT NULL DEFAULT false;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN disabled_from timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN document_count_since_creation integer not null default 0;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN plugins jsonb NOT NULL DEFAULT '[]';

UPDATE t_repository SET plugins = '[{
  "id": "org_feedless_fulltext",
  "params": {
    "org_feedless_fulltext": {
      "readability": true,
      "inheritParams": true
    }
  }
}]'::jsonb where exists(
  select true from t_importer ti
  inner join t_feed_native tfn
  on tfn.id = ti.feedid
  where ti.bucketid = t_repository.id and tfn.plugins = '["fulltext"]'
);

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN for_product smallint NOT NULL DEFAULT 2;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN retention_max_age_days integer;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN retention_max_items integer;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN segmentation_id uuid;

-- scheduler cron
ALTER TABLE IF EXISTS t_repository
  ADD COLUMN scheduler_expression character varying(255);
UPDATE t_repository set scheduler_expression = '0 0 */12 * * *' WHERE scheduler_expression IS NULL;
ALTER TABLE IF EXISTS t_repository
  ALTER COLUMN scheduler_expression SET NOT NULL;

ALTER TABLE IF EXISTS t_repository DROP COLUMN IF EXISTS websiteurl;


--

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN sunset_after timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN sunset_after_total_document_count integer;

ALTER TABLE IF EXISTS t_repository
  ADD COLUMN trigger_scheduled_next_at timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_repository
  RENAME CONSTRAINT t_bucket_pkey TO t_repository_pkey;

ALTER TABLE IF EXISTS t_repository DROP CONSTRAINT IF EXISTS fkhr17e39pk9333v21m3ha23ggl;

ALTER TABLE IF EXISTS t_repository
  ADD CONSTRAINT fkcxaqh74vklfwnfrxaa0jmix4i FOREIGN KEY (owner_id)
    REFERENCES t_user (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;


--
-- Name: t_scrape_source; Type: TABLE; Schema: public; Owner: -
--

-- CREATE TABLE t_scrape_source (
--     id uuid NOT NULL,
--     created_at timestamp(6) without time zone NOT NULL,
--     additional_wait_sec integer,
--     debug_console boolean NOT NULL,
--     debug_cookies boolean NOT NULL,
--     debug_html boolean NOT NULL,
--     debug_network boolean NOT NULL,
--     debug_screenshot boolean NOT NULL,
--     emit jsonb NOT NULL,
--     erroneous boolean NOT NULL,
--     language character varying(255),
--     last_error_message character varying(255),
--     prerender boolean NOT NULL,
--     repository_id uuid NOT NULL,
--     timeout integer,
--     url character varying(255) NOT NULL,
--     viewport jsonb,
--     wait_until character varying(255)
-- );

ALTER TABLE t_feed_native RENAME TO t_scrape_source;

ALTER TABLE IF EXISTS t_repository DROP CONSTRAINT IF EXISTS fk157cu4wjd97imxmg4ns0x3x85;
ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS streamid;
ALTER TABLE IF EXISTS t_repository DROP COLUMN IF EXISTS streamid;

INSERT INTO t_repository(id, created_at, description, last_updated_at, owner_id, title, visibility, scheduler_expression)
select gen_random_uuid(), now(), '', now(), ownerid, 'orphaned feeds', 'PRIVATE', '' from (select distinct tf.ownerid as ownerid from t_scrape_source tf
                                                                                       where not exists(select TRUe from t_importer ti where ti.feedid=tf.id)) as users_with_broken_feed;


ALTER TABLE t_scrape_source RENAME CONSTRAINT t_feed_native_pkey TO t_scrape_source_pkey;
ALTER TABLE IF EXISTS t_scrape_source RENAME COLUMN createdat TO created_at;

ALTER TABLE IF EXISTS t_scrape_source RENAME COLUMN websiteurl TO url;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS description;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS domain;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS failedattemptcount;


ALTER TABLE IF EXISTS t_scrape_source RENAME COLUMN harvestsitewithprerender TO prerender;




ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS harvestintervalminutes;


ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS iconurl;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS imageurl;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS lang;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS lastchangedat;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS lastupdatedat;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS lat;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS lon;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS nextharvestat;


ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS retentionsize;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS status;


-- ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS title;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS visibility;

ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS plugins;


ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN additional_wait_sec integer;

ALTER TABLE IF EXISTS t_scrape_source ADD COLUMN debug_console boolean;
UPDATE t_scrape_source SET debug_console = false where debug_console IS NULL;
ALTER TABLE IF EXISTS t_scrape_source ALTER COLUMN debug_console SET NOT NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN debug_cookies boolean;
UPDATE t_scrape_source SET debug_cookies = false where debug_cookies IS NULL;
ALTER TABLE IF EXISTS t_scrape_source ALTER COLUMN debug_cookies SET NOT NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN debug_html boolean;
UPDATE t_scrape_source SET debug_html = false where debug_html IS NULL;
ALTER TABLE IF EXISTS t_scrape_source ALTER COLUMN debug_html SET NOT NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN debug_network boolean;
UPDATE t_scrape_source SET debug_network = false where debug_network IS NULL;
ALTER TABLE IF EXISTS t_scrape_source ALTER COLUMN debug_network SET NOT NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN debug_screenshot boolean;
UPDATE t_scrape_source SET debug_screenshot = false where debug_screenshot IS NULL;
ALTER TABLE IF EXISTS t_scrape_source ALTER COLUMN debug_screenshot SET NOT NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN emit jsonb NOT NULL DEFAULT '{}';

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN erroneous boolean;
UPDATE t_scrape_source SET erroneous = false where erroneous IS NULL;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN language character varying(255) COLLATE pg_catalog."default";

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN last_error_message character varying(255) COLLATE pg_catalog."default";


ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN repository_id uuid;


UPDATE t_scrape_source ts SET repository_id = (
  SELECT distinct ti.bucketid from t_importer ti
  WHERE ti.feedid = ts.id and ti.bucketid is not null
  union
  select id from t_repository tr where ts.ownerid=tr.owner_id and tr.title='orphaned feeds'
  limit 1
) WHERE repository_id IS NULL;


ALTER TABLE IF EXISTS t_scrape_source DROP COLUMN IF EXISTS ownerid;
ALTER TABLE t_scrape_source ALTER COLUMN repository_id SET NOT NULL;
ALTER TABLE IF EXISTS t_scrape_source
  ADD CONSTRAINT fkpdn9867c5spvfb785hkvuh71o FOREIGN KEY (repository_id)
    REFERENCES t_repository (id) MATCH SIMPLE
    ON UPDATE NO ACTION
    ON DELETE CASCADE;

ALTER TABLE t_user DROP CONSTRAINT fk_user__stream;
DROP TABLE t_stream;


ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN timeout integer;

ALTER TABLE IF EXISTS t_scrape_source
  ADD COLUMN viewport jsonb;

--
-- Name: t_segment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_segment (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    digest boolean NOT NULL,
    size integer NOT NULL,
    sort_asc boolean NOT NULL,
    sort_by character varying(255) NOT NULL,
    repository_id uuid,
    CONSTRAINT t_segment_size_check CHECK ((size >= 1))
);


--
-- Name: t_user; Type: TABLE; Schema: public; Owner: -
--

ALTER TABLE t_user
  RENAME COLUMN createdat to created_at;
ALTER TABLE t_user
  RENAME COLUMN approvedtermsat to approved_terms_at;
ALTER TABLE t_user
  RENAME COLUMN isroot to is_root;
ALTER TABLE t_user
  RENAME COLUMN locked to is_locked;
ALTER TABLE t_user
  RENAME COLUMN purgescheduledfor to purge_scheduled_for;

ALTER TABLE IF EXISTS t_user DROP COLUMN IF EXISTS name;

ALTER TABLE IF EXISTS t_user DROP COLUMN IF EXISTS notifications_stream_id;

ALTER TABLE IF EXISTS t_user DROP COLUMN IF EXISTS plugins;

ALTER TABLE IF EXISTS t_user
  ALTER COLUMN email DROP NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN githubid character varying(255);

ALTER TABLE IF EXISTS t_user
  ADD COLUMN has_validated_email boolean;
UPDATE t_user set has_validated_email = false where has_validated_email is null;
ALTER TABLE t_user ALTER has_validated_email SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_anonymous boolean;
UPDATE t_user set is_anonymous = false where is_anonymous is null;
ALTER TABLE t_user ALTER is_anonymous SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_banned boolean;
UPDATE t_user set is_banned = false where is_banned is null;
ALTER TABLE t_user ALTER is_banned SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_banned_until timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_shaddow_banned boolean;
UPDATE t_user set is_shaddow_banned = false where is_shaddow_banned is null;
ALTER TABLE t_user ALTER is_shaddow_banned SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_spamming_submissions boolean;
UPDATE t_user set is_spamming_submissions = false where is_spamming_submissions is null;
ALTER TABLE t_user ALTER is_spamming_submissions SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN is_spamming_votes boolean;
UPDATE t_user set is_spamming_votes = false where is_spamming_votes is null;
ALTER TABLE t_user ALTER is_spamming_votes SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN karma integer;
UPDATE t_user set karma = 10 where karma is null;
ALTER TABLE t_user ALTER karma SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN last_login timestamp(6) without time zone;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN product smallint;
UPDATE t_user set product = 0 where product is null;
ALTER TABLE t_user ALTER product SET NOT NULL;

ALTER TABLE IF EXISTS t_user
  ADD COLUMN validated_email_at timestamp(6) without time zone;
ALTER TABLE IF EXISTS t_user
  ADD CONSTRAINT uniqueuser UNIQUE (email, product);
ALTER TABLE IF EXISTS t_user DROP CONSTRAINT IF EXISTS fk5t89sip0n4g578g4yxaugjc1h;

--
-- Name: t_user_plan_subscription; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_user_plan_subscription (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    paiduntil timestamp(6) without time zone,
    plan_id uuid NOT NULL,
    recurring boolean NOT NULL,
    startedat timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: t_user_secret; Type: TABLE; Schema: public; Owner: -
--

-- CREATE TABLE t_user_secret (
--     id uuid NOT NULL,
--     created_at timestamp(6) without time zone NOT NULL,
--     lastusedat timestamp(6) without time zone,
--     owner_id uuid NOT NULL,
--     type character varying(50) NOT NULL,
--     validuntil timestamp(6) without time zone NOT NULL,
--     value character varying(400) NOT NULL
-- );

ALTER TABLE t_user_secret
  RENAME COLUMN createdat to created_at;



--
-- Name: t_agent t_agent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_agent
    ADD CONSTRAINT t_agent_pkey PRIMARY KEY (id);


--
-- Name: t_annotation t_annotation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation
    ADD CONSTRAINT t_annotation_pkey PRIMARY KEY (id);


--
-- Name: t_annotation_text t_annotation_text_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation_text
    ADD CONSTRAINT t_annotation_text_pkey PRIMARY KEY (id);


--
-- Name: t_annotation_vote t_annotation_vote_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation_vote
    ADD CONSTRAINT t_annotation_vote_pkey PRIMARY KEY (id);


--
-- Name: t_attachment t_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_attachment
    ADD CONSTRAINT t_attachment_pkey PRIMARY KEY (id);


--
-- Name: t_browser_action t_browser_action_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_browser_action
    ADD CONSTRAINT t_browser_action_pkey PRIMARY KEY (id);


--
-- Name: t_document t_document_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_document
    ADD CONSTRAINT t_document_pkey PRIMARY KEY (id);


--
-- Name: t_feature t_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature
    ADD CONSTRAINT t_feature_pkey PRIMARY KEY (id);


--
-- Name: t_feature_value t_feature_value_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature_value
    ADD CONSTRAINT t_feature_value_pkey PRIMARY KEY (id);


--
-- Name: t_mail_forward t_mail_forward_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_mail_forward
    ADD CONSTRAINT t_mail_forward_pkey PRIMARY KEY (id);


--
-- Name: t_notification t_notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_notification
    ADD CONSTRAINT t_notification_pkey PRIMARY KEY (id);


--
-- Name: t_otp t_otp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_otp
--     ADD CONSTRAINT t_otp_pkey PRIMARY KEY (id);


--
-- Name: t_pipeline_job t_pipeline_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_pipeline_job
    ADD CONSTRAINT t_pipeline_job_pkey PRIMARY KEY (id);


--
-- Name: t_plan t_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT t_plan_pkey PRIMARY KEY (id);


--
-- Name: t_product t_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_product
    ADD CONSTRAINT t_product_pkey PRIMARY KEY (id);


--
-- Name: t_repository t_repository_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_repository
--     ADD CONSTRAINT t_repository_pkey PRIMARY KEY (id);


--
-- Name: t_scrape_source t_scrape_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_scrape_source
--     ADD CONSTRAINT t_scrape_source_pkey PRIMARY KEY (id);


--
-- Name: t_segment t_segment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_segment
    ADD CONSTRAINT t_segment_pkey PRIMARY KEY (id);


--
-- Name: t_user t_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_user
--     ADD CONSTRAINT t_user_pkey PRIMARY KEY (id);


--
-- Name: t_user_plan_subscription t_user_plan_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT t_user_plan_subscription_pkey PRIMARY KEY (id);


--
-- Name: t_user_secret t_user_secret_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_user_secret
--     ADD CONSTRAINT t_user_secret_pkey PRIMARY KEY (id);


--
-- Name: t_feature_value uniquefeatureperplan; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature_value
    ADD CONSTRAINT uniquefeatureperplan UNIQUE (plan_id, feature_id);


--
-- Name: t_feature uniquefeatureperproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature
    ADD CONSTRAINT uniquefeatureperproduct UNIQUE (product_id, name);


--
-- Name: t_plan uniqueplannameperproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT uniqueplannameperproduct UNIQUE (name, product_id);


--
-- Name: t_product uniqueproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_product
    ADD CONSTRAINT uniqueproduct UNIQUE (name);


--
-- Name: t_user uniqueuser; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_user
--     ADD CONSTRAINT uniqueuser UNIQUE (email, product);


--
-- Name: t_repository fk1bvl5tll4isnaxnyjqq1shtld; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_repository
    ADD CONSTRAINT fk1bvl5tll4isnaxnyjqq1shtld FOREIGN KEY (segmentation_id) REFERENCES t_segment(id);


--
-- Name: t_annotation_text fk2wrv2qn79kvxyjtbe7mach2vg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation_text
    ADD CONSTRAINT fk2wrv2qn79kvxyjtbe7mach2vg FOREIGN KEY (id) REFERENCES t_annotation(id);


--
-- Name: t_user_secret fk4evcslbhw4nofy5xsl2yyqxjk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_user_secret
--     ADD CONSTRAINT fk4evcslbhw4nofy5xsl2yyqxjk FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_agent fk5emoamaua4ofq33mpdsxh4buo; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_agent
    ADD CONSTRAINT fk5emoamaua4ofq33mpdsxh4buo FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_product fk5iswe5e30x0hgt9kx5aandina; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_product
    ADD CONSTRAINT fk5iswe5e30x0hgt9kx5aandina FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_segment fk64dblt0y6ne0i6eyeilrhslx7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_segment
    ADD CONSTRAINT fk64dblt0y6ne0i6eyeilrhslx7 FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;


--
-- Name: t_attachment fk6uwautfjtj7y4pl2hw01pq24y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_attachment
    ADD CONSTRAINT fk6uwautfjtj7y4pl2hw01pq24y FOREIGN KEY (document_id) REFERENCES t_document(id) ON DELETE CASCADE;


--
-- Name: t_plan fk707naifuaavfpjkccwxw8jmxh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT fk707naifuaavfpjkccwxw8jmxh FOREIGN KEY (parent_plan_id) REFERENCES t_plan(id);


--
-- Name: t_product fk79ko141pkur3lnuo865vhffdm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_product
    ADD CONSTRAINT fk79ko141pkur3lnuo865vhffdm FOREIGN KEY (parent_product_id) REFERENCES t_product(id);


--
-- Name: t_user_plan_subscription fk8ncewprxstppim67lx6e7ysn4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT fk8ncewprxstppim67lx6e7ysn4 FOREIGN KEY (plan_id) REFERENCES t_plan(id) ON DELETE CASCADE;


--
-- Name: t_annotation_vote fk8v9srbb30wqn2nbn8t3yy5i1e; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation_vote
    ADD CONSTRAINT fk8v9srbb30wqn2nbn8t3yy5i1e FOREIGN KEY (id) REFERENCES t_annotation(id);


--
-- Name: t_user_plan_subscription fk9fe7lce4xahbf06k9eiwgy02h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT fk9fe7lce4xahbf06k9eiwgy02h FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_notification fkboru2k9q1whuculc2axpggcpa; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_notification
    ADD CONSTRAINT fkboru2k9q1whuculc2axpggcpa FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_user fkciy9noxqwbr96ybkp5fv2c2ty; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2ty FOREIGN KEY (plan_id) REFERENCES t_plan(id);


--
-- Name: t_document fkcteq09rhg8qhsxp17g1m6uyj6; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_document
    ADD CONSTRAINT fkcteq09rhg8qhsxp17g1m6uyj6 FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;


--
-- Name: t_repository fkcxaqh74vklfwnfrxaa0jmix4i; Type: FK CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_repository
--     ADD CONSTRAINT fkcxaqh74vklfwnfrxaa0jmix4i FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_plan fkdti5eeue89brhd7myq69v3klr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT fkdti5eeue89brhd7myq69v3klr FOREIGN KEY (product_id) REFERENCES t_product(id);


--
-- Name: t_annotation_text fkdu83owfk735ovcjk9mb9ngpdg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation_text
    ADD CONSTRAINT fkdu83owfk735ovcjk9mb9ngpdg FOREIGN KEY (comment_id) REFERENCES t_document(id) ON DELETE CASCADE;


--
-- Name: t_feature_value fke5debxvklvw4odvwapar7u1qg; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature_value
    ADD CONSTRAINT fke5debxvklvw4odvwapar7u1qg FOREIGN KEY (feature_id) REFERENCES t_feature(id) ON DELETE CASCADE;


--
-- Name: t_feature fkgv8wbrpqnuw2snmkml7s26csv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature
    ADD CONSTRAINT fkgv8wbrpqnuw2snmkml7s26csv FOREIGN KEY (product_id) REFERENCES t_product(id) ON DELETE CASCADE;


--
-- Name: t_agent fkhb1tqh55xp1941nsgf6j4a2b8; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_agent
    ADD CONSTRAINT fkhb1tqh55xp1941nsgf6j4a2b8 FOREIGN KEY (secret_id) REFERENCES t_user_secret(id) ON DELETE CASCADE;


--
-- Name: t_annotation fkkluvd7rpx2upem1423ynecjwn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation
    ADD CONSTRAINT fkkluvd7rpx2upem1423ynecjwn FOREIGN KEY (document_id) REFERENCES t_document(id) ON DELETE CASCADE;


--
-- Name: t_document fkkwqbkpxrw6bibp88dqy6d4ueh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_document
    ADD CONSTRAINT fkkwqbkpxrw6bibp88dqy6d4ueh FOREIGN KEY (parent_id) REFERENCES t_document(id) ON DELETE CASCADE;


--
-- Name: t_mail_forward fko85uxyf2hn04isnhnq8kd4grx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_mail_forward
    ADD CONSTRAINT fko85uxyf2hn04isnhnq8kd4grx FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;


--
-- Name: t_pipeline_job fkocn0ypekfntw4vsvr3f14r593; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_pipeline_job
    ADD CONSTRAINT fkocn0ypekfntw4vsvr3f14r593 FOREIGN KEY (document_id) REFERENCES t_document(id) ON DELETE CASCADE;


--
-- Name: t_scrape_source fkpdn9867c5spvfb785hkvuh71o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY t_scrape_source
--     ADD CONSTRAINT fkpdn9867c5spvfb785hkvuh71o FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;


--
-- Name: t_browser_action fkr018iugrf2p492v4d199aty01; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_browser_action
    ADD CONSTRAINT fkr018iugrf2p492v4d199aty01 FOREIGN KEY (source_id) REFERENCES t_scrape_source(id) ON DELETE CASCADE;


--
-- Name: t_feature_value fkr8txka4ino7s5t1eux397j2ln; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_feature_value
    ADD CONSTRAINT fkr8txka4ino7s5t1eux397j2ln FOREIGN KEY (plan_id) REFERENCES t_plan(id) ON DELETE CASCADE;


--
-- Name: t_annotation fksbaduv38d2d4ew34q5rop3ibr; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_annotation
    ADD CONSTRAINT fksbaduv38d2d4ew34q5rop3ibr FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;

