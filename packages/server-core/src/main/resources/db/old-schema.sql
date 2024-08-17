--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3 (Debian 15.3-1.pgdg110+1)
-- Dumped by pg_dump version 16.3 (Ubuntu 16.3-1.pgdg22.04+1)

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
-- Name: map_plan_to_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.map_plan_to_feature (
    plan_id uuid NOT NULL,
    feature_id uuid NOT NULL
);


--
-- Name: t_article; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_article (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    importerid uuid,
    ownerid uuid NOT NULL,
    releasedat timestamp(6) without time zone,
    status character varying(255) NOT NULL,
    streamid uuid NOT NULL,
    type character varying(255) NOT NULL,
    webdocumentid uuid NOT NULL
);


--
-- Name: t_bucket; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_bucket (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    description character varying(1024) NOT NULL,
    imageurl character varying(200),
    lastupdatedat timestamp(6) without time zone,
    ownerid uuid NOT NULL,
    streamid uuid NOT NULL,
    tags text[],
    title character varying(255) NOT NULL,
    visibility character varying(255) NOT NULL,
    webhookurl character varying(200),
    websiteurl character varying(200)
);


--
-- Name: t_feature; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_feature (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    plan_id uuid,
    state character varying(255) NOT NULL,
    valueboolean boolean,
    valueint integer,
    valuetype character varying(255) NOT NULL
);


--
-- Name: t_feed_generic; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_feed_generic (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    feedspecification jsonb NOT NULL,
    websiteurl character varying(255) NOT NULL,
    nativefeed_id uuid
);


--
-- Name: t_feed_native; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_feed_native (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    description character varying(1024),
    domain character varying(255),
    failedattemptcount integer NOT NULL,
    feedurl character varying(1000) NOT NULL,
    harvestintervalminutes integer,
    harvestsitewithprerender boolean NOT NULL,
    iconurl character varying(255),
    imageurl character varying(255),
    lang character varying(255),
    lastchangedat timestamp(6) without time zone,
    lastupdatedat timestamp(6) without time zone,
    lat bigint,
    lon bigint,
    nextharvestat timestamp(6) without time zone,
    ownerid uuid NOT NULL,
    retentionsize integer,
    status character varying(255) NOT NULL,
    streamid uuid NOT NULL,
    title character varying(256) NOT NULL,
    visibility character varying(255) NOT NULL,
    websiteurl character varying(255),
    generic_feed_id uuid,
    plugins jsonb DEFAULT '[]'::jsonb NOT NULL
);


--
-- Name: t_hyperlink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_hyperlink (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    fromid uuid NOT NULL,
    hypertext character varying(256) NOT NULL,
    relevance double precision NOT NULL,
    toid uuid NOT NULL
);


--
-- Name: t_importer; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_importer (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    is_auto_release boolean,
    bucketid uuid,
    segment_digest boolean NOT NULL,
    feedid uuid NOT NULL,
    filter character varying(255),
    lastupdatedat timestamp(6) without time zone,
    lookaheadmin integer,
    ownerid uuid NOT NULL,
    segment_size integer,
    segmentsortasc boolean NOT NULL,
    segmentsortfield character varying(255),
    title character varying(255),
    triggerrefreshon character varying(255) NOT NULL,
    triggerscheduleexpression character varying(255),
    triggerscheduledlastat timestamp(6) without time zone,
    triggerschedulednextat timestamp(6) without time zone,
    plugins jsonb DEFAULT '[]'::jsonb NOT NULL
);


--
-- Name: t_otp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_otp (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    password character varying(255) NOT NULL,
    userid uuid NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL
);


--
-- Name: t_plan; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_plan (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    availability character varying(255) NOT NULL,
    costs double precision NOT NULL,
    name character varying(255) NOT NULL,
    is_primary boolean NOT NULL
);


--
-- Name: t_stream; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_stream (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL
);


--
-- Name: t_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_user (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    approvedtermsat timestamp(6) without time zone,
    date_format character varying(255),
    email character varying(255) NOT NULL,
    hasapprovedterms boolean NOT NULL,
    isroot boolean NOT NULL,
    locked boolean NOT NULL,
    name character varying(255) NOT NULL,
    notifications_stream_id uuid NOT NULL,
    plan_id uuid,
    time_format character varying(255),
    purgescheduledfor timestamp(6) without time zone,
    plugins jsonb DEFAULT '{}'::jsonb NOT NULL
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
    type character varying(255) NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL,
    value character varying(400) NOT NULL
);


