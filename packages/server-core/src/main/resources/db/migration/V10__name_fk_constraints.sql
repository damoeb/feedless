ALTER TABLE ONLY t_article
  DROP CONSTRAINT IF EXISTS fk5a2eyi09un1yg3xls70asf5va,
  DROP CONSTRAINT IF EXISTS fk_article__webdocument,
  ADD CONSTRAINT fk_article__webdocument
    FOREIGN KEY (webdocumentid)
      REFERENCES t_web_document(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fkdxiy9chl8ghd9848def9b8o4o,
  DROP CONSTRAINT IF EXISTS fk_article__stream,
  ADD CONSTRAINT fk_article__stream
    FOREIGN KEY (streamid)
      REFERENCES t_stream(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fkq3lxplias2mkt5aqmxc1d9kiv,
  DROP CONSTRAINT IF EXISTS fk_article__user,
  ADD CONSTRAINT fk_article__user
    FOREIGN KEY (ownerid)
      REFERENCES t_user(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_article__importer,
  ADD CONSTRAINT fk_article__importer
    FOREIGN KEY (importerid)
      REFERENCES t_importer(id)
      ON DELETE SET NULL
;

ALTER TABLE ONLY t_bucket
  DROP CONSTRAINT IF EXISTS fk157cu4wjd97imxmg4ns0x3x85,
  DROP CONSTRAINT IF EXISTS fk_bucket__stream,
  ADD CONSTRAINT fk_bucket__stream
    FOREIGN KEY (streamid)
      REFERENCES t_stream(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fkhr17e39pk9333v21m3ha23ggl,
  DROP CONSTRAINT IF EXISTS fk_bucket__user,
  ADD CONSTRAINT fk_bucket__user
    FOREIGN KEY (ownerid)
      REFERENCES t_user(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_user_secret
  DROP CONSTRAINT IF EXISTS fk4evcslbhw4nofy5xsl2yyqxjk,
  DROP CONSTRAINT IF EXISTS fk_user_secrets__user,
  ADD CONSTRAINT fk_user_secrets__user
    FOREIGN KEY (owner_id)
      REFERENCES t_user(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_feed_generic
  DROP CONSTRAINT IF EXISTS fk2ft85wtvm6ivw0vxhteihm0rw,
  DROP CONSTRAINT IF EXISTS fk_generic_feed__native__feed,
  ADD CONSTRAINT fk_generic_feed__native__feed
    FOREIGN KEY (nativefeed_id)
      REFERENCES t_feed_native(id)
;

ALTER TABLE ONLY t_user_plan_subscription
  DROP CONSTRAINT IF EXISTS fk8ncewprxstppim67lx6e7ysn4,
  DROP CONSTRAINT IF EXISTS fk9fe7lce4xahbf06k9eiwgy02h,
  DROP CONSTRAINT IF EXISTS fk_ups__user,
  ADD CONSTRAINT fk_ups__user
    FOREIGN KEY (user_id)
      REFERENCES t_user(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_ups__plan,
  ADD CONSTRAINT fk_ups__plan
    FOREIGN KEY (user_id)
      REFERENCES t_plan(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_user
  DROP CONSTRAINT IF EXISTS fk5t89sip0n4g578g4yxaugjc1h,
  DROP CONSTRAINT IF EXISTS fkciy9noxqwbr96ybkp5fv2c2ty,
  DROP CONSTRAINT IF EXISTS fk_user__stream,
  ADD CONSTRAINT fk_user__stream
    FOREIGN KEY (notifications_stream_id)
      REFERENCES t_stream(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_user__plan,
  ADD CONSTRAINT fk_user__plan
    FOREIGN KEY (plan_id)
      REFERENCES t_plan(id)
      ON DELETE SET NULL
;

ALTER TABLE ONLY t_otp
  DROP CONSTRAINT IF EXISTS fk36b6qk1g90ucc651dole1w4et,
  DROP CONSTRAINT IF EXISTS fk_otp__user,
  ADD CONSTRAINT fk_otp__user
    FOREIGN KEY (userId)
      REFERENCES t_user(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_feature
  DROP CONSTRAINT IF EXISTS fkj9etkvylxqaj7ee5e840nphcc,
  DROP CONSTRAINT IF EXISTS fk_feature__plan,
  ADD CONSTRAINT fk_feature__plan
    FOREIGN KEY (plan_id)
      REFERENCES t_plan(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_feed_native
  DROP CONSTRAINT IF EXISTS fkn9kfg8o3jfarh3kqa960w7t3q,
  DROP CONSTRAINT IF EXISTS fknwf9rwa6eoqe5p08mo6uxkgwn,
  DROP CONSTRAINT IF EXISTS fkpadc1pn1ghj7c1qfnsau0b1ml,
  DROP CONSTRAINT IF EXISTS fk_native_feed__user,
  ADD CONSTRAINT fk_native_feed__user
    FOREIGN KEY (ownerid)
      REFERENCES t_user(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_native_feed__generic_feed,
  ADD CONSTRAINT fk_native_feed__generic_feed
    FOREIGN KEY (generic_feed_id)
      REFERENCES t_feed_generic(id)
      ON DELETE SET NULL,
  DROP CONSTRAINT IF EXISTS fk_native_feed__stream,
  ADD CONSTRAINT fk_native_feed__stream
    FOREIGN KEY (streamId)
      REFERENCES t_stream(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_importer
  DROP CONSTRAINT IF EXISTS fk4u7buardohn32sp7yocqn59ke,
  DROP CONSTRAINT IF EXISTS fkdunmc6oa4jocbgv4q4jyh4xny,
  DROP CONSTRAINT IF EXISTS fkmfgv3wp5008are0gbk2il2blp,
  DROP CONSTRAINT IF EXISTS fk_importer__bucket,
  ADD CONSTRAINT fk_importer__bucket
    FOREIGN KEY (bucketid)
      REFERENCES t_bucket(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_importer__feed,
  ADD CONSTRAINT fk_importer__feed
    FOREIGN KEY (feedid)
      REFERENCES t_feed_native(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_importer__user,
  ADD CONSTRAINT fk_importer__user
    FOREIGN KEY (ownerid)
      REFERENCES t_user(id)
      ON DELETE CASCADE
;

ALTER TABLE ONLY t_hyperlink
  DROP CONSTRAINT IF EXISTS fkgd7hbkkhrefq82vngp00so06d,
  DROP CONSTRAINT IF EXISTS fkixc22j33g4ov6eskdww7884ci,
  DROP CONSTRAINT IF EXISTS fk_hyperlink__source_document,
  ADD CONSTRAINT fk_hyperlink__source_document
    FOREIGN KEY (fromid)
      REFERENCES t_web_document(id)
      ON DELETE CASCADE,
  DROP CONSTRAINT IF EXISTS fk_hyperlink__target_document,
  ADD CONSTRAINT fk_hyperlink__target_document
    FOREIGN KEY (toid)
      REFERENCES t_web_document(id)
      ON DELETE CASCADE
;
