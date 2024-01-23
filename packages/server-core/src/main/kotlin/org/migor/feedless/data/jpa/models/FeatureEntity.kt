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
import org.migor.feedless.generated.types.FeatureName as FeatureNameDto
import java.util.*

enum class FeatureName {
  // internal
  database,
  authentication,
  authSSO,
  authMail,

  rateLimit,
  minRefreshRateInMinutes,
  publicScrapeSource,
  plugins,

  scrapeRequestTimeout,
  scrapeSourceRetentionMaxItems,
  itemEmailForward,
  itemWebhookForward,
  api,
  scrapeSourceExpiryInDays,
  scrapeSourceMaxCountActive,
  scrapeRequestMaxCountPerSource,
  scrapeRequestActionMaxCount,
  scrapeSourceMaxCountTotal,
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
  @Column(name = StandardJpaFields.planId)
  open lateinit var planId: UUID

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
  @JoinColumn(name = StandardJpaFields.planId, referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_feature__plan"), insertable = false, updatable = false)
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

private fun FeatureName.toDto(): FeatureNameDto {
  return when(this) {
    FeatureName.database -> FeatureNameDto.database
    FeatureName.plugins -> FeatureNameDto.plugins
    FeatureName.authentication -> FeatureNameDto.authentication
    FeatureName.authSSO -> FeatureNameDto.authSSO
    FeatureName.authMail -> FeatureNameDto.authMail
    FeatureName.rateLimit -> FeatureNameDto.rateLimit
    FeatureName.minRefreshRateInMinutes -> FeatureNameDto.minRefreshRateInMinutes
    FeatureName.publicScrapeSource -> FeatureNameDto.publicScrapeSource
    FeatureName.scrapeRequestTimeout -> FeatureNameDto.scrapeRequestTimeout
    FeatureName.scrapeSourceRetentionMaxItems -> FeatureNameDto.scrapeSourceRetentionMaxItems
    FeatureName.itemEmailForward -> FeatureNameDto.itemEmailForward
    FeatureName.itemWebhookForward -> FeatureNameDto.itemWebhookForward
    FeatureName.scrapeSourceExpiryInDays -> FeatureNameDto.scrapeSourceExpiryInDays
    FeatureName.scrapeSourceMaxCountActive -> FeatureNameDto.scrapeSourceMaxCountActive
    FeatureName.scrapeRequestMaxCountPerSource -> FeatureNameDto.scrapeRequestMaxCountPerSource
    FeatureName.scrapeRequestActionMaxCount -> FeatureNameDto.scrapeRequestActionMaxCount
    FeatureName.scrapeSourceMaxCountTotal -> FeatureNameDto.scrapeSourceMaxCountTotal
    FeatureName.api -> FeatureNameDto.api

  }
}
