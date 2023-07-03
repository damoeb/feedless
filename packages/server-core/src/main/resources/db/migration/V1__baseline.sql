--
-- Name: map_plan_to_feature; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE map_plan_to_feature (
    plan_id uuid NOT NULL,
    feature_id uuid NOT NULL
);


ALTER TABLE map_plan_to_feature OWNER TO postgres;

--
-- Name: t_article; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_article (
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


ALTER TABLE t_article OWNER TO postgres;

--
-- Name: t_bucket; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_bucket (
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


ALTER TABLE t_bucket OWNER TO postgres;

--
-- Name: t_feature; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_feature (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    name character varying(255) NOT NULL,
    plan_id uuid,
    state character varying(255) NOT NULL,
    valueboolean boolean,
    valueint integer,
    valuetype character varying(255) NOT NULL
);


ALTER TABLE t_feature OWNER TO postgres;

--
-- Name: t_feed_generic; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_feed_generic (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    feedspecification jsonb NOT NULL,
    websiteurl character varying(255) NOT NULL,
    nativefeed_id uuid
);


ALTER TABLE t_feed_generic OWNER TO postgres;

--
-- Name: t_feed_native; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_feed_native (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    description character varying(1024),
    domain character varying(255),
    failedattemptcount integer NOT NULL,
    feedurl character varying(1000) NOT NULL,
    harvestintervalminutes integer,
    harvestitems boolean NOT NULL,
    harvestsitewithprerender boolean NOT NULL,
    iconurl character varying(255),
    imageurl character varying(255),
    inlineimages boolean NOT NULL,
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
    generic_feed_id uuid
);


ALTER TABLE t_feed_native OWNER TO postgres;

--
-- Name: t_hyperlink; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_hyperlink (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    fromid uuid NOT NULL,
    hypertext character varying(256) NOT NULL,
    relevance double precision NOT NULL,
    toid uuid NOT NULL
);


ALTER TABLE t_hyperlink OWNER TO postgres;

--
-- Name: t_importer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_importer (
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
    triggerschedulednextat timestamp(6) without time zone
);


ALTER TABLE t_importer OWNER TO postgres;

--
-- Name: t_otp; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_otp (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    password character varying(255) NOT NULL,
    userid uuid NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL
);


ALTER TABLE t_otp OWNER TO postgres;

--
-- Name: t_plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_plan (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    availability character varying(255) NOT NULL,
    costs double precision NOT NULL,
    name character varying(255) NOT NULL,
    is_primary boolean NOT NULL
);


ALTER TABLE t_plan OWNER TO postgres;

--
-- Name: t_stream; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_stream (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL
);


ALTER TABLE t_stream OWNER TO postgres;

--
-- Name: t_user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_user (
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
    time_format character varying(255)
);


ALTER TABLE t_user OWNER TO postgres;

--
-- Name: t_user_plan_subscription; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_user_plan_subscription (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    paiduntil timestamp(6) without time zone,
    plan_id uuid NOT NULL,
    recurring boolean NOT NULL,
    startedat timestamp(6) without time zone,
    user_id uuid NOT NULL
);


ALTER TABLE t_user_plan_subscription OWNER TO postgres;

--
-- Name: t_user_secret; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_user_secret (
    id uuid NOT NULL,
    createdat timestamp(6) without time zone NOT NULL,
    lastusedat timestamp(6) without time zone,
    owner_id uuid NOT NULL,
    type character varying(255) NOT NULL,
    validuntil timestamp(6) without time zone NOT NULL,
    value character varying(400) NOT NULL
);


ALTER TABLE t_user_secret OWNER TO postgres;

--
-- Name: t_web_document; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE t_web_document (
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
    plugins jsonb NOT NULL,
    pluginscooldownuntil timestamp(6) without time zone,
    releasedat timestamp(6) without time zone NOT NULL,
    score integer NOT NULL,
    startingat timestamp(6) without time zone,
    title character varying(256),
    updatedat timestamp(6) without time zone NOT NULL,
    url character varying(1000) NOT NULL
);


ALTER TABLE t_web_document OWNER TO postgres;

--
-- Data for Name: t_feature; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY t_feature (id, createdat, name, plan_id, state, valueboolean, valueint, valuetype) FROM stdin;
a961a8e7-919c-4484-af6e-452bb6841d73	2023-05-09 20:21:12.009	rateLimit	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	\N	40	number
91b5a955-e2f5-417c-a7e2-eda1e85bf1ab	2023-05-09 20:21:12.01	rateLimit	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	\N	120	number
d0c5f08c-3ac5-4466-910c-e9353d8b05fb	2023-05-09 20:21:12.01	feedsMaxRefreshRate	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	\N	120	number
33822272-65cc-4472-8665-c3078f961d2f	2023-05-09 20:21:12.01	feedsMaxRefreshRate	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	\N	10	number
895d8106-79eb-495a-afce-034987717e82	2023-05-09 20:21:12.01	bucketsMaxCount	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	\N	3	number
d1b87a8e-8222-4023-a556-53011feeff01	2023-05-09 20:21:12.01	bucketsMaxCount	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	\N	100	number
70ae5d76-060e-4519-b57e-24700ee3494b	2023-05-09 20:21:12.01	feedsMaxCount	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	\N	30	number
04bfd145-806e-4854-9534-e414f8fe3bc2	2023-05-09 20:21:12.01	feedsMaxCount	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	\N	1000	number
bdca2c7b-6c87-4fbc-b7b8-10541802f7ef	2023-05-09 20:21:12.01	itemsRetention	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	\N	400	number
c9948d6c-ca4b-4689-a351-96fc8c2aa6a4	2023-05-09 20:21:12.01	itemsRetention	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	\N	10000	number
c7f3012f-b88c-4f19-a828-e7de33f58e75	2023-05-09 20:21:12.01	bucketsAccessOther	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	t	\N	bool
2ec2098c-6720-49b4-8e68-aaceb90aba86	2023-05-09 20:21:12.011	bucketsAccessOther	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
e7756052-18bd-4de0-8052-9768d9e56313	2023-05-09 20:21:12.011	notifications	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	t	\N	bool
35dc46f6-b75e-44eb-b0a9-2e5a29ce996d	2023-05-09 20:21:12.011	notifications	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
1ecd3c6e-aa70-43fb-9de9-64fd9dc92155	2023-05-09 20:21:12.011	genFeedFromWebsite	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	t	\N	bool
f5b39fad-9b49-4541-b01a-e19387deee8b	2023-05-09 20:21:12.011	genFeedFromWebsite	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
1b5eca07-2ff6-470d-83ff-6433f51c23f0	2023-05-09 20:21:12.011	genFeedFromFeed	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	t	\N	bool
0a73d7a7-7c70-4a97-a1ea-48ddc637dc25	2023-05-09 20:21:12.011	genFeedFromFeed	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
f80fb70e-3b90-46c9-8140-e0825cde3f6d	2023-05-09 20:21:12.011	genFeedFromPageChange	0df4ed43-dd1e-416e-b87b-0263cafeda02	experimental	t	\N	bool
6de5f128-da50-4868-ba85-ba67c03a01c1	2023-05-09 20:21:12.011	genFeedFromPageChange	ef6c4b90-b6a6-4a43-b745-966e9da00159	experimental	t	\N	bool
b063f4c0-e54b-453d-8191-382619816e84	2023-05-09 20:21:12.011	genFeedWithPrerender	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	t	\N	bool
28861b74-30cb-4db1-8722-bda98165fd20	2023-05-09 20:21:12.011	genFeedWithPrerender	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
77017e24-cff6-4859-a7de-4ce058daf0bf	2023-05-09 20:21:12.011	genFeedWithPuppeteerScript	0df4ed43-dd1e-416e-b87b-0263cafeda02	experimental	f	\N	bool
f22768b3-2b4f-4a28-b5d2-03e88fe516be	2023-05-09 20:21:12.011	genFeedWithPuppeteerScript	ef6c4b90-b6a6-4a43-b745-966e9da00159	experimental	t	\N	bool
2d34a40a-80c6-4bdc-811d-3432a0626f8d	2023-05-09 20:21:12.011	feedAuthentication	0df4ed43-dd1e-416e-b87b-0263cafeda02	experimental	f	\N	bool
da8b1c8d-b4a4-40a6-a078-5cc4f4ad17a5	2023-05-09 20:21:12.011	feedAuthentication	ef6c4b90-b6a6-4a43-b745-966e9da00159	experimental	t	\N	bool
05030b25-8686-4920-ad86-12db3c2eb7c7	2023-05-09 20:21:12.011	feedsPrivateAccess	0df4ed43-dd1e-416e-b87b-0263cafeda02	beta	f	\N	bool
0e727bda-bd00-4cdb-93e4-c48e0b0b446d	2023-05-09 20:21:12.011	feedsPrivateAccess	ef6c4b90-b6a6-4a43-b745-966e9da00159	beta	t	\N	bool
36045e64-c7f2-413b-8590-5622e3739d8d	2023-05-09 20:21:12.011	bucketsPrivateAccess	0df4ed43-dd1e-416e-b87b-0263cafeda02	beta	f	\N	bool
791983d6-062d-4245-a0bf-7d9ed0b98a0c	2023-05-09 20:21:12.011	bucketsPrivateAccess	ef6c4b90-b6a6-4a43-b745-966e9da00159	beta	t	\N	bool
1e3fefb1-4c33-4eb7-a7f3-db81c52d36bb	2023-05-09 20:21:12.011	feedsFulltext	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	f	\N	bool
ca2309c2-bcbd-45c4-b8af-a67fa5ab99a6	2023-05-09 20:21:12.011	feedsFulltext	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
2b1b5c4d-fb95-4dd9-98b3-c6207f2e3ec7	2023-05-09 20:21:12.011	itemsInlineImages	0df4ed43-dd1e-416e-b87b-0263cafeda02	stable	f	\N	bool
189ec16c-5dc0-4f5c-bb6d-59355544fd69	2023-05-09 20:21:12.011	itemsInlineImages	ef6c4b90-b6a6-4a43-b745-966e9da00159	stable	t	\N	bool
cf0cf9a6-b55a-479d-8cf8-d516b2d4829d	2023-05-09 20:21:12.011	itemsNoUrlShortener	0df4ed43-dd1e-416e-b87b-0263cafeda02	experimental	t	\N	bool
5cd0a1b3-4d41-4e8f-bca4-c5b8754d9c24	2023-05-09 20:21:12.011	itemsNoUrlShortener	ef6c4b90-b6a6-4a43-b745-966e9da00159	experimental	t	\N	bool
ec0330e7-39de-43f4-ab61-acca283d6be7	2023-05-09 20:21:12.011	api	0df4ed43-dd1e-416e-b87b-0263cafeda02	off	f	\N	bool
d70dc276-877b-4de3-8724-7fee1f918377	2023-05-09 20:21:12.011	api	ef6c4b90-b6a6-4a43-b745-966e9da00159	off	t	\N	bool
70f75af8-4427-431d-9066-754341f825ce	2023-05-09 20:21:12.011	itemEmailForward	0df4ed43-dd1e-416e-b87b-0263cafeda02	off	f	\N	bool
b713c468-72d6-4e1a-bfb5-efbce4a4f2a9	2023-05-09 20:21:12.011	itemEmailForward	ef6c4b90-b6a6-4a43-b745-966e9da00159	off	t	\N	bool
5f210107-c0b4-48b2-ac6f-b712d664509d	2023-05-09 20:21:12.011	itemWebhookForward	0df4ed43-dd1e-416e-b87b-0263cafeda02	off	t	\N	bool
2a1c35c7-8246-4e20-9947-3b6319889d93	2023-05-09 20:21:12.011	itemWebhookForward	ef6c4b90-b6a6-4a43-b745-966e9da00159	off	t	\N	bool
\.

--
-- Data for Name: t_plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY t_plan (id, createdat, availability, costs, name, is_primary) FROM stdin;
0df4ed43-dd1e-416e-b87b-0263cafeda02	2023-05-09 20:21:11.694	available	0	free	t
ef6c4b90-b6a6-4a43-b745-966e9da00159	2023-05-09 20:21:12.002	by_request	9.99	basic	f
\.


--
-- Name: t_article t_article_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_article
    ADD CONSTRAINT t_article_pkey PRIMARY KEY (id);


--
-- Name: t_bucket t_bucket_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_bucket
    ADD CONSTRAINT t_bucket_pkey PRIMARY KEY (id);


--
-- Name: t_feature t_feature_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feature
    ADD CONSTRAINT t_feature_pkey PRIMARY KEY (id);


--
-- Name: t_feed_generic t_feed_generic_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_generic
    ADD CONSTRAINT t_feed_generic_pkey PRIMARY KEY (id);


--
-- Name: t_feed_native t_feed_native_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT t_feed_native_pkey PRIMARY KEY (id);


--
-- Name: t_hyperlink t_hyperlink_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_hyperlink
    ADD CONSTRAINT t_hyperlink_pkey PRIMARY KEY (id);


--
-- Name: t_importer t_importer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_importer
    ADD CONSTRAINT t_importer_pkey PRIMARY KEY (id);


--
-- Name: t_otp t_otp_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_otp
    ADD CONSTRAINT t_otp_pkey PRIMARY KEY (id);


--
-- Name: t_plan t_plan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT t_plan_pkey PRIMARY KEY (id);


--
-- Name: t_stream t_stream_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_stream
    ADD CONSTRAINT t_stream_pkey PRIMARY KEY (id);


--
-- Name: t_user t_user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT t_user_pkey PRIMARY KEY (id);


--
-- Name: t_user_plan_subscription t_user_plan_subscription_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT t_user_plan_subscription_pkey PRIMARY KEY (id);


--
-- Name: t_user_secret t_user_secret_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user_secret
    ADD CONSTRAINT t_user_secret_pkey PRIMARY KEY (id);


--
-- Name: t_web_document t_web_document_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_web_document
    ADD CONSTRAINT t_web_document_pkey PRIMARY KEY (id);


--
-- Name: t_feed_native uk_6v95mi2ep5qw29314u15vwsj3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT uk_6v95mi2ep5qw29314u15vwsj3 UNIQUE (feedurl);


--
-- Name: t_plan uk_hslenih4o8iw4gbho72545mf3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_plan
    ADD CONSTRAINT uk_hslenih4o8iw4gbho72545mf3 UNIQUE (name);


--
-- Name: t_user uk_i6qjjoe560mee5ajdg7v1o6mi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT uk_i6qjjoe560mee5ajdg7v1o6mi UNIQUE (email);


--
-- Name: t_importer ukknq9ceicvfc210mced7ncsuie; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_importer
    ADD CONSTRAINT ukknq9ceicvfc210mced7ncsuie UNIQUE (bucketid, feedid);


--
-- Name: t_feed_native uniqueownerandfeed; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT uniqueownerandfeed UNIQUE (ownerid, feedurl);


--
-- Name: idx_hyperlink_url; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_hyperlink_url ON t_hyperlink USING btree (fromid);


--
-- Name: idx_web_document_url; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_web_document_url ON t_web_document USING btree (url);


--
-- Name: t_bucket fk157cu4wjd97imxmg4ns0x3x85; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_bucket
    ADD CONSTRAINT fk157cu4wjd97imxmg4ns0x3x85 FOREIGN KEY (streamid) REFERENCES t_stream(id);


--
-- Name: t_feed_generic fk2ft85wtvm6ivw0vxhteihm0rw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_generic
    ADD CONSTRAINT fk2ft85wtvm6ivw0vxhteihm0rw FOREIGN KEY (nativefeed_id) REFERENCES t_feed_native(id);


--
-- Name: t_otp fk36b6qk1g90ucc651dole1w4et; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_otp
    ADD CONSTRAINT fk36b6qk1g90ucc651dole1w4et FOREIGN KEY (userid) REFERENCES t_user(id);


--
-- Name: t_user_secret fk4evcslbhw4nofy5xsl2yyqxjk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user_secret
    ADD CONSTRAINT fk4evcslbhw4nofy5xsl2yyqxjk FOREIGN KEY (owner_id) REFERENCES t_user(id);


--
-- Name: t_importer fk4u7buardohn32sp7yocqn59ke; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_importer
    ADD CONSTRAINT fk4u7buardohn32sp7yocqn59ke FOREIGN KEY (ownerid) REFERENCES t_user(id);


--
-- Name: t_article fk5a2eyi09un1yg3xls70asf5va; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_article
    ADD CONSTRAINT fk5a2eyi09un1yg3xls70asf5va FOREIGN KEY (webdocumentid) REFERENCES t_web_document(id);


--
-- Name: t_user fk5t89sip0n4g578g4yxaugjc1h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT fk5t89sip0n4g578g4yxaugjc1h FOREIGN KEY (notifications_stream_id) REFERENCES t_stream(id);


--
-- Name: t_user_plan_subscription fk8ncewprxstppim67lx6e7ysn4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT fk8ncewprxstppim67lx6e7ysn4 FOREIGN KEY (plan_id) REFERENCES t_plan(id);


--
-- Name: t_user_plan_subscription fk9fe7lce4xahbf06k9eiwgy02h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user_plan_subscription
    ADD CONSTRAINT fk9fe7lce4xahbf06k9eiwgy02h FOREIGN KEY (user_id) REFERENCES t_user(id);


--
-- Name: map_plan_to_feature fkavs1bn9cp7ur1t2xqguf12b1t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY map_plan_to_feature
    ADD CONSTRAINT fkavs1bn9cp7ur1t2xqguf12b1t FOREIGN KEY (feature_id) REFERENCES t_feature(id);


--
-- Name: t_user fkciy9noxqwbr96ybkp5fv2c2ty; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_user
    ADD CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2ty FOREIGN KEY (plan_id) REFERENCES t_plan(id);


--
-- Name: t_article fkdh0dxm4bk8ahvk23dnfc40u8y; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_article
    ADD CONSTRAINT fkdh0dxm4bk8ahvk23dnfc40u8y FOREIGN KEY (streamid) REFERENCES t_stream(id);


--
-- Name: t_importer fkdunmc6oa4jocbgv4q4jyh4xny; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_importer
    ADD CONSTRAINT fkdunmc6oa4jocbgv4q4jyh4xny FOREIGN KEY (bucketid) REFERENCES t_bucket(id);


--
-- Name: t_article fkdxiy9chl8ghd9848def9b8o4o; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_article
    ADD CONSTRAINT fkdxiy9chl8ghd9848def9b8o4o FOREIGN KEY (importerid) REFERENCES t_importer(id);


--
-- Name: t_hyperlink fkgd7hbkkhrefq82vngp00so06d; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_hyperlink
    ADD CONSTRAINT fkgd7hbkkhrefq82vngp00so06d FOREIGN KEY (fromid) REFERENCES t_web_document(id);


--
-- Name: t_bucket fkhr17e39pk9333v21m3ha23ggl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_bucket
    ADD CONSTRAINT fkhr17e39pk9333v21m3ha23ggl FOREIGN KEY (ownerid) REFERENCES t_user(id);


--
-- Name: t_hyperlink fkixc22j33g4ov6eskdww7884ci; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_hyperlink
    ADD CONSTRAINT fkixc22j33g4ov6eskdww7884ci FOREIGN KEY (toid) REFERENCES t_web_document(id);


--
-- Name: t_feature fkj9etkvylxqaj7ee5e840nphcc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feature
    ADD CONSTRAINT fkj9etkvylxqaj7ee5e840nphcc FOREIGN KEY (plan_id) REFERENCES t_plan(id);


--
-- Name: t_importer fkmfgv3wp5008are0gbk2il2blp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_importer
    ADD CONSTRAINT fkmfgv3wp5008are0gbk2il2blp FOREIGN KEY (feedid) REFERENCES t_feed_native(id);


--
-- Name: t_feed_native fkn9kfg8o3jfarh3kqa960w7t3q; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT fkn9kfg8o3jfarh3kqa960w7t3q FOREIGN KEY (generic_feed_id) REFERENCES t_feed_generic(id);


--
-- Name: t_feed_native fknwf9rwa6eoqe5p08mo6uxkgwn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT fknwf9rwa6eoqe5p08mo6uxkgwn FOREIGN KEY (streamid) REFERENCES t_stream(id);


--
-- Name: t_feed_native fkpadc1pn1ghj7c1qfnsau0b1ml; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_feed_native
    ADD CONSTRAINT fkpadc1pn1ghj7c1qfnsau0b1ml FOREIGN KEY (ownerid) REFERENCES t_user(id);


--
-- Name: t_article fkq3lxplias2mkt5aqmxc1d9kiv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY t_article
    ADD CONSTRAINT fkq3lxplias2mkt5aqmxc1d9kiv FOREIGN KEY (ownerid) REFERENCES t_user(id);


--
-- Name: map_plan_to_feature fksc26dpu29xdimt1d0ejr4n3oa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY map_plan_to_feature
    ADD CONSTRAINT fksc26dpu29xdimt1d0ejr4n3oa FOREIGN KEY (plan_id) REFERENCES t_plan(id);


--
-- PostgreSQL database dump complete
--

