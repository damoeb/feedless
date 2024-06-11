-- t_agent
ALTER TABLE IF EXISTS t_agent RENAME CONSTRAINT fk5emoamaua4ofq33mpdsxh4buo TO fk_agent__to__user;
ALTER TABLE IF EXISTS t_agent RENAME CONSTRAINT fkhb1tqh55xp1941nsgf6j4a2b8 TO fk_agent__to__secret;

-- t_annotation
ALTER TABLE IF EXISTS t_annotation RENAME CONSTRAINT fksbaduv38d2d4ew34q5rop3ibr TO fk_annotation__to__user;
ALTER TABLE IF EXISTS t_annotation RENAME CONSTRAINT fkkluvd7rpx2upem1423ynecjwn TO fk_annotation__to__document;

-- t_annotation_text
ALTER TABLE IF EXISTS t_annotation_text RENAME CONSTRAINT fkdu83owfk735ovcjk9mb9ngpdg TO fk_text_annotation__to__document;

-- t_attachment
ALTER TABLE IF EXISTS t_attachment RENAME CONSTRAINT fk6uwautfjtj7y4pl2hw01pq24y TO fk_attachment__to__document;

-- t_billing
ALTER TABLE IF EXISTS t_billing
  ALTER COLUMN callback_url SET NOT NULL;

ALTER TABLE IF EXISTS t_billing RENAME CONSTRAINT fkgq1aivcdo64mvli5dalusnysp TO fk_order__to__product;
ALTER TABLE IF EXISTS t_billing RENAME CONSTRAINT fkthobpg5fdar1v4u2lsgxegh5l TO fk_order__to__user;

ALTER TABLE IF EXISTS t_billing
  ADD CONSTRAINT t_billing_price_check CHECK (price >= 0::double precision);

-- t_browser_action
ALTER TABLE IF EXISTS t_browser_action RENAME CONSTRAINT fkr018iugrf2p492v4d199aty01 TO fk_source__to__browser_action;

-- t_cloud_subscription
ALTER TABLE IF EXISTS t_cloud_subscription RENAME CONSTRAINT fk8ncewprxstppim67lx6e7ysn4 TO fk_subscription__to__product;
ALTER TABLE IF EXISTS t_cloud_subscription RENAME CONSTRAINT fk9fe7lce4xahbf06k9eiwgy02h TO fk_subscription__to__user;

-- t_document
ALTER TABLE IF EXISTS t_document RENAME CONSTRAINT fkcteq09rhg8qhsxp17g1m6uyj6 TO fk_document__to__repository;
ALTER TABLE IF EXISTS t_document RENAME CONSTRAINT fkkwqbkpxrw6bibp88dqy6d4ueh TO fk_document__to__parent;

-- t_feature_group
ALTER TABLE IF EXISTS t_feature_group
  ALTER COLUMN name DROP DEFAULT;

ALTER TABLE IF EXISTS t_feature_group
  ALTER COLUMN name DROP NOT NULL;

ALTER TABLE IF EXISTS t_feature_group RENAME CONSTRAINT fkgxqjc0sh95jjftv8id9hqsvth TO fk_child__to__parent;

-- t_feature_value
ALTER TABLE IF EXISTS t_feature_value RENAME CONSTRAINT fke5debxvklvw4odvwapar7u1qg TO fk_feature_value__to__feature;
ALTER TABLE IF EXISTS t_feature_value RENAME CONSTRAINT fkh7n02k73onw5cw3afh0qvd1gp TO fk_feature_value__to__feature_group;

-- t_license
ALTER TABLE IF EXISTS t_license RENAME CONSTRAINT fkmj0osl4etytha4j8ytxake8jb TO fk_license__to__order;

-- t_mail_forward
ALTER TABLE IF EXISTS t_mail_forward RENAME CONSTRAINT fko85uxyf2hn04isnhnq8kd4grx TO fk_mail__to__repository;

-- t_notification
ALTER TABLE IF EXISTS t_notification RENAME CONSTRAINT fkboru2k9q1whuculc2axpggcpa TO fk_notification__to__user;

-- t_otp
ALTER TABLE IF EXISTS t_otp RENAME CONSTRAINT fk3s2gywtxnvtcjvbkh52jyawnc TO fk_otp__to__user;

-- t_pipeline_job
ALTER TABLE IF EXISTS t_pipeline_job
  ALTER COLUMN schema_version DROP DEFAULT;

ALTER TABLE IF EXISTS t_pipeline_job RENAME CONSTRAINT fkocn0ypekfntw4vsvr3f14r593 TO fk_pipeline_job__to__document;

-- t_priced_product
ALTER TABLE IF EXISTS t_priced_product
  ALTER COLUMN sold_unit SET NOT NULL;
ALTER TABLE IF EXISTS t_priced_product RENAME CONSTRAINT fkg7rortdb1i3uf68mltpr5c9ao TO fk_priced_product__to__product;

-- t_product
ALTER TABLE IF EXISTS t_product
  ALTER COLUMN description DROP DEFAULT;

ALTER TABLE IF EXISTS t_product
  ALTER COLUMN is_base_product DROP DEFAULT;

ALTER TABLE IF EXISTS t_product
  ALTER COLUMN is_cloud DROP DEFAULT;

ALTER TABLE IF EXISTS t_product RENAME CONSTRAINT fkpa5fq82wm7m4aphvboxo4a1on TO fk_product__to__feature_group;

-- t_repository
ALTER TABLE IF EXISTS t_repository
  ALTER COLUMN schema_version DROP DEFAULT;
ALTER TABLE IF EXISTS t_repository RENAME CONSTRAINT fk1bvl5tll4isnaxnyjqq1shtld TO fk_repository__to__segmentation;
ALTER TABLE IF EXISTS t_repository RENAME CONSTRAINT fkcxaqh74vklfwnfrxaa0jmix4i TO fk_repository__to__user;

-- t_source
ALTER TABLE IF EXISTS t_source
  ALTER COLUMN schema_version DROP DEFAULT;

ALTER TABLE IF EXISTS t_source RENAME CONSTRAINT t_scrape_source_pkey TO t_source_pkey;

ALTER TABLE IF EXISTS t_source RENAME CONSTRAINT fkpdn9867c5spvfb785hkvuh71o TO fk_source__to__repository;

-- t_user
ALTER TABLE IF EXISTS t_user
  ALTER COLUMN last_login DROP NOT NULL;

ALTER TABLE IF EXISTS t_user RENAME CONSTRAINT fkciy9noxqwbr96ybkp5fv2c2tz TO fk_user__to__subscription;

-- t_user_secret
ALTER TABLE IF EXISTS t_user_secret RENAME CONSTRAINT fk4evcslbhw4nofy5xsl2yyqxjk TO fk_user_secret__to__user;
