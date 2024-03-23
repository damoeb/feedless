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
-- Name: t_agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_agent (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    connectionid character varying(255) NOT NULL,
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
    planid uuid NOT NULL,
    scope character varying(50) NOT NULL,
    valueboolean boolean,
    valueint integer,
    valuetype character varying(50) NOT NULL
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
    validuntil timestamp(6) without time zone NOT NULL
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
    primaryplan boolean NOT NULL,
    product character varying(50) NOT NULL
);


--
-- Name: t_scrape_debug; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_scrape_debug (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    console boolean NOT NULL,
    cookies boolean NOT NULL,
    html boolean NOT NULL,
    network boolean NOT NULL,
    screenshot boolean NOT NULL
);


--
-- Name: t_scrape_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_scrape_source (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    actions jsonb NOT NULL,
    additionalwaitsec integer NOT NULL,
    debugconsole boolean NOT NULL,
    debugcookies boolean NOT NULL,
    debughtml boolean NOT NULL,
    debugnetwork boolean NOT NULL,
    debugscreenshot boolean NOT NULL,
    emit jsonb NOT NULL,
    erroneous boolean NOT NULL,
    language character varying(255),
    lasterrormessage character varying(255),
    subscriptionid uuid NOT NULL,
    timeout integer,
    url character varying(255) NOT NULL,
    viewport jsonb,
    waituntil character varying(255)
);


--
-- Name: t_segment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_segment (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    digest boolean NOT NULL,
    size integer NOT NULL,
    sortasc boolean NOT NULL,
    sortby character varying(255) NOT NULL,
    subscription_id uuid,
    CONSTRAINT t_segment_size_check CHECK ((size >= 1))
);


--
-- Name: t_source_subscription; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_source_subscription (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    archived boolean NOT NULL,
    description character varying(1024) NOT NULL,
    disabledfrom timestamp(6) without time zone,
    lastupdatedat timestamp(6) without time zone,
    ownerid uuid NOT NULL,
    plugins jsonb NOT NULL,
    product smallint NOT NULL,
    retentionmaxagedays integer,
    retentionmaxitems integer,
    schedulerexpression character varying(255) NOT NULL,
    segmentation_id uuid,
    tag character varying(255),
    title character varying(50) NOT NULL,
    triggerschedulednextat timestamp(6) without time zone,
    visibility character varying(50) NOT NULL
);


--
-- Name: t_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.t_user (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    acceptedtermsat timestamp(6) without time zone,
    anonymous boolean NOT NULL,
    dateformat character varying(255),
    email character varying(255),
    githubid character varying(255),
    hasacceptedterms boolean NOT NULL,
    hasvalidatedemail boolean NOT NULL,
    locked boolean NOT NULL,
    plan_id uuid,
    product smallint NOT NULL,
    purgescheduledfor timestamp(6) without time zone,
    root boolean NOT NULL,
    timeformat character varying(255),
    usesauthsource character varying(50) NOT NULL,
    validatedemailat timestamp(6) without time zone
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
    contenthtml text,
    contentraw bytea,
    contentrawmime character varying(50),
    contenttext text,
    contenttitle character varying(256),
    imageurl character varying(1000),
    releasedat timestamp(6) without time zone NOT NULL,
    score integer NOT NULL,
    startingat timestamp(6) without time zone,
    status character varying(50) NOT NULL,
    subscriptionid uuid NOT NULL,
    updatedat timestamp(6) without time zone NOT NULL,
    url character varying(1000) NOT NULL
);


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
-- Name: t_scrape_debug t_scrape_debug_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_scrape_debug
    ADD CONSTRAINT t_scrape_debug_pkey PRIMARY KEY (id);


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
-- Name: t_feature uniquefeatureperplan; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT uniquefeatureperplan UNIQUE (planid, name);


--
-- Name: t_plan uniqueplannameperproduct; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_plan
    ADD CONSTRAINT uniqueplannameperproduct UNIQUE (name, product);


--
-- Name: t_user uniqueuser; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_user
    ADD CONSTRAINT uniqueuser UNIQUE (email, product);


--
-- Name: idx_attachment_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_attachment_url ON public.t_attachment USING btree (url);


--
-- Name: idx_web_document_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_web_document_url ON public.t_web_document USING btree (url);


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
-- Name: t_feature fk_feature__plan; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.t_feature
    ADD CONSTRAINT fk_feature__plan FOREIGN KEY (planid) REFERENCES public.t_plan(id) ON DELETE CASCADE;


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
-- PostgreSQL database dump complete
--

