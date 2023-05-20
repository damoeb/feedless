package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID
import java.util.*

enum class FeatureName {
  database,
  elasticsearch,
  puppeteer,
  authentication,
  authenticated,
  authRoot,
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
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var name: FeatureName

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var state: FeatureState = FeatureState.off

  @Basic
  @Column(name = "plan_id", insertable = false, updatable = false)
  open var planId: UUID? = null

  @Basic
  open var valueInt: Int? = null

  @Basic
  open var valueBoolean: Boolean? = null

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var valueType: FeatureValueType

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "plan_id", referencedColumnName = "id")
  open var plan: PlanEntity? = null
}
