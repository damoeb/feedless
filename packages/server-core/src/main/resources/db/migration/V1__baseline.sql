--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3 (Debian 15.3-1.pgdg110+1)
-- Dumped by pg_dump version 16.2 (Ubuntu 16.2-1.pgdg22.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: scrape_action; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.scrape_action (
    action_type character varying(31) NOT NULL,
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    scrapesourceid uuid NOT NULL,
    x integer NOT NULL,
    y integer NOT NULL,
    data character varying(255) NOT NULL,
    event character varying(50) NOT NULL,
    xpath character varying(255) NOT NULL,
    header character varying(255) NOT NULL
);


--
-- Name: t_agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_agent (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    connection_id character varying(255) NOT NULL,
    openinstance boolean NOT NULL,
    owner_id uuid NOT NULL,
    secret_id uuid NOT NULL,
    version character varying(255) NOT NULL
);


--
-- Name: t_attachment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_attachment (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    data text,
    remotedata boolean NOT NULL,
    type character varying(255) NOT NULL,
    url character varying(255) NOT NULL,
    webdocumentid uuid NOT NULL
);


--
-- Name: t_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_feature (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    name character varying(50) NOT NULL,
    productid uuid NOT NULL,
    scope character varying(50)
);


--
-- Name: t_feature_value; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_feature_value (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    featureid uuid NOT NULL,
    planid uuid NOT NULL,
    value_bool boolean,
    value_int integer,
    value_type character varying(50) NOT NULL
);


--
-- Name: t_mail_forward; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_mail_forward (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    authorized boolean NOT NULL,
    authorizedat timestamp(6) without time zone,
    email character varying(255) NOT NULL,
    subscriptionid uuid NOT NULL
);


--
-- Name: t_notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_notification (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    message character varying(255) NOT NULL,
    owner_id uuid NOT NULL
);


--
-- Name: t_otp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_otp (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    password character varying(255) NOT NULL,
    userid uuid NOT NULL,
    valid_until timestamp(6) without time zone NOT NULL
);


--
-- Name: t_pipeline_job; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_pipeline_job (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    attempt integer NOT NULL,
    cool_down_until timestamp(6) without time zone,
    executorid character varying(255) NOT NULL,
    executorparams jsonb,
    sequence_id integer NOT NULL,
    terminated boolean NOT NULL,
    terminatedat timestamp(6) without time zone,
    webdocument_id uuid NOT NULL
);


--
-- Name: t_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_plan (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    availability character varying(50) NOT NULL,
    beforecosts double precision,
    currentcosts double precision NOT NULL,
    name character varying(50) NOT NULL,
    parent_plan_id uuid,
    productid uuid NOT NULL
);


--
-- Name: t_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_product (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    name character varying(50) NOT NULL,
    owner_id uuid NOT NULL,
    parent_product_id uuid
);


--
-- Name: t_scrape_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_scrape_source (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    additional_wait_sec integer,
    debug_console boolean NOT NULL,
    debug_cookies boolean NOT NULL,
    debug_html boolean NOT NULL,
    debug_network boolean NOT NULL,
    debug_screenshot boolean NOT NULL,
    emit jsonb NOT NULL,
    erroneous boolean NOT NULL,
    language character varying(255),
    last_error_message character varying(255),
    prerender boolean NOT NULL,
    subscriptionid uuid NOT NULL,
    timeout integer,
    url character varying(255) NOT NULL,
    viewport jsonb,
    wait_until character varying(255)
);


--
-- Name: t_segment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_segment (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    digest boolean NOT NULL,
    size integer NOT NULL,
    sort_asc boolean NOT NULL,
    sort_by character varying(255) NOT NULL,
    subscription_id uuid,
    CONSTRAINT t_segment_size_check CHECK ((size >= 1))
);


--
-- Name: t_source_subscription; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_source_subscription (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    is_archived boolean NOT NULL,
    description character varying(1024) NOT NULL,
    disabled_from timestamp(6) without time zone,
    document_count_since_creation integer,
    last_updated_at timestamp(6) without time zone,
    ownerid uuid NOT NULL,
    plugins jsonb NOT NULL,
    for_product smallint NOT NULL,
    retention_max_age_days integer,
    retention_max_items integer,
    scheduler_expression character varying(255) NOT NULL,
    segmentation_id uuid,
    sunset_after timestamp(6) without time zone,
    sunset_after_total_document_count integer,
    title character varying(50) NOT NULL,
    trigger_scheduled_next_at timestamp(6) without time zone,
    visibility character varying(50) NOT NULL
);


--
-- Name: t_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_user (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    approved_terms_at timestamp(6) without time zone,
    is_anonymous boolean NOT NULL,
    date_format character varying(255),
    email character varying(255),
    githubid character varying(255),
    hasapprovedterms boolean NOT NULL,
    has_validated_email boolean NOT NULL,
    is_locked boolean NOT NULL,
    plan_id uuid,
    product smallint NOT NULL,
    purge_scheduled_for timestamp(6) without time zone,
    is_root boolean NOT NULL,
    time_format character varying(255),
    usesauthsource character varying(50) NOT NULL,
    validated_email_at timestamp(6) without time zone
);


--
-- Name: t_user_plan_subscription; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_user_plan_subscription (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    paiduntil timestamp(6) without time zone,
    plan_id uuid NOT NULL,
    recurring boolean NOT NULL,
    startedat timestamp(6) without time zone,
    user_id uuid NOT NULL
);


--
-- Name: t_user_secret; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_user_secret (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    lastusedat timestamp(6) without time zone,
    owner_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL,
    value character varying(400) NOT NULL
);


--
-- Name: t_web_document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_web_document (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    content_html text,
    content_raw bytea,
    content_raw_mime character varying(50),
    content_text text,
    content_title character varying(256),
    image_url character varying(1000),
    releasedat timestamp(6) without time zone NOT NULL,
    score integer NOT NULL,
    scored_at timestamp(6) without time zone,
    starting_at timestamp(6) without time zone,
    status character varying(50) NOT NULL,
    subscriptionid uuid NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    url character varying(1000) NOT NULL
);


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (1, '1', '<< Flyway Baseline >>', 'BASELINE', '<< Flyway Baseline >>', NULL, 'postgres', '2024-04-18 16:03:10.349649', 0, true);


--
-- Data for Name: scrape_action; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_agent; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_attachment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_feature; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('2a844afb-472c-45da-978d-21da822e413a', '2024-04-18 16:29:01.667', 'scrapeSourceExpiryInDaysInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('02c1ce5a-7cc1-4164-9294-248e60aac830', '2024-04-18 16:29:01.682', 'minRefreshRateInMinutesInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('1a9e1bda-8cd2-4d08-8c4d-61fc6450454a', '2024-04-18 16:29:01.693', 'scrapeSourceRetentionMaxItemsInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('eeeccdfa-c746-4870-833c-bc4dd5c454ba', '2024-04-18 16:29:01.702', 'scrapeRequestTimeoutInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('302f728d-34c3-44ea-b183-12707cb7d0af', '2024-04-18 16:29:01.712', 'scrapeSourceMaxCountTotalInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('68db9105-4dc1-4a02-b799-4cfbe4bacb26', '2024-04-18 16:29:01.722', 'scrapeSourceMaxCountActiveInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('91a5bde0-b37c-49a9-afcc-14237293ad26', '2024-04-18 16:29:01.731', 'scrapeRequestActionMaxCountInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('c0a84477-b257-4fa1-a740-c1cc6c43c0e5', '2024-04-18 16:29:01.741', 'scrapeRequestMaxCountPerSourceInt', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('f1d4e367-874a-49d3-a49e-cf783b0f36d1', '2024-04-18 16:29:01.75', 'publicScrapeSourceBool', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('76b39d2b-3e84-4fd4-93f2-ddfbafb7ac68', '2024-04-18 16:29:01.759', 'apiBool', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('1c4f7c1e-3fa8-44cd-bd5d-0edcab597b55', '2024-04-18 16:29:01.773', 'canLogin', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('1744b21c-ac5e-47e6-b3a9-3a03e3d888a7', '2024-04-18 16:29:01.784', 'canCreateUser', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('15f238de-407d-4fe0-8af0-05190a7926c8', '2024-04-18 16:29:01.793', 'pluginsBool', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('c93394cb-7b60-446b-87e4-bcab717a309f', '2024-04-18 16:29:01.801', 'itemEmailForwardBool', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);
INSERT INTO public.t_feature (id, createdat, name, productid, scope) VALUES ('52f71712-daae-4a4c-9182-a92eceff3b36', '2024-04-18 16:29:01.809', 'itemWebhookForwardBool', '168bad6b-faf8-41cf-b9d1-2cbd67e9873b', NULL);


--
-- Data for Name: t_feature_value; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('37903fd4-76d0-474d-9393-4d04ee48af5b', '2024-04-18 16:29:01.674', '2a844afb-472c-45da-978d-21da822e413a', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 7, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('986ee6a0-1170-43ff-947f-1133424fd8f3', '2024-04-18 16:29:01.688', '02c1ce5a-7cc1-4164-9294-248e60aac830', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 120, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('a8597642-1224-48a6-9ea8-56bc640d223f', '2024-04-18 16:29:01.698', '1a9e1bda-8cd2-4d08-8c4d-61fc6450454a', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 10, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('3c872549-247d-4b21-af40-1f13285d1f85', '2024-04-18 16:29:01.707', 'eeeccdfa-c746-4870-833c-bc4dd5c454ba', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 30000, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('bccdb553-f99d-459e-98ed-7645724f0448', '2024-04-18 16:29:01.716', '302f728d-34c3-44ea-b183-12707cb7d0af', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 10000, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('9f09ed39-7dc3-43cd-ba71-45ca9e1eeb86', '2024-04-18 16:29:01.726', '68db9105-4dc1-4a02-b799-4cfbe4bacb26', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 10000, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('3a6a768b-c3d5-4e0d-a958-55d5717b4a49', '2024-04-18 16:29:01.736', '91a5bde0-b37c-49a9-afcc-14237293ad26', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 5, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('a2e9436e-aee1-430a-b990-77f4c798b2f2', '2024-04-18 16:29:01.745', 'c0a84477-b257-4fa1-a740-c1cc6c43c0e5', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', NULL, 2, 'number');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('0b6cda57-a592-495a-84cd-3a4926c4efa3', '2024-04-18 16:29:01.754', 'f1d4e367-874a-49d3-a49e-cf783b0f36d1', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', false, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('de518f31-35ca-4bb8-a0f4-1f4a42e1b4d5', '2024-04-18 16:29:01.766', '76b39d2b-3e84-4fd4-93f2-ddfbafb7ac68', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', false, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('32f1c158-1937-4275-9338-a5e69b8fa9b6', '2024-04-18 16:29:01.779', '1c4f7c1e-3fa8-44cd-bd5d-0edcab597b55', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', true, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('8f0ae4de-56dc-4df6-b464-8d642d81552f', '2024-04-18 16:29:01.789', '1744b21c-ac5e-47e6-b3a9-3a03e3d888a7', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', false, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('617c7da5-f7ca-4a8b-a39b-e8cb6cf54caa', '2024-04-18 16:29:01.797', '15f238de-407d-4fe0-8af0-05190a7926c8', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', true, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('d076a8a7-89a7-4a63-9be3-4a1c708bf2d3', '2024-04-18 16:29:01.805', 'c93394cb-7b60-446b-87e4-bcab717a309f', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', true, NULL, 'bool');
INSERT INTO public.t_feature_value (id, createdat, featureid, planid, value_bool, value_int, value_type) VALUES ('7349d91e-7b82-4972-9850-741ab0e3db1d', '2024-04-18 16:29:01.813', '52f71712-daae-4a4c-9182-a92eceff3b36', 'd74eb8b4-7245-45c4-872d-ecd6a7e9820f', true, NULL, 'bool');


--
-- Data for Name: t_mail_forward; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_notification; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_otp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_pipeline_job; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_plan; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_plan (id, createdat, availability, beforecosts, currentcosts, name, parent_plan_id, productid) VALUES ('d74eb8b4-7245-45c4-872d-ecd6a7e9820f', '2024-04-18 16:29:01.655', 'unavailable', NULL, 0, 'system', NULL, '168bad6b-faf8-41cf-b9d1-2cbd67e9873b');


--
-- Data for Name: t_product; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_product (id, createdat, name, owner_id, parent_product_id) VALUES ('168bad6b-faf8-41cf-b9d1-2cbd67e9873b', '2024-04-18 16:29:01.647', 'system', '821a79d7-6124-4158-bde1-39c49577edb2', NULL);


--
-- Data for Name: t_scrape_source; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_segment; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_source_subscription; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_user (id, createdat, approved_terms_at, is_anonymous, date_format, email, githubid, hasapprovedterms, has_validated_email, is_locked, plan_id, product, purge_scheduled_for, is_root, time_format, usesauthsource, validated_email_at) VALUES ('821a79d7-6124-4158-bde1-39c49577edb2', '2024-04-18 16:29:01.434', NULL, false, NULL, 'admin@localhost', NULL, true, false, false, NULL, 9, NULL, true, NULL, 'none', NULL);
INSERT INTO public.t_user (id, createdat, approved_terms_at, is_anonymous, date_format, email, githubid, hasapprovedterms, has_validated_email, is_locked, plan_id, product, purge_scheduled_for, is_root, time_format, usesauthsource, validated_email_at) VALUES ('a351172c-2572-44d9-85d2-87684b01d340', '2024-04-18 16:29:01.819', NULL, true, NULL, 'anonymous@localhost', NULL, true, false, false, NULL, 9, NULL, false, NULL, 'none', NULL);


--
-- Data for Name: t_user_plan_subscription; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_user_secret; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_user_secret (id, createdat, lastusedat, owner_id, type, validuntil, value) VALUES ('cf3e7885-ee6f-4d7c-a187-ea88d4b8dd8f', '2024-04-18 16:29:01.533', NULL, '821a79d7-6124-4158-bde1-39c49577edb2', 'SecretKey', '2025-04-09 16:29:01.533', 'QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK');


--
-- Data for Name: t_web_document; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: scrape_action scrape_action_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.scrape_action
    ADD CONSTRAINT scrape_action_pkey PRIMARY KEY (id);


--
-- Name: t_agent t_agent_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_agent
    ADD CONSTRAINT t_agent_pkey PRIMARY KEY (id);


--
-- Name: t_attachment t_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_attachment
    ADD CONSTRAINT t_attachment_pkey PRIMARY KEY (id);


--
-- Name: t_feature t_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT t_feature_pkey PRIMARY KEY (id);


--
-- Name: t_feature_value t_feature_value_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature_value
    ADD CONSTRAINT t_feature_value_pkey PRIMARY KEY (id);


--
-- Name: t_mail_forward t_mail_forward_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_mail_forward
    ADD CONSTRAINT t_mail_forward_pkey PRIMARY KEY (id);


--
-- Name: t_notification t_notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_notification
    ADD CONSTRAINT t_notification_pkey PRIMARY KEY (id);


--
-- Name: t_otp t_otp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_otp
    ADD CONSTRAINT t_otp_pkey PRIMARY KEY (id);


--
-- Name: t_pipeline_job t_pipeline_job_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_pipeline_job
    ADD CONSTRAINT t_pipeline_job_pkey PRIMARY KEY (id);


--
-- Name: t_plan t_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT t_plan_pkey PRIMARY KEY (id);


--
-- Name: t_product t_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_product
    ADD CONSTRAINT t_product_pkey PRIMARY KEY (id);


--
-- Name: t_scrape_source t_scrape_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scrape_source
    ADD CONSTRAINT t_scrape_source_pkey PRIMARY KEY (id);


--
-- Name: t_segment t_segment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_segment
    ADD CONSTRAINT t_segment_pkey PRIMARY KEY (id);


--
-- Name: t_source_subscription t_source_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_source_subscription
    ADD CONSTRAINT t_source_subscription_pkey PRIMARY KEY (id);


--
-- Name: t_user t_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT t_user_pkey PRIMARY KEY (id);


--
-- Name: t_user_plan_subscription t_user_plan_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_plan_subscription
    ADD CONSTRAINT t_user_plan_subscription_pkey PRIMARY KEY (id);


--
-- Name: t_user_secret t_user_secret_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_secret
    ADD CONSTRAINT t_user_secret_pkey PRIMARY KEY (id);


--
-- Name: t_web_document t_web_document_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_web_document
    ADD CONSTRAINT t_web_document_pkey PRIMARY KEY (id);


--
-- Name: t_feature_value uniquefeatureperplan; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature_value
    ADD CONSTRAINT uniquefeatureperplan UNIQUE (planid, featureid);


--
-- Name: t_feature uniquefeatureperproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT uniquefeatureperproduct UNIQUE (productid, name);


--
-- Name: t_plan uniqueplannameperproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT uniqueplannameperproduct UNIQUE (name, productid);


--
-- Name: t_product uniqueproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_product
    ADD CONSTRAINT uniqueproduct UNIQUE (name);


--
-- Name: t_user uniqueuser; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT uniqueuser UNIQUE (email, product);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_attachment_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attachment_url ON public.t_attachment USING btree (url);


--
-- Name: idx_web_document_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_web_document_url ON public.t_web_document USING btree (url);


--
-- Name: scrape_action fk16mkc0hxrf9emo54amddd9f5l; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.scrape_action
    ADD CONSTRAINT fk16mkc0hxrf9emo54amddd9f5l FOREIGN KEY (scrapesourceid) REFERENCES public.t_scrape_source(id) ON DELETE CASCADE;


--
-- Name: t_product fk5iswe5e30x0hgt9kx5aandina; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_product
    ADD CONSTRAINT fk5iswe5e30x0hgt9kx5aandina FOREIGN KEY (owner_id) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_plan fk707naifuaavfpjkccwxw8jmxh; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT fk707naifuaavfpjkccwxw8jmxh FOREIGN KEY (parent_plan_id) REFERENCES public.t_plan(id);


--
-- Name: t_product fk79ko141pkur3lnuo865vhffdm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_product
    ADD CONSTRAINT fk79ko141pkur3lnuo865vhffdm FOREIGN KEY (parent_product_id) REFERENCES public.t_product(id);


--
-- Name: t_feature_value fk8ic0ufalqxbiywmwv2y5hffa4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature_value
    ADD CONSTRAINT fk8ic0ufalqxbiywmwv2y5hffa4 FOREIGN KEY (planid) REFERENCES public.t_plan(id) ON DELETE CASCADE;


--
-- Name: t_agent fk_agent__user_secrets; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_agent
    ADD CONSTRAINT fk_agent__user_secrets FOREIGN KEY (secret_id) REFERENCES public.t_user_secret(id) ON DELETE CASCADE;


--
-- Name: t_attachment fk_attachment__web_document; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_attachment
    ADD CONSTRAINT fk_attachment__web_document FOREIGN KEY (webdocumentid) REFERENCES public.t_web_document(id) ON DELETE CASCADE;


--
-- Name: t_pipeline_job fk_attachment__web_document; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_pipeline_job
    ADD CONSTRAINT fk_attachment__web_document FOREIGN KEY (webdocument_id) REFERENCES public.t_web_document(id) ON DELETE CASCADE;


--
-- Name: t_web_document fk_item__subscritpion; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_web_document
    ADD CONSTRAINT fk_item__subscritpion FOREIGN KEY (subscriptionid) REFERENCES public.t_source_subscription(id) ON DELETE CASCADE;


--
-- Name: t_source_subscription fk_native_feed__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_source_subscription
    ADD CONSTRAINT fk_native_feed__user FOREIGN KEY (ownerid) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_otp fk_otp__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_otp
    ADD CONSTRAINT fk_otp__user FOREIGN KEY (userid) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_source_subscription fk_source_subscription__segmentation; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_source_subscription
    ADD CONSTRAINT fk_source_subscription__segmentation FOREIGN KEY (segmentation_id) REFERENCES public.t_segment(id);


--
-- Name: t_mail_forward fk_subscription__mail_forward; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_mail_forward
    ADD CONSTRAINT fk_subscription__mail_forward FOREIGN KEY (subscriptionid) REFERENCES public.t_source_subscription(id) ON DELETE CASCADE;


--
-- Name: t_user_plan_subscription fk_ups__plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_plan_subscription
    ADD CONSTRAINT fk_ups__plan FOREIGN KEY (plan_id) REFERENCES public.t_plan(id) ON DELETE CASCADE;


--
-- Name: t_user_plan_subscription fk_ups__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_plan_subscription
    ADD CONSTRAINT fk_ups__user FOREIGN KEY (user_id) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_user fk_user__plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT fk_user__plan FOREIGN KEY (plan_id) REFERENCES public.t_plan(id);


--
-- Name: t_scrape_source fk_user__stream; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scrape_source
    ADD CONSTRAINT fk_user__stream FOREIGN KEY (subscriptionid) REFERENCES public.t_source_subscription(id) ON DELETE CASCADE;


--
-- Name: t_agent fk_user_secrets__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_agent
    ADD CONSTRAINT fk_user_secrets__user FOREIGN KEY (owner_id) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_notification fk_user_secrets__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_notification
    ADD CONSTRAINT fk_user_secrets__user FOREIGN KEY (owner_id) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_user_secret fk_user_secrets__user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_secret
    ADD CONSTRAINT fk_user_secrets__user FOREIGN KEY (owner_id) REFERENCES public.t_user(id) ON DELETE CASCADE;


--
-- Name: t_segment fkchll8yqiakf4mnr045hlf2oa7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_segment
    ADD CONSTRAINT fkchll8yqiakf4mnr045hlf2oa7 FOREIGN KEY (subscription_id) REFERENCES public.t_source_subscription(id) ON DELETE CASCADE;


--
-- Name: t_feature fkfytslac1jgwvxkermpcu4g8g7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT fkfytslac1jgwvxkermpcu4g8g7 FOREIGN KEY (productid) REFERENCES public.t_product(id) ON DELETE CASCADE;


--
-- Name: t_plan fklfm2yfqljeiwlewymdr1xwkd0; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT fklfm2yfqljeiwlewymdr1xwkd0 FOREIGN KEY (productid) REFERENCES public.t_product(id);


--
-- Name: t_feature_value fkrcahk72xesj99wd68vc30noq5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature_value
    ADD CONSTRAINT fkrcahk72xesj99wd68vc30noq5 FOREIGN KEY (featureid) REFERENCES public.t_feature(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

