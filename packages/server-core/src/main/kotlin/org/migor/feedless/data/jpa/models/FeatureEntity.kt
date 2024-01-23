package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue
import java.util.*

enum class FeatureName {
  database,
  authentication,
  authenticated,
  authRoot,
  authSSO,
  authMail,

  rateLimit,
  notifications,
  minRefreshRateInMinutes,
  publicScrapeSource,
  itemsInlineImages,

  scrapeRequestTimeout,
  itemsRetention,
  itemEmailForward,
  itemWebhookForward,
  api,
  scrapeSourceExpiryInDays
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

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "plan_id", referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_feature__plan"))
  open var plan: PlanEntity? = null
}

fun FeatureEntity.toDto(): Feature {
  val value = FeatureValue.newBuilder()
  if(this.valueType == FeatureValueType.number) {
    value.numVal(
      FeatureIntValue.newBuilder()
        .value(this.valueInt!!)
        .build())
  } else {
    value.boolVal(
      FeatureBooleanValue.newBuilder()
        .value(this.valueBoolean!!)
        .build())
  }

  return Feature.newBuilder()
    .state(this.state.toDto())
    .name(this.name.toDto())
    .value(value.build())
    .build()
}

private fun FeatureName.toDto(): org.migor.feedless.generated.types.FeatureName {
  return org.migor.feedless.generated.types.FeatureName.valueOf(this.name)
}
