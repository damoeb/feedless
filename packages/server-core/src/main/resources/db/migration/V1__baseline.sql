--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3 (Debian 15.3-1.pgdg110+1)
-- Dumped by pg_dump version 16.2 (Ubuntu 16.2-1.pgdg22.04+1)


--
-- Name: t_agent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_agent (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    connection_id character varying(255) NOT NULL,
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

CREATE TABLE t_otp (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    password character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    valid_until timestamp(6) without time zone NOT NULL
);


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

CREATE TABLE t_repository (
    type character varying(31) NOT NULL,
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    is_archived boolean NOT NULL,
    description character varying(1024) NOT NULL,
    disabled_from timestamp(6) without time zone,
    document_count_since_creation integer,
    last_updated_at timestamp(6) without time zone,
    owner_id uuid NOT NULL,
    plugins jsonb NOT NULL,
    for_product smallint NOT NULL,
    retention_max_age_days integer,
    retention_max_items integer,
    segmentation_id uuid,
    scheduler_expression character varying(255) NOT NULL,
    sunset_after timestamp(6) without time zone,
    sunset_after_total_document_count integer,
    title character varying(50) NOT NULL,
    trigger_scheduled_next_at timestamp(6) without time zone,
    visibility character varying(50) NOT NULL
);


--
-- Name: t_scrape_source; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE t_scrape_source (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
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
    repository_id uuid NOT NULL,
    timeout integer,
    url character varying(255) NOT NULL,
    viewport jsonb,
    wait_until character varying(255)
);


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

CREATE TABLE t_user (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    approved_terms_at timestamp(6) without time zone,
    is_anonymous boolean NOT NULL,
    is_banned boolean NOT NULL,
    is_banned_until timestamp(6) without time zone,
    date_format character varying(255),
    email character varying(255),
    githubid character varying(255),
    hasapprovedterms boolean NOT NULL,
    has_validated_email boolean NOT NULL,
    karma integer NOT NULL,
    last_login timestamp(6) without time zone NOT NULL,
    is_locked boolean NOT NULL,
    plan_id uuid,
    product smallint NOT NULL,
    purge_scheduled_for timestamp(6) without time zone,
    is_root boolean NOT NULL,
    is_shaddow_banned boolean NOT NULL,
    is_spamming_submissions boolean NOT NULL,
    is_spamming_votes boolean NOT NULL,
    time_format character varying(255),
    usesauthsource character varying(50) NOT NULL,
    validated_email_at timestamp(6) without time zone
);


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

CREATE TABLE t_user_secret (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    lastusedat timestamp(6) without time zone,
    owner_id uuid NOT NULL,
    type character varying(50) NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL,
    value character varying(400) NOT NULL
);


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

ALTER TABLE ONLY t_otp
    ADD CONSTRAINT t_otp_pkey PRIMARY KEY (id);


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

ALTER TABLE ONLY t_repository
    ADD CONSTRAINT t_repository_pkey PRIMARY KEY (id);


--
-- Name: t_scrape_source t_scrape_source_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_scrape_source
    ADD CONSTRAINT t_scrape_source_pkey PRIMARY KEY (id);


--
-- Name: t_segment t_segment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_segment
    ADD CONSTRAINT t_segment_pkey PRIMARY KEY (id);


--
-- Name: t_user t_user_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT t_user_pkey PRIMARY KEY (id);


--
-- Name: t_user_plan_subscription t_user_plan_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT t_user_plan_subscription_pkey PRIMARY KEY (id);


--
-- Name: t_user_secret t_user_secret_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_secret
    ADD CONSTRAINT t_user_secret_pkey PRIMARY KEY (id);


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

ALTER TABLE ONLY t_user
    ADD CONSTRAINT uniqueuser UNIQUE (email, product);


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
-- Name: t_otp fk3s2gywtxnvtcjvbkh52jyawnc; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_otp
    ADD CONSTRAINT fk3s2gywtxnvtcjvbkh52jyawnc FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE;


--
-- Name: t_user_secret fk4evcslbhw4nofy5xsl2yyqxjk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY t_user_secret
    ADD CONSTRAINT fk4evcslbhw4nofy5xsl2yyqxjk FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


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

ALTER TABLE ONLY t_repository
    ADD CONSTRAINT fkcxaqh74vklfwnfrxaa0jmix4i FOREIGN KEY (owner_id) REFERENCES t_user(id) ON DELETE CASCADE;


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

ALTER TABLE ONLY t_scrape_source
    ADD CONSTRAINT fkpdn9867c5spvfb785hkvuh71o FOREIGN KEY (repository_id) REFERENCES t_repository(id) ON DELETE CASCADE;


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


--
-- PostgreSQL database dump complete
--

