package org.migor.rich.rss.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.migor.rich.rss.data.jpa.EntityWithUUID

enum class FeatureName {
  database,
  elasticsearch,
  puppeteer,
  authentication,
  authenticated,
  authAllowRoot,
  authSSO,
  authMail,

  rateLimit,
  notifications,
  feedsMaxRefreshRate,
  bucketsMaxCount,
  bucketsAccessOther,
  feedsMaxCount,
  feedsFulltext,
  itemsInlineImages,

  genFeedFromWebsite,
  genFeedFromFeed,
  genFeedFromPageChange,
  genFeedWithPrerender,
  genFeedWithPuppeteerScript,

  itemsNoUrlShortener,
  itemsRetention,
  feedsPrivateAccess,
  bucketsPrivateAccess,
  feedAuthentication,
  itemEmailForward,
  itemWebhookForward,
  api
}


@Entity
@Table(name = "t_feature")
open class FeatureEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  open lateinit var name: FeatureName

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var state: FeatureState = FeatureState.off

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  open lateinit var planFeatureConfigs: List<PlanFeatureConfig>
}

data class PlanFeatureConfig (
  val name: PlanName,
  val valueInt: Int?,
  val valueBoolean: Boolean?,
  var valueType: FeatureValueType,
)