--
-- Name: t_web_document; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_web_document (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    aliasurl character varying(1000),
    attachments jsonb,
    contentraw text,
    contentrawmime character varying(255),
    contenttext text,
    contenttitle character varying(256),
    description text,
    finalized boolean NOT NULL,
    hasaudio boolean NOT NULL,
    hasfulltext boolean NOT NULL,
    hasvideo boolean NOT NULL,
    imageurl character varying(1000),
    pending_plugins jsonb NOT NULL,
    pluginscooldownuntil timestamp(6) without time zone,
    releasedat timestamp(6) without time zone NOT NULL,
    score integer NOT NULL,
    startingat timestamp(6) without time zone,
    title character varying(256),
    updatedat timestamp(6) without time zone NOT NULL,
    url character varying(1000) NOT NULL,
    executed_plugins jsonb DEFAULT '[]'::jsonb NOT NULL
);


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (1, '1', 'baseline', 'SQL', 'V1__baseline.sql', -404334859, 'postgres', '2024-08-16 14:56:32.547455', 146, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (2, '2', 'patch user', 'SQL', 'V2__patch_user.sql', -1788641070, 'postgres', '2024-08-16 14:56:32.769589', 2, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (3, '3', 'generalize plugins', 'SQL', 'V3__generalize_plugins.sql', 1985351672, 'postgres', '2024-08-16 14:56:32.782212', 6, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (4, '4', 'add plugin to user', 'SQL', 'V4__add_plugin_to_user.sql', 1319472064, 'postgres', '2024-08-16 14:56:32.796901', 6, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (5, '5', 'plugins in webdocument', 'SQL', 'V5__plugins_in_webdocument.sql', -2131848437, 'postgres', '2024-08-16 14:56:32.824888', 4, true);
INSERT INTO public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES (6, '6', 'alter feed native', 'SQL', 'V6__alter_feed_native.sql', -958800633, 'postgres', '2024-08-16 14:56:32.836965', 2, true);


--
-- Data for Name: map_plan_to_feature; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_article; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('647993b8-f3b2-446c-ad23-ac1ed1f044da', '2024-08-16 15:03:58.982', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '419c7f6a-3643-40c6-91e6-6181c85f7d33');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('78df4ce5-22ec-4573-a63e-ad1699da949f', '2024-08-16 15:03:58.985', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'd128970c-57fb-4969-adfb-1b04472ba1a6');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('cd1c2443-eede-4252-a340-2e0e1b153b63', '2024-08-16 15:03:58.986', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'cdc2fb5b-5cfa-4b82-b491-f1dd7dba845d');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('e4fa628b-4d9e-4c11-bcc0-e7801f4cebd2', '2024-08-16 15:03:58.987', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'e577f716-d844-491a-ab70-429da726c8c2');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('4a6c7495-00f8-4f4d-8fbc-3fa6b0b9947e', '2024-08-16 15:03:58.988', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '9adb479a-b817-4bb7-903d-d99d30ab699c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('cc0133ac-81b3-4489-b825-982c66439fad', '2024-08-16 15:03:58.989', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '3da65efc-e13d-4c70-9382-9a441b5c64b7');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('707b0114-1b11-4962-b06c-7f6c1a4b5013', '2024-08-16 15:03:58.99', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'ffc9604c-50cc-4453-872a-4a5007f213a5');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('6669ffd7-3779-476a-ba4f-bd23a77351b1', '2024-08-16 15:03:58.99', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '2fe0d930-7545-4faf-8ca8-38d111cb5ca0');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('eaacb326-71f2-400c-804e-cd3bbd1f9689', '2024-08-16 15:03:58.991', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'ff154c10-5db9-4774-b40c-f7e5988d8d97');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('e5124929-de3e-4e85-a3ed-521548cdf6ed', '2024-08-16 15:03:58.992', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'd8c62bdf-b7bc-44c0-89a4-0f264c80d09c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('69a1dc84-662b-481c-a86c-7e8e77e733ea', '2024-08-16 15:03:58.993', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'b2baa47b-e932-46c2-ad5b-23493cfbbec9');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('7faff3ca-c6df-4266-a2fa-76967a676058', '2024-08-16 15:03:58.993', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'c7fe9326-3af6-4848-98cd-c4722d9778ee');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3db8a877-bb3c-4dfb-9306-ea81808763c5', '2024-08-16 15:03:58.994', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', 'e3b539c4-ea81-4f72-91ec-548f626cd82b');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('814b43e1-2dd2-44a6-a465-9b72177d9838', '2024-08-16 15:03:58.995', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '70f1cd22-1ff4-4263-9f7d-83d8c28d6df8');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('311c6673-24d2-4425-8fb7-c7e82ba9ec65', '2024-08-16 15:03:58.996', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'feed', '32ca1717-3f74-471d-8347-76884cbdfd5b');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('730a1603-307d-4ec0-ade2-e008b82fae17', '2024-08-16 15:03:59.349', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '419c7f6a-3643-40c6-91e6-6181c85f7d33');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('5b44760e-332d-4004-9048-d1e2647016ee', '2024-08-16 15:03:59.353', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'd128970c-57fb-4969-adfb-1b04472ba1a6');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('821d7ae5-1e9e-4415-b012-3e4a481cbc23', '2024-08-16 15:03:59.355', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'cdc2fb5b-5cfa-4b82-b491-f1dd7dba845d');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('5e679c8f-7b21-4618-977a-cafe11e80a96', '2024-08-16 15:03:59.357', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'e577f716-d844-491a-ab70-429da726c8c2');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('d429e0c5-616c-475b-b08a-0d78edfe2bcf', '2024-08-16 15:03:59.359', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '9adb479a-b817-4bb7-903d-d99d30ab699c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c9997c6d-286d-4863-b464-c8aaa04346b5', '2024-08-16 15:03:59.36', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '3da65efc-e13d-4c70-9382-9a441b5c64b7');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('efdd09e5-8b10-4d3a-9ace-437aef91f72d', '2024-08-16 15:03:59.362', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'ffc9604c-50cc-4453-872a-4a5007f213a5');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('f946927c-3b72-4ab9-ab9a-ee27e47a83a1', '2024-08-16 15:03:59.364', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '2fe0d930-7545-4faf-8ca8-38d111cb5ca0');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('1535809f-4ee2-4e7f-967f-208de89a9ff3', '2024-08-16 15:03:59.366', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'ff154c10-5db9-4774-b40c-f7e5988d8d97');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('a56592f6-874c-4209-965b-cf99bb53e862', '2024-08-16 15:03:59.368', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'd8c62bdf-b7bc-44c0-89a4-0f264c80d09c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('f5671b04-c876-4769-8ef9-a14cf931124f', '2024-08-16 15:03:59.369', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'b2baa47b-e932-46c2-ad5b-23493cfbbec9');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3f38775e-716a-4480-95b6-4a29b88a23e1', '2024-08-16 15:03:59.371', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'c7fe9326-3af6-4848-98cd-c4722d9778ee');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('d8f2c1b8-acb4-43d3-b103-c12806673c03', '2024-08-16 15:03:59.372', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', 'e3b539c4-ea81-4f72-91ec-548f626cd82b');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('ca51fbbc-1f14-4cb0-8409-fb0ab2123294', '2024-08-16 15:03:59.373', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '70f1cd22-1ff4-4263-9f7d-83d8c28d6df8');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('cb9b17f2-e5a8-4fa2-ac05-5f0c4b932b70', '2024-08-16 15:03:59.374', 'e14b8591-ad79-412e-866b-124208dc5d6a', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:58', 'released', '3c7f5551-7827-4d53-ace6-0d48761262a8', 'feed', '32ca1717-3f74-471d-8347-76884cbdfd5b');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('b95a31a9-f65a-4cde-b9ce-7d1cd971fe05', '2024-08-16 15:05:12.734', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 14:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '3324f158-4815-44b2-866f-3124c810ffc2');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('5b752d96-fe8c-41d4-bcc0-8cf685b97833', '2024-08-16 15:05:12.734', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 13:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '1209d145-da96-4fa8-8a83-85b79d2f60cc');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c492d490-55b9-4eb4-990d-72aee988a3e3', '2024-08-16 15:05:12.735', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 12:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '29e9e534-f5fe-4d2c-84e1-466dc5033a40');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('675baca9-94ce-41b8-90b3-d9989101737b', '2024-08-16 15:05:12.736', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 11:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '0cd6bbad-b71d-411d-beec-a97fc7de9784');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c00fdbe3-5669-4d4d-80f2-fcef5e9b2231', '2024-08-16 15:05:12.736', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 10:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'e755abc8-0dbc-4a92-9d58-33df867d66f0');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('4a80e72b-a42a-4bdd-8e10-4f67bc44beb9', '2024-08-16 15:05:12.737', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 09:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'f8fe768b-76e6-49e4-b20a-026dfef8f4d8');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('205f7942-6f36-4f93-b9fd-fef8a11da9c3', '2024-08-16 15:05:12.738', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 08:30:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'e7fdb578-ea36-4bd8-b047-e5f8e4050631');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('648bb856-70aa-4244-996d-1c0e45e869b7', '2024-08-16 15:05:12.738', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 08:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '17205a16-ed97-42da-a1c2-b52403a9441a');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c536c2c5-3168-4493-be22-c5886f9044ee', '2024-08-16 15:05:12.739', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 07:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '5b0fcd1c-654a-4bc3-86c6-566676290edb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('33db82e6-a5e8-4a2b-84a6-cecc05517306', '2024-08-16 15:05:12.739', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 00:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '5b00baec-25a0-42da-8751-ffbde4086743');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('d19d3d0a-727f-44cc-afff-f361bad31512', '2024-08-16 15:05:12.74', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 00:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '418585a8-9237-45d1-b1fb-a5fe363d97ac');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('b81ff875-0be6-4c5c-8aa1-8e01af393cc3', '2024-08-16 15:05:12.74', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 16:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '44f6005a-3a78-4a7b-a192-a483dd587b99');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('b2bc6433-d7d2-4d70-8265-0bd08635cd3f', '2024-08-16 15:05:12.741', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 15:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '22f203b1-c3c5-429c-bc0e-b89476fe74eb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('a71abcb8-6516-40e2-b396-c61e79de764b', '2024-08-16 15:05:12.742', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 14:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'e8d0baf3-2b0f-4e90-a48a-7994a08788dc');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('5eeefa4e-8a04-4465-b4f7-36ba16b226f3', '2024-08-16 15:05:12.742', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 12:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '4afb0cec-b194-468c-a089-1b263f85fdbb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('f271553c-5416-4a89-ae58-1bda1434a4bd', '2024-08-16 15:05:12.743', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 11:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '07ca943d-e9bf-4cfa-988e-6e7a9a6504b3');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('d3d52929-3bea-49e9-800f-ef6aeb3e3244', '2024-08-16 15:05:12.744', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 10:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '40003e90-ac2d-4d47-b03e-38b21755d883');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('f9b6c1c9-ef74-4934-81c7-2cb825bb670c', '2024-08-16 15:05:12.744', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 10:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '8ab186e7-9400-4bb0-8b88-b44f9af6c807');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('a67b5ca1-79c2-4a39-909c-8ceb9657d70f', '2024-08-16 15:05:12.744', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 09:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'a4eb7098-df36-4cf8-a0c9-0e1b0f739c5c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('bda14410-a4e5-427e-a7b6-2a7305d40c37', '2024-08-16 15:05:12.745', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 08:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'a97dc0ea-ec43-483b-86da-7f20fef62ce3');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('ad9cf988-a6dc-49e1-9bae-c77a63580461', '2024-08-16 15:05:12.746', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 07:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'bbf9f3af-68b7-448e-8b8c-0a345bdbb6f5');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('304e7478-dddf-4df2-98c9-d2893cbb13a8', '2024-08-16 15:05:12.746', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 00:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '6483a3ff-226c-49a4-aedb-70ec5dd1909d');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('9b6baef3-e176-47c0-9fcc-51381abc122d', '2024-08-16 15:05:12.747', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 00:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'f6337f71-357a-47c5-8d2f-3edf38066350');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('ac2a0b1d-0860-4fe3-88e1-590277d189c3', '2024-08-16 15:05:12.747', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 16:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'e0d3221b-12fe-4d37-9a40-ca13f589d54a');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('bd87905a-2bc5-4d8f-9680-aaa77065ba4f', '2024-08-16 15:05:12.748', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 15:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'affa06b4-79c6-4e7c-8a0a-8a0121be9c19');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('9dd224b8-666e-4b06-b51f-9319dffe2509', '2024-08-16 15:05:12.749', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 14:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '3e924cfb-4ef4-428a-be7d-769fdecb2b94');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('1eda4ed7-bbee-4ad8-968d-3fab07c8d624', '2024-08-16 15:05:12.751', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 12:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '0883d555-99aa-402a-b7d2-9f5d1d282937');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('d1140b4b-5e5c-43eb-8d2f-63528e73c146', '2024-08-16 15:05:12.751', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 11:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', 'd1c4a1a8-c409-4432-98ba-acfc89c40f4e');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('b81ac9c3-0278-4796-8e71-6b2e820537d9', '2024-08-16 15:05:12.752', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 10:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '64de34ae-a726-40d9-9fb5-bb65198f1441');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('4b7edc19-1539-4a33-836f-928a96b93937', '2024-08-16 15:05:12.753', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 09:00:00', 'released', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'feed', '910db22b-e52f-489a-af2d-699cd656659b');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('87bf33e5-a700-4f89-9282-6b1ae599d6a5', '2024-08-16 15:05:12.778', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 14:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '3324f158-4815-44b2-866f-3124c810ffc2');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('b9e24a28-0e76-4724-bb99-90bccb2cc113', '2024-08-16 15:05:12.783', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 13:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '1209d145-da96-4fa8-8a83-85b79d2f60cc');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('a8642616-767b-4d5a-86a3-ee246b6d832f', '2024-08-16 15:05:12.789', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 12:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '29e9e534-f5fe-4d2c-84e1-466dc5033a40');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('9c385c6a-908f-459b-ba64-11134e7b49f1', '2024-08-16 15:05:12.791', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 11:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '0cd6bbad-b71d-411d-beec-a97fc7de9784');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('4ab2dccd-c70a-48a5-b3c1-5091591ff82f', '2024-08-16 15:05:12.793', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 10:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'e755abc8-0dbc-4a92-9d58-33df867d66f0');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('1744295e-78a5-4e92-843f-907d888944f7', '2024-08-16 15:05:12.795', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 09:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'f8fe768b-76e6-49e4-b20a-026dfef8f4d8');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('1d76de82-6a44-453c-8293-c4e9e65d5b4b', '2024-08-16 15:05:12.797', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 08:30:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'e7fdb578-ea36-4bd8-b047-e5f8e4050631');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('9fd12685-3c46-4a49-bc40-ff70579f0d10', '2024-08-16 15:05:12.799', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 08:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '17205a16-ed97-42da-a1c2-b52403a9441a');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('f44b9be9-eb1e-4479-ba2e-254995e16ebb', '2024-08-16 15:05:12.801', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 07:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '5b0fcd1c-654a-4bc3-86c6-566676290edb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('a00dbe72-ec1e-4903-9b6f-b32a90416134', '2024-08-16 15:05:12.802', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 00:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '5b00baec-25a0-42da-8751-ffbde4086743');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c1764f2a-73b8-4b27-9c0a-1c73b0101fdc', '2024-08-16 15:05:12.804', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 00:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '418585a8-9237-45d1-b1fb-a5fe363d97ac');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('ae61198d-e393-423b-9cae-042d0b7f1ef9', '2024-08-16 15:05:12.805', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 16:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '44f6005a-3a78-4a7b-a192-a483dd587b99');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3b5a3d2b-317c-4f61-a2eb-e0e88292bbbb', '2024-08-16 15:05:12.806', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 15:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '22f203b1-c3c5-429c-bc0e-b89476fe74eb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('8f63317f-e206-43d3-b4bf-108db9ca3de5', '2024-08-16 15:05:12.808', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 14:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'e8d0baf3-2b0f-4e90-a48a-7994a08788dc');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('41e33813-0d92-4ffd-bf4c-e147978b87a8', '2024-08-16 15:05:12.809', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 12:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '4afb0cec-b194-468c-a089-1b263f85fdbb');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('90f9c779-f469-4ded-812a-ddd75be822c5', '2024-08-16 15:05:12.811', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 11:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '07ca943d-e9bf-4cfa-988e-6e7a9a6504b3');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('9a23cd84-3007-4c2b-8269-813ebd53650a', '2024-08-16 15:05:12.813', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 10:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '40003e90-ac2d-4d47-b03e-38b21755d883');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('70dc7ae1-e46b-434b-b094-6976abcd6258', '2024-08-16 15:05:12.814', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 10:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '8ab186e7-9400-4bb0-8b88-b44f9af6c807');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3e72ffa1-e9a4-49b3-ac8e-b6713063559d', '2024-08-16 15:05:12.816', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 09:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'a4eb7098-df36-4cf8-a0c9-0e1b0f739c5c');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('5c2a4d41-6993-4bdd-8039-29aed0b0b77f', '2024-08-16 15:05:12.817', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 08:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'a97dc0ea-ec43-483b-86da-7f20fef62ce3');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('413578e6-c1c8-481a-9df6-7ea2dd94bb06', '2024-08-16 15:05:12.818', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 07:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'bbf9f3af-68b7-448e-8b8c-0a345bdbb6f5');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3c470e83-515a-43ec-ba4d-f4026821e0ba', '2024-08-16 15:05:12.82', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 00:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '6483a3ff-226c-49a4-aedb-70ec5dd1909d');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('3a11426c-c482-47c5-9870-c294cf6e02ab', '2024-08-16 15:05:12.821', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-15 00:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'f6337f71-357a-47c5-8d2f-3edf38066350');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('1653d468-b409-44d2-982c-32d4315f2801', '2024-08-16 15:05:12.823', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 16:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'e0d3221b-12fe-4d37-9a40-ca13f589d54a');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('c4842fb6-9ec1-4a31-92de-2ebb3a715682', '2024-08-16 15:05:12.824', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 15:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'affa06b4-79c6-4e7c-8a0a-8a0121be9c19');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('0ee7a22d-1e34-4901-8adb-fb5ac195b5d9', '2024-08-16 15:05:12.825', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 14:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '3e924cfb-4ef4-428a-be7d-769fdecb2b94');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('76aac481-73ba-47e4-a412-2edd6a5774d1', '2024-08-16 15:05:12.827', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 12:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '0883d555-99aa-402a-b7d2-9f5d1d282937');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('dd5c6485-f286-46af-8fa9-b0821f0fe8b5', '2024-08-16 15:05:12.829', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 11:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', 'd1c4a1a8-c409-4432-98ba-acfc89c40f4e');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('027ad40d-2e3b-4ee9-9511-238273a914e3', '2024-08-16 15:05:12.831', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 10:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '64de34ae-a726-40d9-9fb5-bb65198f1441');
INSERT INTO public.t_article (id, createdat, importerid, ownerid, releasedat, status, streamid, type, webdocumentid) VALUES ('afc8d6b3-9b9b-44dd-b4da-756e0d281715', '2024-08-16 15:05:12.832', 'aeb218bf-cec5-4016-905c-1ca4210c0c65', '2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-14 09:00:00', 'released', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', 'feed', '910db22b-e52f-489a-af2d-699cd656659b');


--
-- Data for Name: t_bucket; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_bucket (id, createdat, description, imageurl, lastupdatedat, ownerid, streamid, tags, title, visibility, webhookurl, websiteurl) VALUES ('5a853d8f-bc1d-47b8-aec2-8a33de86dd7d', '2024-08-16 15:03:57.674', '✓ kritisch, ✓ meinungsstark, ✓ informativ! Telepolis hinterfragt die digitale Gesellschaft und ihre Entwicklung in Politik, Wirtschaft &amp; Medien.', NULL, NULL, '2621625f-2de8-4407-912d-04de63fb32a6', '3c7f5551-7827-4d53-ace6-0d48761262a8', '{}', 'Telepolis generated', 'isPublic', NULL, 'https://www.telepolis.de/');
INSERT INTO public.t_bucket (id, createdat, description, imageurl, lastupdatedat, ownerid, streamid, tags, title, visibility, webhookurl, websiteurl) VALUES ('0584f447-9a99-437c-a656-a2bda991193b', '2024-08-16 15:05:11.395', '✓ kritisch, ✓ meinungsstark, ✓ informativ! Telepolis hinterfragt die digitale Gesellschaft und ihre Entwicklung in Politik, Wirtschaft &amp; Medien.', NULL, NULL, '2621625f-2de8-4407-912d-04de63fb32a6', 'eb4ef3f5-159d-4c8f-9aa3-da3245416587', '{}', 'Telepolis native', 'isPublic', NULL, 'https://www.telepolis.de/');


--
-- Data for Name: t_feature; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('a961a8e7-919c-4484-af6e-452bb6841d73', '2023-05-09 20:21:12.009', 'rateLimit', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', NULL, 40, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('91b5a955-e2f5-417c-a7e2-eda1e85bf1ab', '2023-05-09 20:21:12.01', 'rateLimit', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', NULL, 120, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('d0c5f08c-3ac5-4466-910c-e9353d8b05fb', '2023-05-09 20:21:12.01', 'feedsMaxRefreshRate', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', NULL, 120, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('33822272-65cc-4472-8665-c3078f961d2f', '2023-05-09 20:21:12.01', 'feedsMaxRefreshRate', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', NULL, 10, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('895d8106-79eb-495a-afce-034987717e82', '2023-05-09 20:21:12.01', 'bucketsMaxCount', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', NULL, 3, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('d1b87a8e-8222-4023-a556-53011feeff01', '2023-05-09 20:21:12.01', 'bucketsMaxCount', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', NULL, 100, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('70ae5d76-060e-4519-b57e-24700ee3494b', '2023-05-09 20:21:12.01', 'feedsMaxCount', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', NULL, 30, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('04bfd145-806e-4854-9534-e414f8fe3bc2', '2023-05-09 20:21:12.01', 'feedsMaxCount', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', NULL, 1000, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('bdca2c7b-6c87-4fbc-b7b8-10541802f7ef', '2023-05-09 20:21:12.01', 'itemsRetention', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', NULL, 400, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('c9948d6c-ca4b-4689-a351-96fc8c2aa6a4', '2023-05-09 20:21:12.01', 'itemsRetention', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', NULL, 10000, 'number');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('c7f3012f-b88c-4f19-a828-e7de33f58e75', '2023-05-09 20:21:12.01', 'bucketsAccessOther', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('2ec2098c-6720-49b4-8e68-aaceb90aba86', '2023-05-09 20:21:12.011', 'bucketsAccessOther', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('e7756052-18bd-4de0-8052-9768d9e56313', '2023-05-09 20:21:12.011', 'notifications', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('35dc46f6-b75e-44eb-b0a9-2e5a29ce996d', '2023-05-09 20:21:12.011', 'notifications', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('1ecd3c6e-aa70-43fb-9de9-64fd9dc92155', '2023-05-09 20:21:12.011', 'genFeedFromWebsite', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('f5b39fad-9b49-4541-b01a-e19387deee8b', '2023-05-09 20:21:12.011', 'genFeedFromWebsite', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('1b5eca07-2ff6-470d-83ff-6433f51c23f0', '2023-05-09 20:21:12.011', 'genFeedFromFeed', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('0a73d7a7-7c70-4a97-a1ea-48ddc637dc25', '2023-05-09 20:21:12.011', 'genFeedFromFeed', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('f80fb70e-3b90-46c9-8140-e0825cde3f6d', '2023-05-09 20:21:12.011', 'genFeedFromPageChange', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('6de5f128-da50-4868-ba85-ba67c03a01c1', '2023-05-09 20:21:12.011', 'genFeedFromPageChange', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('b063f4c0-e54b-453d-8191-382619816e84', '2023-05-09 20:21:12.011', 'genFeedWithPrerender', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('28861b74-30cb-4db1-8722-bda98165fd20', '2023-05-09 20:21:12.011', 'genFeedWithPrerender', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('77017e24-cff6-4859-a7de-4ce058daf0bf', '2023-05-09 20:21:12.011', 'genFeedWithPuppeteerScript', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'experimental', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('f22768b3-2b4f-4a28-b5d2-03e88fe516be', '2023-05-09 20:21:12.011', 'genFeedWithPuppeteerScript', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('2d34a40a-80c6-4bdc-811d-3432a0626f8d', '2023-05-09 20:21:12.011', 'feedAuthentication', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'experimental', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('da8b1c8d-b4a4-40a6-a078-5cc4f4ad17a5', '2023-05-09 20:21:12.011', 'feedAuthentication', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('05030b25-8686-4920-ad86-12db3c2eb7c7', '2023-05-09 20:21:12.011', 'feedsPrivateAccess', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'beta', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('0e727bda-bd00-4cdb-93e4-c48e0b0b446d', '2023-05-09 20:21:12.011', 'feedsPrivateAccess', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'beta', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('36045e64-c7f2-413b-8590-5622e3739d8d', '2023-05-09 20:21:12.011', 'bucketsPrivateAccess', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'beta', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('791983d6-062d-4245-a0bf-7d9ed0b98a0c', '2023-05-09 20:21:12.011', 'bucketsPrivateAccess', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'beta', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('1e3fefb1-4c33-4eb7-a7f3-db81c52d36bb', '2023-05-09 20:21:12.011', 'feedsFulltext', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('ca2309c2-bcbd-45c4-b8af-a67fa5ab99a6', '2023-05-09 20:21:12.011', 'feedsFulltext', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('2b1b5c4d-fb95-4dd9-98b3-c6207f2e3ec7', '2023-05-09 20:21:12.011', 'itemsInlineImages', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'stable', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('189ec16c-5dc0-4f5c-bb6d-59355544fd69', '2023-05-09 20:21:12.011', 'itemsInlineImages', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'stable', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('cf0cf9a6-b55a-479d-8cf8-d516b2d4829d', '2023-05-09 20:21:12.011', 'itemsNoUrlShortener', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('5cd0a1b3-4d41-4e8f-bca4-c5b8754d9c24', '2023-05-09 20:21:12.011', 'itemsNoUrlShortener', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'experimental', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('ec0330e7-39de-43f4-ab61-acca283d6be7', '2023-05-09 20:21:12.011', 'api', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'off', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('d70dc276-877b-4de3-8724-7fee1f918377', '2023-05-09 20:21:12.011', 'api', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'off', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('70f75af8-4427-431d-9066-754341f825ce', '2023-05-09 20:21:12.011', 'itemEmailForward', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'off', false, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('b713c468-72d6-4e1a-bfb5-efbce4a4f2a9', '2023-05-09 20:21:12.011', 'itemEmailForward', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'off', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('5f210107-c0b4-48b2-ac6f-b712d664509d', '2023-05-09 20:21:12.011', 'itemWebhookForward', '0df4ed43-dd1e-416e-b87b-0263cafeda02', 'off', true, NULL, 'bool');
INSERT INTO public.t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) VALUES ('2a1c35c7-8246-4e20-9947-3b6319889d93', '2023-05-09 20:21:12.011', 'itemWebhookForward', 'ef6c4b90-b6a6-4a43-b745-966e9da00159', 'off', true, NULL, 'bool');


--
-- Data for Name: t_feed_generic; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_feed_generic (id, createdat, feedspecification, websiteurl, nativefeed_id) VALUES ('e681a56d-e9b7-437f-95f2-e4f89b434d51', '2024-08-16 15:03:57.677', '{"selectors": {"count": null, "score": null, "contexts": null, "dateXPath": "./a[1]/section[1]/ul[1]/li[2]/time[1]", "linkXPath": "./a[1]", "contextXPath": "//div[1]/div[3]/main[1]/div[2]/article", "extendContext": "NONE", "paginationXPath": null, "dateIsStartOfEvent": false}, "fetchOptions": {"emit": "markup", "baseXpath": "", "prerender": false, "websiteUrl": "https://telepolis.de", "prerenderScript": "", "prerenderWaitUntil": "load"}, "parserOptions": {"version": "0.1", "strictMode": false, "minLinkGroupSize": 2, "minWordCountOfLink": 1}, "refineOptions": {"filter": null}}', 'https://telepolis.de', NULL);


--
-- Data for Name: t_feed_native; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_feed_native (id, createdat, description, domain, failedattemptcount, feedurl, harvestintervalminutes, harvestsitewithprerender, iconurl, imageurl, lang, lastchangedat, lastupdatedat, lat, lon, nextharvestat, ownerid, retentionsize, status, streamid, title, visibility, websiteurl, generic_feed_id, plugins) VALUES ('9750f712-023f-4e02-b1c4-a9f71fb7865f', '2024-08-16 15:03:57.851', NULL, 'telepolis.de', 0, 'http://localhost:8080/api/w2f/rule?v=0.1&u=https%3A%2F%2Ftelepolis.de&l=.%2Fa%5B1%5D&cp=%2F%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fmain%5B1%5D%2Fdiv%5B2%5D%2Farticle&pp=&dp=.%2Fa%5B1%5D%2Fsection%5B1%5D%2Ful%5B1%5D%2Fli%5B2%5D%2Ftime%5B1%5D&ec=&p=false&ps=&aw=load&ef=false&q=&', 10, false, NULL, NULL, NULL, '2024-08-16 15:03:58.969', '2024-08-16 15:03:58.998', NULL, NULL, '2024-08-16 15:13:59.001', '2621625f-2de8-4407-912d-04de63fb32a6', NULL, 'OK', 'b57ad84b-d29b-48c3-8125-e48cf0c98d4a', 'Onlinemagazin für Politik & Medien im digitalen Zeitalter | Telepolis', 'isPublic', 'https://telepolis.de', 'e681a56d-e9b7-437f-95f2-e4f89b434d51', '[]');
INSERT INTO public.t_feed_native (id, createdat, description, domain, failedattemptcount, feedurl, harvestintervalminutes, harvestsitewithprerender, iconurl, imageurl, lang, lastchangedat, lastupdatedat, lat, lon, nextharvestat, ownerid, retentionsize, status, streamid, title, visibility, websiteurl, generic_feed_id, plugins) VALUES ('7784a903-38a3-4da1-a46a-932d481ea2e4', '2024-08-16 15:05:11.401', NULL, NULL, 0, 'http://www.heise.de/tp/rss/news-xl.xml', 10, false, NULL, NULL, NULL, '2024-08-16 15:05:12.731', '2024-08-16 15:05:12.753', NULL, NULL, '2024-08-16 15:15:12.756', '2621625f-2de8-4407-912d-04de63fb32a6', NULL, 'OK', '6f52773a-df35-4766-aa1c-5110a6d8afe7', 'Telepolis', 'isPublic', NULL, NULL, '[]');


--
-- Data for Name: t_hyperlink; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_importer; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_importer (id, createdat, is_auto_release, bucketid, segment_digest, feedid, filter, lastupdatedat, lookaheadmin, ownerid, segment_size, segmentsortasc, segmentsortfield, title, triggerrefreshon, triggerscheduleexpression, triggerscheduledlastat, triggerschedulednextat, plugins) VALUES ('e14b8591-ad79-412e-866b-124208dc5d6a', '2024-08-16 15:03:57.862', true, '5a853d8f-bc1d-47b8-aec2-8a33de86dd7d', false, '9750f712-023f-4e02-b1c4-a9f71fb7865f', '', '2024-08-16 15:03:59.375', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', NULL, true, NULL, NULL, 'CHANGE', NULL, NULL, NULL, '[]');
INSERT INTO public.t_importer (id, createdat, is_auto_release, bucketid, segment_digest, feedid, filter, lastupdatedat, lookaheadmin, ownerid, segment_size, segmentsortasc, segmentsortfield, title, triggerrefreshon, triggerscheduleexpression, triggerscheduledlastat, triggerschedulednextat, plugins) VALUES ('aeb218bf-cec5-4016-905c-1ca4210c0c65', '2024-08-16 15:05:11.403', true, '0584f447-9a99-437c-a656-a2bda991193b', false, '7784a903-38a3-4da1-a46a-932d481ea2e4', '', '2024-08-16 15:05:12.833', NULL, '2621625f-2de8-4407-912d-04de63fb32a6', NULL, true, NULL, NULL, 'CHANGE', NULL, NULL, NULL, '[]');


--
-- Data for Name: t_otp; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_plan; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_plan (id, createdat, availability, costs, name, is_primary) VALUES ('0df4ed43-dd1e-416e-b87b-0263cafeda02', '2023-05-09 20:21:11.694', 'available', 0, 'free', true);
INSERT INTO public.t_plan (id, createdat, availability, costs, name, is_primary) VALUES ('ef6c4b90-b6a6-4a43-b745-966e9da00159', '2023-05-09 20:21:12.002', 'by_request', 9.99, 'basic', false);


--
-- Data for Name: t_stream; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_stream (id, createdat) VALUES ('e869e45c-0f3a-4102-8a8f-3518b23c4cd2', '2024-08-16 14:56:40.872');
INSERT INTO public.t_stream (id, createdat) VALUES ('fd03dbf0-378e-47ab-9efe-5e8c693d132d', '2024-08-16 14:56:41.129');
INSERT INTO public.t_stream (id, createdat) VALUES ('90950efc-3944-43b3-b242-80fdb6eac424', '2024-08-16 15:03:17.86');
INSERT INTO public.t_stream (id, createdat) VALUES ('3c7f5551-7827-4d53-ace6-0d48761262a8', '2024-08-16 15:03:57.672');
INSERT INTO public.t_stream (id, createdat) VALUES ('b57ad84b-d29b-48c3-8125-e48cf0c98d4a', '2024-08-16 15:03:57.85');
INSERT INTO public.t_stream (id, createdat) VALUES ('eb4ef3f5-159d-4c8f-9aa3-da3245416587', '2024-08-16 15:05:11.394');
INSERT INTO public.t_stream (id, createdat) VALUES ('6f52773a-df35-4766-aa1c-5110a6d8afe7', '2024-08-16 15:05:11.4');


--
-- Data for Name: t_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_user (id, createdat, approvedtermsat, date_format, email, hasapprovedterms, isroot, locked, name, notifications_stream_id, plan_id, time_format, purgescheduledfor, plugins) VALUES ('affad993-7719-49f9-a9fb-d666eb97baaf', '2024-08-16 14:56:40.871', NULL, NULL, 'anonymous@localhost', false, false, false, 'anonymous', 'e869e45c-0f3a-4102-8a8f-3518b23c4cd2', NULL, NULL, NULL, '{}');
INSERT INTO public.t_user (id, createdat, approvedtermsat, date_format, email, hasapprovedterms, isroot, locked, name, notifications_stream_id, plan_id, time_format, purgescheduledfor, plugins) VALUES ('6125ed83-055a-4424-96dc-96f7435ca31a', '2024-08-16 14:56:41.129', NULL, NULL, 'admin@localhost', false, true, false, 'root', 'fd03dbf0-378e-47ab-9efe-5e8c693d132d', NULL, NULL, NULL, '{}');
INSERT INTO public.t_user (id, createdat, approvedtermsat, date_format, email, hasapprovedterms, isroot, locked, name, notifications_stream_id, plan_id, time_format, purgescheduledfor, plugins) VALUES ('2621625f-2de8-4407-912d-04de63fb32a6', '2024-08-16 15:03:17.86', '2024-08-16 15:03:30.705', NULL, '7574272@github.com', true, false, false, 'Markus Ruepp', '90950efc-3944-43b3-b242-80fdb6eac424', NULL, NULL, NULL, '{}');


--
-- Data for Name: t_user_plan_subscription; Type: TABLE DATA; Schema: public; Owner: -
--



--
-- Data for Name: t_user_secret; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_user_secret (id, createdat, lastusedat, owner_id, type, validuntil, value) VALUES ('3267bd75-5efe-468a-aa77-41210c9fbbef', '2024-08-16 14:56:41.155', NULL, '6125ed83-055a-4424-96dc-96f7435ca31a', 'SecretKey', '2025-08-07 14:56:41.155', 'password');


--
-- Data for Name: t_web_document; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('419c7f6a-3643-40c6-91e6-6181c85f7d33', '2024-08-16 15:03:58.815', NULL, NULL, NULL, NULL, NULL, NULL, '





   Trinkwasser-Sabotagealarm bei Bundeswehr: Nur der Anfang?
 Kriegstüchtige Verteidigung und Bolzenschneider: Verdächtige Vorfälle an mehreren Standorten führen zur Debatte, wie verwundbar Bundeswehreinrichtungen sind.
  Thomas Pany
 heute, 14:00 Uhr

 2', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Trinkwasser-Sabotagealarm bei Bundeswehr: Nur der Anfang? Kriegstüchtige Verteidigung und Bolzenschn', '2024-08-16 15:03:58', 'https://telepolis.de/features/Trinkwasser-Sabotagealarm-bei-Bundeswehr-Nur-der-Anfang-9837718.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('d128970c-57fb-4969-adfb-1b04472ba1a6', '2024-08-16 15:03:58.832', NULL, NULL, NULL, NULL, NULL, NULL, '





   Klimawandel: Die Wirtschaft hat keinen Rückwärtsgang
 Grüne fordern Wirtschaft rückzubauen, um Klima zu retten. Experte warnt: Rückwärts führt in die Katastrophe. Steckt ein fataler Denkfehler dahinter?
  Heiner Flassbeck
 heute, 11:00 Uhr

 52', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Klimawandel: Die Wirtschaft hat keinen Rückwärtsgang Grüne fordern Wirtschaft rückzubauen, um Klima ', '2024-08-16 15:03:58', 'https://telepolis.de/features/Klimawandel-Die-Wirtschaft-hat-keinen-Rueckwaertsgang-9837137.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('cdc2fb5b-5cfa-4b82-b491-f1dd7dba845d', '2024-08-16 15:03:58.842', NULL, NULL, NULL, NULL, NULL, NULL, '





   Waffenproduktion: Warum die russische Kriegswirtschaft den Westen längst abgehängt hat
 Fehlender Weitblick und eine Rüstungsindustrie, die nur an Profiten interessiert ist, sind eine schlechte Kombination im Wettlauf mit Russland, meint unser Gastautor Mike Fredenburg.
  Mike Fredenburg
 heute, 10:00 Uhr

 41', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Waffenproduktion: Warum die russische Kriegswirtschaft den Westen längst abgehängt hat Fehlender Wei', '2024-08-16 15:03:58', 'https://telepolis.de/features/Waffenproduktion-Warum-die-russische-Kriegswirtschaft-den-Westen-laengst-abgehaengt-hat-9836619.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e577f716-d844-491a-ab70-429da726c8c2', '2024-08-16 15:03:58.851', NULL, NULL, NULL, NULL, NULL, NULL, '





   Gegenschlag von Boxerin Imane Khelif: Trump und Rowling in Klage namentlich genannt
 Kampf gegen Hass und Vorurteile: Wie Khelif gegen Rassismus und Sexismus streitet. Wer wird zur Rechenschaft gezogen?
  Christian Kliver
 heute, 09:00 Uhr

 41', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Gegenschlag von Boxerin Imane Khelif: Trump und Rowling in Klage namentlich genannt Kampf gegen Hass', '2024-08-16 15:03:58', 'https://telepolis.de/features/Gegenschlag-von-Boxerin-Imane-Khelif-Trump-und-Rowling-in-Klage-namentlich-genannt-9836842.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('9adb479a-b817-4bb7-903d-d99d30ab699c', '2024-08-16 15:03:58.858', NULL, NULL, NULL, NULL, NULL, NULL, '





   Nord Stream und medialer Mainstream: Fragen einer lesenden Autorin
 Patriotische Taucher oder geopolitische Marionetten? Neue Enthüllungen, neue Fragen. Die Investigativ-Berichterstattung der Tagesschau im kritischen Blick.
  Christiane Voges
 heute, 08:00 Uhr

 12', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Nord Stream und medialer Mainstream: Fragen einer lesenden Autorin Patriotische Taucher oder geopoli', '2024-08-16 15:03:58', 'https://telepolis.de/features/Nord-Stream-und-medialer-Mainstream-Fragen-einer-lesenden-Autorin-9836984.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('3da65efc-e13d-4c70-9382-9a441b5c64b7', '2024-08-16 15:03:58.868', NULL, NULL, NULL, NULL, NULL, NULL, '





   Chinesische Firmen dominieren Europas Solarmarkt – auch dank innovativer KI-Technik
 Chinesische Anbieter führen den PV-Markt an. Deutsche Hersteller haben das Nachsehen. Huawei setzt auf KI für mehr Effizienz im Bereich Photovoltaik.
  Christoph Jehle
 heute, 07:00 Uhr

 16', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Chinesische Firmen dominieren Europas Solarmarkt – auch dank innovativer KI-Technik Chinesische Anbi', '2024-08-16 15:03:58', 'https://telepolis.de/features/Chinesische-Firmen-dominieren-Europas-Solarmarkt-auch-dank-innovativer-KI-Technik-9831944.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('ffc9604c-50cc-4453-872a-4a5007f213a5', '2024-08-16 15:03:58.875', NULL, NULL, NULL, NULL, NULL, NULL, '





   Kursk-Offensive: Was der Vorstoß der Ukraine nach Russland mit Donald Trump zu tun hat
 Vor der US-Wahl sollen neue Realiäten geschaffen werden. Der Krieg wird in den kommenden Wochen seinen Charakter ändern. Woran das liegt.
  Christian Kliver
 heute, 00:00 Uhr

 48', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Kursk-Offensive: Was der Vorstoß der Ukraine nach Russland mit Donald Trump zu tun hat Vor der US-Wa', '2024-08-16 15:03:58', 'https://telepolis.de/features/Kursk-Offensive-Was-der-Vorstoss-der-Ukraine-nach-Russland-mit-Donald-Trump-zu-tun-hat-9836805.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('2fe0d930-7545-4faf-8ca8-38d111cb5ca0', '2024-08-16 15:03:58.884', NULL, NULL, NULL, NULL, NULL, NULL, '





   Beijing statt Paris: Wie China afrikanische Studierende an seine Unis lockt
 Volksrepublik verstärkt Bildungsoffensive in Afrika. Stipendienprogramm zieht jedes Jahr tausende Köpfe an. Schafft Beijing eine neue, pro-chinesische Generation?
  Marcel Kunzmann
 heute, 00:00 Uhr

 14', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Beijing statt Paris: Wie China afrikanische Studierende an seine Unis lockt Volksrepublik verstärkt ', '2024-08-16 15:03:58', 'https://telepolis.de/features/Beijing-statt-Paris-Wie-China-afrikanische-Studierende-an-seine-Unis-lockt-9836047.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('ff154c10-5db9-4774-b40c-f7e5988d8d97', '2024-08-16 15:03:58.891', NULL, NULL, NULL, NULL, NULL, NULL, '





   Bürgergeld: Wer kürzt mehr?
 Die CDU will einigen Beziehern das Bürgergeld streichen, die FDP es senken. Dabei gibt es bereits Verschärfungen. Ein feiner Wettbewerb um die besten Kürzungsideen.
  Björn Hendrig
 gestern, 16:00 Uhr

 75', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Bürgergeld: Wer kürzt mehr? Die CDU will einigen Beziehern das Bürgergeld streichen, die FDP es senk', '2024-08-16 15:03:58', 'https://telepolis.de/features/Buergergeld-Wer-kuerzt-mehr-9834469.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('d8c62bdf-b7bc-44c0-89a4-0f264c80d09c', '2024-08-16 15:03:58.899', NULL, NULL, NULL, NULL, NULL, NULL, '





   Anschlag auf Nord Stream: Billigte Wolodymyr Selenskyj den Anschlag?
 Neue Erkenntnisse zum Nord-Stream-Anschlag: Ukrainische Offiziere sollen involviert sein. Präsident Selenskyj gerät unter Verdacht. Wusste er mehr, als er zugibt?
  Bernd Müller
 gestern, 15:00 Uhr

 186', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Anschlag auf Nord Stream: Billigte Wolodymyr Selenskyj den Anschlag? Neue Erkenntnisse zum Nord-Stre', '2024-08-16 15:03:58', 'https://telepolis.de/features/Anschlag-auf-Nord-Stream-Billigte-Wolodymyr-Selenskyj-den-Anschlag-9836507.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('b2baa47b-e932-46c2-ad5b-23493cfbbec9', '2024-08-16 15:03:58.906', NULL, NULL, NULL, NULL, NULL, NULL, '





   "Alien: Romulus": Schleimig, böse, unkaputtbar
 Der Feind in uns: Die Alien-Filmreihe zeigt Bilder des Schreckens und formuliert unsere eigene Frage als Gestalt. Für Fans der frühen Filme, wie für eine neue Kinogeneration.
  Rüdiger Suchsland
 gestern, 14:00 Uhr

 6', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, '"Alien: Romulus": Schleimig, böse, unkaputtbar Der Feind in uns: Die Alien-Filmreihe zeigt Bilder de', '2024-08-16 15:03:58', 'https://telepolis.de/features/Alien-Romulus-Schleimig-boese-unkaputtbar-9835715.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('c7fe9326-3af6-4848-98cd-c4722d9778ee', '2024-08-16 15:03:58.914', NULL, NULL, NULL, NULL, NULL, NULL, '





   Chinas Küstenwache rüstet auf: Neues Schiff nach Zerstörer-Vorbild
 Neues Küstenschutzschiff soll über große Reichweite verfügen. Einsatz im Ost- und Südchinesischen Meer geplant. Indienststellung soll in Kürze erfolgen.
  Marcel Kunzmann
 gestern, 12:00 Uhr

 13', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Chinas Küstenwache rüstet auf: Neues Schiff nach Zerstörer-Vorbild Neues Küstenschutzschiff soll übe', '2024-08-16 15:03:58', 'https://telepolis.de/features/Chinas-Kuestenwache-ruestet-auf-Neues-Schiff-nach-Zerstoerer-Vorbild-9835918.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('22f203b1-c3c5-429c-bc0e-b89476fe74eb', '2024-08-16 15:05:12.6', NULL, NULL, NULL, NULL, NULL, NULL, 'Neue Erkenntnisse zum Nord-Stream-Anschlag: Ukrainische Offiziere sollen involviert sein. Präsident Selenskyj gerät unter Verdacht. Wusste er mehr, als er zugibt?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 15:00:00', 0, NULL, 'Anschlag auf Nord Stream: Billigte Wolodymyr Selenskyj den Anschlag?', '2024-08-15 15:00:00', 'https://www.telepolis.de/features/Anschlag-auf-Nord-Stream-Billigte-Wolodymyr-Selenskyj-den-Anschlag-9836507.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e3b539c4-ea81-4f72-91ec-548f626cd82b', '2024-08-16 15:03:58.921', NULL, NULL, NULL, NULL, NULL, NULL, '





   Tickt auch bei Männern die biologische Uhr? Neue Studie warnt vor später Vaterschaft
 Neue US-Studie zeigt: Auch für Männer tickt die biologische Uhr. Mit steigendem Alter des Vaters nehmen die Risiken für das Kind zu. Was bedeutet das für Spätväter?
  Bernd Müller
 gestern, 11:00 Uhr

 25', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Tickt auch bei Männern die biologische Uhr? Neue Studie warnt vor später Vaterschaft Neue US-Studie ', '2024-08-16 15:03:58', 'https://telepolis.de/features/Tickt-auch-bei-Maennern-die-biologische-Uhr-Neue-Studie-warnt-vor-spaeter-Vaterschaft-9836125.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('70f1cd22-1ff4-4263-9f7d-83d8c28d6df8', '2024-08-16 15:03:58.927', NULL, NULL, NULL, NULL, NULL, NULL, '





   Compact-Versagen von Faeser: Wieso ist diese Frau als Innenministerin noch im Amt?
 Die Niederlage gegen die Rechten war absehbar. Und es ist gut, dass sie gekommen ist. Wie wird das Ministerium nun reagieren? Ein Telepolis-Leitartikel.
  Harald Neuber
 gestern, 10:00 Uhr

 118', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Compact-Versagen von Faeser: Wieso ist diese Frau als Innenministerin noch im Amt? Die Niederlage ge', '2024-08-16 15:03:58', 'https://telepolis.de/features/Compact-Versagen-von-Faeser-Wieso-ist-diese-Frau-als-Innenministerin-noch-im-Amt-9835662.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('32ca1717-3f74-471d-8347-76884cbdfd5b', '2024-08-16 15:03:58.934', NULL, NULL, NULL, NULL, NULL, NULL, '





   Abschreckung gegen China: Japans Marine setzt auf unbemannte Schiffe
 Japanische Marine sieht sich derzeit schlecht gewappnet. Neue Investitionen in autonome Marineeinheiten. Kooperation mit US-Navy.
  Marcel Kunzmann
 gestern, 10:00 Uhr

 1', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 15:03:58', 0, NULL, 'Abschreckung gegen China: Japans Marine setzt auf unbemannte Schiffe Japanische Marine sieht sich de', '2024-08-16 15:03:58', 'https://telepolis.de/features/Abschreckung-gegen-China-Japans-Marine-setzt-auf-unbemannte-Schiffe-9835011.html', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('3324f158-4815-44b2-866f-3124c810ffc2', '2024-08-16 15:05:12.536', NULL, NULL, NULL, NULL, NULL, NULL, 'Kriegstüchtige Verteidigung und Bolzenschneider: Verdächtige Vorfälle an mehreren Standorten führen zur Debatte, wie verwundbar Bundeswehreinrichtungen sind.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 14:00:00', 0, NULL, 'Trinkwasser-Sabotagealarm bei Bundeswehr: Nur der Anfang?', '2024-08-16 14:00:00', 'https://www.telepolis.de/features/Trinkwasser-Sabotagealarm-bei-Bundeswehr-Nur-der-Anfang-9837718.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('1209d145-da96-4fa8-8a83-85b79d2f60cc', '2024-08-16 15:05:12.542', NULL, NULL, NULL, NULL, NULL, NULL, 'Ukraine hat Truppen für einen Vorstoß nach Kursk abgezogen. Das nutzt Russland im Osten aus und erobert Gebiete. Steht die ukrainische Front vor dem Zusammenbruch?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 13:00:00', 0, NULL, 'Ukrainische Truppen im Osten unter Druck nach Offensive in Kursk', '2024-08-16 13:00:00', 'https://www.telepolis.de/features/Ukrainische-Truppen-im-Osten-unter-Druck-nach-Offensive-in-Kursk-9837620.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('29e9e534-f5fe-4d2c-84e1-466dc5033a40', '2024-08-16 15:05:12.547', NULL, NULL, NULL, NULL, NULL, NULL, 'Elektronische Kriegsführung. Radare vom Südchinesischen Meer bis nach Alaska offenbar zusammengeschalten. KI entdeckt Muster.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 12:00:00', 0, NULL, 'Mysteriöse Signale: Chinesische KI entdeckt koordinierte Radare', '2024-08-16 12:00:00', 'https://www.telepolis.de/features/Mysterioese-Signale-Chinesische-KI-entdeckt-koordinierte-Radare-9837428.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('0cd6bbad-b71d-411d-beec-a97fc7de9784', '2024-08-16 15:05:12.552', NULL, NULL, NULL, NULL, NULL, NULL, 'Grüne fordern Wirtschaft rückzubauen, um Klima zu retten. Experte warnt: Rückwärts führt in die Katastrophe. Steckt ein fataler Denkfehler dahinter?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 11:00:00', 0, NULL, 'Klimawandel: Die Wirtschaft hat keinen Rückwärtsgang', '2024-08-16 11:00:00', 'https://www.telepolis.de/features/Klimawandel-Die-Wirtschaft-hat-keinen-Rueckwaertsgang-9837137.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e755abc8-0dbc-4a92-9d58-33df867d66f0', '2024-08-16 15:05:12.557', NULL, NULL, NULL, NULL, NULL, NULL, 'Fehlender Weitblick und eine Rüstungsindustrie, die nur an Profiten interessiert ist, sind eine schlechte Kombination im Wettlauf mit Russland, meint unser Gastautor Mike Fredenburg.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 10:00:00', 0, NULL, 'Waffenproduktion: Warum die russische Kriegswirtschaft den Westen längst abgehängt hat', '2024-08-16 10:00:00', 'https://www.telepolis.de/features/Waffenproduktion-Warum-die-russische-Kriegswirtschaft-den-Westen-laengst-abgehaengt-hat-9836619.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('f8fe768b-76e6-49e4-b20a-026dfef8f4d8', '2024-08-16 15:05:12.562', NULL, NULL, NULL, NULL, NULL, NULL, 'Kampf gegen Hass und Vorurteile: Wie Khelif gegen Rassismus und Sexismus streitet. Wer wird zur Rechenschaft gezogen?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 09:00:00', 0, NULL, 'Gegenschlag von Boxerin Imane Khelif: Trump und Rowling in Klage namentlich genannt', '2024-08-16 09:00:00', 'https://www.telepolis.de/features/Gegenschlag-von-Boxerin-Imane-Khelif-Trump-und-Rowling-in-Klage-namentlich-genannt-9836842.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e7fdb578-ea36-4bd8-b047-e5f8e4050631', '2024-08-16 15:05:12.568', NULL, NULL, NULL, NULL, NULL, NULL, 'Frage nach Verantwortlichen für die Zerstörung der Nord-Stream-Pipelines bleibt brisant. Neue Entwicklungen werfen Licht auf Täter. Kiew will keine Schuld tragen.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 08:30:00', 0, NULL, 'Wer steckt hinter dem Nord-Stream-Anschlag? Kiew wiederholt Dementi', '2024-08-16 08:30:00', 'https://www.telepolis.de/features/Wer-steckt-hinter-dem-Nord-Stream-Anschlag-Kiew-wiederholt-Dementi-9837046.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('17205a16-ed97-42da-a1c2-b52403a9441a', '2024-08-16 15:05:12.573', NULL, NULL, NULL, NULL, NULL, NULL, 'Patriotische Taucher oder geopolitische Marionetten? Neue Enthüllungen, neue Fragen. Die Investigativ-Berichterstattung der Tagesschau im kritischen Blick.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 08:00:00', 0, NULL, 'Nord Stream und medialer Mainstream: Fragen einer lesenden Autorin', '2024-08-16 08:00:00', 'https://www.telepolis.de/features/Nord-Stream-und-medialer-Mainstream-Fragen-einer-lesenden-Autorin-9836984.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('5b0fcd1c-654a-4bc3-86c6-566676290edb', '2024-08-16 15:05:12.579', NULL, NULL, NULL, NULL, NULL, NULL, 'Chinesische Anbieter führen den PV-Markt an. Deutsche Hersteller haben das Nachsehen. Huawei setzt auf KI für mehr Effizienz im Bereich Photovoltaik.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 07:00:00', 0, NULL, 'Chinesische Firmen dominieren Europas Solarmarkt – auch dank innovativer KI-Technik', '2024-08-16 07:00:00', 'https://www.telepolis.de/features/Chinesische-Firmen-dominieren-Europas-Solarmarkt-auch-dank-innovativer-KI-Technik-9831944.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('5b00baec-25a0-42da-8751-ffbde4086743', '2024-08-16 15:05:12.585', NULL, NULL, NULL, NULL, NULL, NULL, 'Vor der US-Wahl sollen neue Realiäten geschaffen werden. Der Krieg wird in den kommenden Wochen seinen Charakter ändern. Woran das liegt.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 00:00:00', 0, NULL, 'Kursk-Offensive: Was der Vorstoß der Ukraine nach Russland mit Donald Trump zu tun hat', '2024-08-16 00:00:00', 'https://www.telepolis.de/features/Kursk-Offensive-Was-der-Vorstoss-der-Ukraine-nach-Russland-mit-Donald-Trump-zu-tun-hat-9836805.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('418585a8-9237-45d1-b1fb-a5fe363d97ac', '2024-08-16 15:05:12.59', NULL, NULL, NULL, NULL, NULL, NULL, 'Volksrepublik verstärkt Bildungsoffensive in Afrika. Stipendienprogramm zieht jedes Jahr tausende Köpfe an. Schafft Beijing eine neue, pro-chinesische Generation?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-16 00:00:00', 0, NULL, 'Beijing statt Paris: Wie China afrikanische Studierende an seine Unis lockt', '2024-08-16 00:00:00', 'https://www.telepolis.de/features/Beijing-statt-Paris-Wie-China-afrikanische-Studierende-an-seine-Unis-lockt-9836047.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('44f6005a-3a78-4a7b-a192-a483dd587b99', '2024-08-16 15:05:12.595', NULL, NULL, NULL, NULL, NULL, NULL, 'Die CDU will einigen Beziehern das Bürgergeld streichen, die FDP es senken. Dabei gibt es bereits Verschärfungen. Ein feiner Wettbewerb um die besten Kürzungsideen.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 16:00:00', 0, NULL, 'Bürgergeld: Wer kürzt mehr?', '2024-08-15 16:00:00', 'https://www.telepolis.de/features/Buergergeld-Wer-kuerzt-mehr-9834469.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e8d0baf3-2b0f-4e90-a48a-7994a08788dc', '2024-08-16 15:05:12.605', NULL, NULL, NULL, NULL, NULL, NULL, 'Der Feind in uns: Die Alien-Filmreihe zeigt Bilder des Schreckens und formuliert unsere eigene Frage als Gestalt. Für Fans der frühen Filme, wie für eine neue Kinogeneration.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 14:00:00', 0, NULL, '"Alien: Romulus": Schleimig, böse, unkaputtbar', '2024-08-15 14:00:00', 'https://www.telepolis.de/features/Alien-Romulus-Schleimig-boese-unkaputtbar-9835715.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('4afb0cec-b194-468c-a089-1b263f85fdbb', '2024-08-16 15:05:12.61', NULL, NULL, NULL, NULL, NULL, NULL, 'Neues Küstenschutzschiff soll über große Reichweite verfügen. Einsatz im Ost- und Südchinesischen Meer geplant. Indienststellung soll in Kürze erfolgen.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 12:00:00', 0, NULL, 'Chinas Küstenwache rüstet auf: Neues Schiff nach Zerstörer-Vorbild', '2024-08-15 12:00:00', 'https://www.telepolis.de/features/Chinas-Kuestenwache-ruestet-auf-Neues-Schiff-nach-Zerstoerer-Vorbild-9835918.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('07ca943d-e9bf-4cfa-988e-6e7a9a6504b3', '2024-08-16 15:05:12.617', NULL, NULL, NULL, NULL, NULL, NULL, 'Neue US-Studie zeigt: Auch für Männer tickt die biologische Uhr. Mit steigendem Alter des Vaters nehmen die Risiken für das Kind zu. Was bedeutet das für Spätväter?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 11:00:00', 0, NULL, 'Tickt auch bei Männern die biologische Uhr? Neue Studie warnt vor später Vaterschaft', '2024-08-15 11:00:00', 'https://www.telepolis.de/features/Tickt-auch-bei-Maennern-die-biologische-Uhr-Neue-Studie-warnt-vor-spaeter-Vaterschaft-9836125.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('40003e90-ac2d-4d47-b03e-38b21755d883', '2024-08-16 15:05:12.622', NULL, NULL, NULL, NULL, NULL, NULL, 'Die Niederlage gegen die Rechten war absehbar. Und es ist gut, dass sie gekommen ist. Wie wird das Ministerium nun reagieren? Ein Telepolis-Leitartikel.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 10:00:00', 0, NULL, 'Compact-Versagen von Faeser: Wieso ist diese Frau als Innenministerin noch im Amt?', '2024-08-15 10:00:00', 'https://www.telepolis.de/features/Compact-Versagen-von-Faeser-Wieso-ist-diese-Frau-als-Innenministerin-noch-im-Amt-9835662.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('8ab186e7-9400-4bb0-8b88-b44f9af6c807', '2024-08-16 15:05:12.628', NULL, NULL, NULL, NULL, NULL, NULL, 'Japanische Marine sieht sich derzeit schlecht gewappnet. Neue Investitionen in autonome Marineeinheiten. Kooperation mit US-Navy.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 10:00:00', 0, NULL, 'Abschreckung gegen China: Japans Marine setzt auf unbemannte Schiffe', '2024-08-15 10:00:00', 'https://www.telepolis.de/features/Abschreckung-gegen-China-Japans-Marine-setzt-auf-unbemannte-Schiffe-9835011.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('a4eb7098-df36-4cf8-a0c9-0e1b0f739c5c', '2024-08-16 15:05:12.634', NULL, NULL, NULL, NULL, NULL, NULL, 'Pläne könnten NS-Vergangenheit wieder salonfähig machen. Droht eine schleichende Rehabilitierung? Wie weit darf Tradition gehen?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 09:00:00', 0, NULL, 'Bundeswehr: Militärische Traditionspflege im Land von Hitlers Wehrmacht', '2024-08-15 09:00:00', 'https://www.telepolis.de/features/Bundeswehr-Militaerische-Traditionspflege-im-Land-von-Hitlers-Wehrmacht-9835500.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('a97dc0ea-ec43-483b-86da-7f20fef62ce3', '2024-08-16 15:05:12.639', NULL, NULL, NULL, NULL, NULL, NULL, 'Kiew bindet wertvolle Ressourcen in einer unsicheren Offensive, während im Donbass der Zusammenbruch droht. Eine Einschätzung.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 08:00:00', 0, NULL, 'Kursk: Die Schattenfront, die den Ukraine-Krieg nicht entscheidet', '2024-08-15 08:00:00', 'https://www.telepolis.de/features/Kursk-Die-Schattenfront-die-den-Ukraine-Krieg-nicht-entscheidet-9835608.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('bbf9f3af-68b7-448e-8b8c-0a345bdbb6f5', '2024-08-16 15:05:12.645', NULL, NULL, NULL, NULL, NULL, NULL, 'PV-Boom überfordert Verteilnetze. Netzausbau hinkt hinterher, Abschaltungen drohen. Wie können Netze dem wachsenden Druck der Solaranlagen standhalten?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 07:00:00', 0, NULL, 'Droht den Verteilnetzen wegen PV-Boom der Zusammenbruch?', '2024-08-15 07:00:00', 'https://www.telepolis.de/features/Droht-den-Verteilnetzen-wegen-PV-Boom-der-Zusammenbruch-9832883.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('6483a3ff-226c-49a4-aedb-70ec5dd1909d', '2024-08-16 15:05:12.651', NULL, NULL, NULL, NULL, NULL, NULL, 'USA haben sich bei Sanktionen verkalkuliert. China begrüßt "peinliche Entscheidung". Doch neue Verschärfungen sind bereits geplant.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 00:00:00', 0, NULL, 'Washington macht Rückzieher: Chinesisches Tech-Unternehmen von Sanktionsliste gestrichen', '2024-08-15 00:00:00', 'https://www.telepolis.de/features/Washington-macht-Rueckzieher-Chinesisches-Tech-Unternehmen-von-Sanktionsliste-gestrichen-9834806.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('f6337f71-357a-47c5-8d2f-3edf38066350', '2024-08-16 15:05:12.657', NULL, NULL, NULL, NULL, NULL, NULL, 'Alle Welt schaut auf Kursk. Der Krieg könnte sich aber im Osten der Ukraine entscheiden. Hier die jüngsten Ereignisse.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-15 00:00:00', 0, NULL, 'Trotz Kursk-Vorstoß: Kämpfe in der Ukraine intensivieren sich', '2024-08-15 00:00:00', 'https://www.telepolis.de/features/Trotz-Kursk-Vorstoss-Kaempfe-in-der-Ukraine-intensivieren-sich-9835541.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('e0d3221b-12fe-4d37-9a40-ca13f589d54a', '2024-08-16 15:05:12.663', NULL, NULL, NULL, NULL, NULL, NULL, 'Einige Folgen der ukrainischen Invasion bei Kursk in Russland stehen bereits unabhängig vom militärischen Ausgang fest. Die von Kiew erwünschten sind nicht dabei.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 16:00:00', 0, NULL, 'Kursk-Invasion: Folgen in Russland', '2024-08-14 16:00:00', 'https://www.telepolis.de/features/Kursk-Invasion-Folgen-in-Russland-9833497.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('affa06b4-79c6-4e7c-8a0a-8a0121be9c19', '2024-08-16 15:05:12.668', NULL, NULL, NULL, NULL, NULL, NULL, 'Das Verbot des rechtsextremen "Compact"-Magazins wurde vorläufig ausgesetzt. Das Bundesverwaltungsgericht gab dem Eilantrag statt.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 15:00:00', 0, NULL, 'Compact-Verbot: Gericht stoppt vorerst Vollzug', '2024-08-14 15:00:00', 'https://www.telepolis.de/features/Compact-Verbot-Gericht-stoppt-vorerst-Vollzug-9834951.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('3e924cfb-4ef4-428a-be7d-769fdecb2b94', '2024-08-16 15:05:12.674', NULL, NULL, NULL, NULL, NULL, NULL, 'Große Studie mit breiter Datenbasis: Impfung hat Covid-19-Sterblichkeit um 59 Prozent gesenkt. Sie war vor allem für ältere Menschen von Nutzen.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 14:00:00', 0, NULL, 'Lancet-Studie: Impfungen gegen Covid-19 retteten 1,6 Millionen Leben in Europa', '2024-08-14 14:00:00', 'https://www.telepolis.de/features/Lancet-Studie-Impfungen-gegen-Covid-19-retteten-1-6-Millionen-Leben-in-Europa-9834759.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('0883d555-99aa-402a-b7d2-9f5d1d282937', '2024-08-16 15:05:12.679', NULL, NULL, NULL, NULL, NULL, NULL, 'Nach Haniyyas Ermordung führt der Hardliner Yahya Sinwar die Hamas. Wie wird sich der Kurs der Organisation ändern? Unsere Gastautoren beleuchten die neu entstandene Lage.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 12:00:00', 0, NULL, 'Neuer Hamas-Chef: Welche Folgen hat Sinwars Ernennung für die Lage in Nahost?', '2024-08-14 12:00:00', 'https://www.telepolis.de/features/Neuer-Hamas-Chef-Welche-Folgen-hat-Sinwars-Ernennung-fuer-die-Lage-in-Nahost-9834501.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('d1c4a1a8-c409-4432-98ba-acfc89c40f4e', '2024-08-16 15:05:12.684', NULL, NULL, NULL, NULL, NULL, NULL, 'Die Stimmung der deutschen Wirtschaft ist im Keller. Laut ZEW-Umfrage fielen die Konjunkturerwartungen um 22,6 Punkte. Wie reagiert die Politik?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 11:00:00', 0, NULL, 'Deutschlands Wirtschaft im Stimmungstief: ZEW-Index bricht ein', '2024-08-14 11:00:00', 'https://www.telepolis.de/features/Deutschlands-Wirtschaft-im-Stimmungstief-ZEW-Index-bricht-ein-9834544.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('64de34ae-a726-40d9-9fb5-bb65198f1441', '2024-08-16 15:05:12.688', NULL, NULL, NULL, NULL, NULL, NULL, 'Taiwan, China und die Philippinen erheben Ansprüche. Manila wirft Beijing "aggressives Verhalten" vor. Jüngstes Abkommen bereits wieder in Gefahr?', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 10:00:00', 0, NULL, 'Spannungen im Südchinesischen Meer: Erneuter Eklat zwischen Manila und Beijing', '2024-08-14 10:00:00', 'https://www.telepolis.de/features/Spannungen-im-Suedchinesischen-Meer-Erneuter-Eklat-zwischen-Manila-und-Beijing-9833772.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');
INSERT INTO public.t_web_document (id, createdat, aliasurl, attachments, contentraw, contentrawmime, contenttext, contenttitle, description, finalized, hasaudio, hasfulltext, hasvideo, imageurl, pending_plugins, pluginscooldownuntil, releasedat, score, startingat, title, updatedat, url, executed_plugins) VALUES ('910db22b-e52f-489a-af2d-699cd656659b', '2024-08-16 15:05:12.693', NULL, NULL, NULL, NULL, NULL, NULL, 'Chinas Bauindustrie deckt über 80 Prozent ihres Sandbedarfs aus künstlicher Produktion. Innovation könnte globale Ressourcenkrise mildern. Warum das wichtig ist.', false, false, false, false, NULL, '["score", "detectMedia"]', NULL, '2024-08-14 09:00:00', 0, NULL, 'China setzt auf künstlichen Sand: Ist das die Lösung für die globale Ressourcenkrise?', '2024-08-14 09:00:00', 'https://www.telepolis.de/features/China-setzt-auf-kuenstlichen-Sand-Ist-das-die-Loesung-fuer-die-globale-Ressourcenkrise-9832930.html?wt_mc=rss.red.tp.tp.atom.beitrag.beitrag', '[]');


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: t_article t_article_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_article
    ADD CONSTRAINT t_article_pkey PRIMARY KEY (id);


--
-- Name: t_bucket t_bucket_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_bucket
    ADD CONSTRAINT t_bucket_pkey PRIMARY KEY (id);


--
-- Name: t_feature t_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT t_feature_pkey PRIMARY KEY (id);


--
-- Name: t_feed_generic t_feed_generic_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_generic
    ADD CONSTRAINT t_feed_generic_pkey PRIMARY KEY (id);


--
-- Name: t_feed_native t_feed_native_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_native
    ADD CONSTRAINT t_feed_native_pkey PRIMARY KEY (id);


--
-- Name: t_hyperlink t_hyperlink_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_hyperlink
    ADD CONSTRAINT t_hyperlink_pkey PRIMARY KEY (id);


--
-- Name: t_importer t_importer_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_importer
    ADD CONSTRAINT t_importer_pkey PRIMARY KEY (id);


--
-- Name: t_otp t_otp_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_otp
    ADD CONSTRAINT t_otp_pkey PRIMARY KEY (id);


--
-- Name: t_plan t_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT t_plan_pkey PRIMARY KEY (id);


--
-- Name: t_stream t_stream_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_stream
    ADD CONSTRAINT t_stream_pkey PRIMARY KEY (id);


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
-- Name: t_plan uk_hslenih4o8iw4gbho72545mf3; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT uk_hslenih4o8iw4gbho72545mf3 UNIQUE (name);


--
-- Name: t_user uk_i6qjjoe560mee5ajdg7v1o6mi; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT uk_i6qjjoe560mee5ajdg7v1o6mi UNIQUE (email);


--
-- Name: t_importer ukknq9ceicvfc210mced7ncsuie; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_importer
    ADD CONSTRAINT ukknq9ceicvfc210mced7ncsuie UNIQUE (bucketid, feedid);


--
-- Name: t_feed_native uniqueownerandfeed; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_native
    ADD CONSTRAINT uniqueownerandfeed UNIQUE (ownerid, feedurl);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_hyperlink_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_hyperlink_url ON public.t_hyperlink USING btree (fromid);


--
-- Name: idx_web_document_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_web_document_url ON public.t_web_document USING btree (url);


--
-- Name: t_bucket fk157cu4wjd97imxmg4ns0x3x85; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_bucket
    ADD CONSTRAINT fk157cu4wjd97imxmg4ns0x3x85 FOREIGN KEY (streamid) REFERENCES public.t_stream(id);


--
-- Name: t_feed_generic fk2ft85wtvm6ivw0vxhteihm0rw; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_generic
    ADD CONSTRAINT fk2ft85wtvm6ivw0vxhteihm0rw FOREIGN KEY (nativefeed_id) REFERENCES public.t_feed_native(id);


--
-- Name: t_otp fk36b6qk1g90ucc651dole1w4et; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_otp
    ADD CONSTRAINT fk36b6qk1g90ucc651dole1w4et FOREIGN KEY (userid) REFERENCES public.t_user(id);


--
-- Name: t_user_secret fk4evcslbhw4nofy5xsl2yyqxjk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_secret
    ADD CONSTRAINT fk4evcslbhw4nofy5xsl2yyqxjk FOREIGN KEY (owner_id) REFERENCES public.t_user(id);


--
-- Name: t_importer fk4u7buardohn32sp7yocqn59ke; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_importer
    ADD CONSTRAINT fk4u7buardohn32sp7yocqn59ke FOREIGN KEY (ownerid) REFERENCES public.t_user(id);


--
-- Name: t_article fk5a2eyi09un1yg3xls70asf5va; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_article
    ADD CONSTRAINT fk5a2eyi09un1yg3xls70asf5va FOREIGN KEY (webdocumentid) REFERENCES public.t_web_document(id);


--
-- Name: t_user fk5t89sip0n4g578g4yxaugjc1h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT fk5t89sip0n4g578g4yxaugjc1h FOREIGN KEY (notifications_stream_id) REFERENCES public.t_stream(id);


--
-- Name: t_user_plan_subscription fk8ncewprxstppim67lx6e7ysn4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_plan_subscription
    ADD CONSTRAINT fk8ncewprxstppim67lx6e7ysn4 FOREIGN KEY (plan_id) REFERENCES public.t_plan(id);


--
-- Name: t_user_plan_subscription fk9fe7lce4xahbf06k9eiwgy02h; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user_plan_subscription
    ADD CONSTRAINT fk9fe7lce4xahbf06k9eiwgy02h FOREIGN KEY (user_id) REFERENCES public.t_user(id);


--
-- Name: map_plan_to_feature fkavs1bn9cp7ur1t2xqguf12b1t; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.map_plan_to_feature
    ADD CONSTRAINT fkavs1bn9cp7ur1t2xqguf12b1t FOREIGN KEY (feature_id) REFERENCES public.t_feature(id);


--
-- Name: t_user fkciy9noxqwbr96ybkp5fv2c2ty; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2ty FOREIGN KEY (plan_id) REFERENCES public.t_plan(id);


--
-- Name: t_article fkdh0dxm4bk8ahvk23dnfc40u8y; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_article
    ADD CONSTRAINT fkdh0dxm4bk8ahvk23dnfc40u8y FOREIGN KEY (streamid) REFERENCES public.t_stream(id);


--
-- Name: t_importer fkdunmc6oa4jocbgv4q4jyh4xny; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_importer
    ADD CONSTRAINT fkdunmc6oa4jocbgv4q4jyh4xny FOREIGN KEY (bucketid) REFERENCES public.t_bucket(id);


--
-- Name: t_article fkdxiy9chl8ghd9848def9b8o4o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_article
    ADD CONSTRAINT fkdxiy9chl8ghd9848def9b8o4o FOREIGN KEY (importerid) REFERENCES public.t_importer(id);


--
-- Name: t_hyperlink fkgd7hbkkhrefq82vngp00so06d; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_hyperlink
    ADD CONSTRAINT fkgd7hbkkhrefq82vngp00so06d FOREIGN KEY (fromid) REFERENCES public.t_web_document(id);


--
-- Name: t_bucket fkhr17e39pk9333v21m3ha23ggl; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_bucket
    ADD CONSTRAINT fkhr17e39pk9333v21m3ha23ggl FOREIGN KEY (ownerid) REFERENCES public.t_user(id);


--
-- Name: t_hyperlink fkixc22j33g4ov6eskdww7884ci; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_hyperlink
    ADD CONSTRAINT fkixc22j33g4ov6eskdww7884ci FOREIGN KEY (toid) REFERENCES public.t_web_document(id);


--
-- Name: t_feature fkj9etkvylxqaj7ee5e840nphcc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT fkj9etkvylxqaj7ee5e840nphcc FOREIGN KEY (plan_id) REFERENCES public.t_plan(id);


--
-- Name: t_importer fkmfgv3wp5008are0gbk2il2blp; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_importer
    ADD CONSTRAINT fkmfgv3wp5008are0gbk2il2blp FOREIGN KEY (feedid) REFERENCES public.t_feed_native(id);


--
-- Name: t_feed_native fkn9kfg8o3jfarh3kqa960w7t3q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_native
    ADD CONSTRAINT fkn9kfg8o3jfarh3kqa960w7t3q FOREIGN KEY (generic_feed_id) REFERENCES public.t_feed_generic(id);


--
-- Name: t_feed_native fknwf9rwa6eoqe5p08mo6uxkgwn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_native
    ADD CONSTRAINT fknwf9rwa6eoqe5p08mo6uxkgwn FOREIGN KEY (streamid) REFERENCES public.t_stream(id);


--
-- Name: t_feed_native fkpadc1pn1ghj7c1qfnsau0b1ml; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feed_native
    ADD CONSTRAINT fkpadc1pn1ghj7c1qfnsau0b1ml FOREIGN KEY (ownerid) REFERENCES public.t_user(id);


--
-- Name: t_article fkq3lxplias2mkt5aqmxc1d9kiv; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_article
    ADD CONSTRAINT fkq3lxplias2mkt5aqmxc1d9kiv FOREIGN KEY (ownerid) REFERENCES public.t_user(id);


--
-- Name: map_plan_to_feature fksc26dpu29xdimt1d0ejr4n3oa; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.map_plan_to_feature
    ADD CONSTRAINT fksc26dpu29xdimt1d0ejr4n3oa FOREIGN KEY (plan_id) REFERENCES public.t_plan(id);


--
-- PostgreSQL database dump complete
--

