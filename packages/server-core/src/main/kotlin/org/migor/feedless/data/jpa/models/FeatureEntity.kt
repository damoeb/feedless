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
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue
import java.util.*
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto

enum class FeatureName {
  // internal
  database,
  authentication,
  authSSO,
  authMail,

  rateLimitInt,
  minRefreshRateInMinutesInt,
  publicScrapeSourceBool,
  pluginsBool,

  scrapeRequestTimeoutInt,
  scrapeSourceRetentionMaxItemsInt,
  itemEmailForwardBool,
  itemWebhookForwardBool,
  apiBool,
  scrapeSourceExpiryInDaysInt,
  scrapeSourceMaxCountActiveInt,
  scrapeRequestMaxCountPerSourceInt,
  scrapeRequestActionMaxCountInt,
  scrapeSourceMaxCountTotalInt,
}


@Entity
@Table(name = "t_feature",   uniqueConstraints = [
  UniqueConstraint(name = "UniqueFeaturePerPlan", columnNames = [StandardJpaFields.planId, StandardJpaFields.name])]
)
open class FeatureEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, name = StandardJpaFields.name)
  @Enumerated(EnumType.STRING)
  open lateinit var name: FeatureName

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var state: FeatureState = FeatureState.off

  @Basic
  open var valueInt: Int? = null

  @Basic
  open var valueBoolean: Boolean? = null

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var valueType: FeatureValueType

  @Basic
  @Column(name = StandardJpaFields.planId, nullable = false)
  open lateinit var planId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = StandardJpaFields.planId, referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_feature__plan"), insertable = false, updatable = false)
  open var plan: PlanEntity? = null
}

fun FeatureEntity.toDto(): Feature {
  val value = FeatureValue.newBuilder()
  if(valueType == FeatureValueType.number) {
    value.numVal(
      FeatureIntValue.newBuilder()
        .value(valueInt!!)
        .build())
  } else {
    value.boolVal(
      FeatureBooleanValue.newBuilder()
        .value(valueBoolean!!)
        .build())
  }

  return Feature.newBuilder()
    .state(state.toDto())
    .name(name.toDto())
    .value(value.build())
    .build()
}

private fun FeatureName.toDto(): FeatureNameDto {
  return when(this) {
    FeatureName.database -> FeatureNameDto.database
    FeatureName.pluginsBool -> FeatureNameDto.plugins
    FeatureName.authentication -> FeatureNameDto.authentication
    FeatureName.authSSO -> FeatureNameDto.authSSO
    FeatureName.authMail -> FeatureNameDto.authMail
    FeatureName.rateLimitInt -> FeatureNameDto.rateLimit
    FeatureName.minRefreshRateInMinutesInt -> FeatureNameDto.minRefreshRateInMinutes
    FeatureName.publicScrapeSourceBool -> FeatureNameDto.publicScrapeSource
    FeatureName.scrapeRequestTimeoutInt -> FeatureNameDto.scrapeRequestTimeout
    FeatureName.scrapeSourceRetentionMaxItemsInt -> FeatureNameDto.scrapeSourceRetentionMaxItems
    FeatureName.itemEmailForwardBool -> FeatureNameDto.itemEmailForward
    FeatureName.itemWebhookForwardBool -> FeatureNameDto.itemWebhookForward
    FeatureName.scrapeSourceExpiryInDaysInt -> FeatureNameDto.scrapeSourceExpiryInDays
    FeatureName.scrapeSourceMaxCountActiveInt -> FeatureNameDto.scrapeSourceMaxCountActive
    FeatureName.scrapeRequestMaxCountPerSourceInt -> FeatureNameDto.scrapeRequestMaxCountPerSource
    FeatureName.scrapeRequestActionMaxCountInt -> FeatureNameDto.scrapeRequestActionMaxCount
    FeatureName.scrapeSourceMaxCountTotalInt -> FeatureNameDto.scrapeSourceMaxCountTotal
    FeatureName.apiBool -> FeatureNameDto.api

  }
}
