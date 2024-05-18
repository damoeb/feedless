package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue
import java.util.*

enum class FeatureName {
  // internal
//  authSSO,
//  authMail,

  rateLimitInt,
  refreshRateInMinutesLowerLimitInt,
  publicRepositoryBool,
  pluginsBool,

  scrapeRequestTimeoutInt,
  repositoryRetentionMaxItemsUpperLimitInt,
  repositoryRetentionMaxItemsLowerLimitInt,
  repositoryRetentionMaxDaysLowerLimitInt,
  itemEmailForwardBool,
  itemWebhookForwardBool,
//  apiBool,
  canLogin,
  canCreateUser,
  canCreateAsAnonymous,
  hasWaitList,
  canSignUp,
  repositoryWhenAnonymousExpiryInDaysInt,
  scrapeSourceMaxCountActiveInt,
  scrapeRequestMaxCountPerSourceInt,
  scrapeRequestActionMaxCountInt,
  scrapeSourceMaxCountTotalInt,
}

@Entity
@Table(
  name = "t_feature_value", uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueFeaturePerPlan",
      columnNames = [StandardJpaFields.planId, StandardJpaFields.featureId]
    )]
)
open class FeatureValueEntity : EntityWithUUID() {

  @Column(name="value_int")
  open var valueInt: Int? = null

  @Column(name="value_bool")
  open var valueBoolean: Boolean? = null

  @Column(nullable = false, length = 50, name = "value_type")
  @Enumerated(EnumType.STRING)
  open lateinit var valueType: FeatureValueType

  @Column(name = StandardJpaFields.planId, nullable = false)
  open lateinit var planId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.planId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false
  )
  open var plan: PlanEntity? = null

  @Column(name = StandardJpaFields.featureId, nullable = false)
  open lateinit var featureId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.featureId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false
  )
  open var feature: FeatureEntity? = null
}

fun FeatureValueEntity.toDto(): FeatureValue {
  val value = FeatureValue.newBuilder()
  if (valueType == FeatureValueType.number) {
    value.numVal(
      FeatureIntValue.newBuilder()
        .value(valueInt!!)
        .build()
    )
  } else {
    value.boolVal(
      FeatureBooleanValue.newBuilder()
        .value(valueBoolean!!)
        .build()
    )
  }

  return value.build()
}

//fun FeatureName.toDto(): FeatureNameDto {
//  return when (this) {
//    FeatureName.pluginsBool -> FeatureNameDto.plugins
//    FeatureName.rateLimitInt -> FeatureNameDto.rateLimit
//    FeatureName.minRefreshRateInMinutesInt -> FeatureNameDto.minRefreshRateInMinutes
//    FeatureName.publicScrapeSourceBool -> FeatureNameDto.publicScrapeSource
//    FeatureName.scrapeRequestTimeoutInt -> FeatureNameDto.scrapeRequestTimeout
//    FeatureName.repositoryRetentionMaxItemsUpperLimitInt -> FeatureNameDto.scrapeSourceRetentionMaxItems
//    FeatureName.itemEmailForwardBool -> FeatureNameDto.itemEmailForward
//    FeatureName.itemWebhookForwardBool -> FeatureNameDto.itemWebhookForward
//    FeatureName.scrapeSourceExpiryInDaysInt -> FeatureNameDto.scrapeSourceExpiryInDays
//    FeatureName.scrapeSourceMaxCountActiveInt -> FeatureNameDto.scrapeSourceMaxCountActive
//    FeatureName.scrapeRequestMaxCountPerSourceInt -> FeatureNameDto.scrapeRequestMaxCountPerSource
//    FeatureName.scrapeRequestActionMaxCountInt -> FeatureNameDto.scrapeRequestActionMaxCount
//    FeatureName.scrapeSourceMaxCountTotalInt -> FeatureNameDto.scrapeSourceMaxCountTotal
//    FeatureName.apiBool -> FeatureNameDto.api
//    FeatureName.canLogin -> FeatureNameDto.canLogin
//    FeatureName.canCreateUser -> FeatureNameDto.canCreateUser
//    FeatureName.canSignUp -> FeatureNameDto.canSignUp
//    FeatureName.hasWaitList -> FeatureNameDto.hasWaitList
//    FeatureName.canCreateAsAnonymous -> FeatureNameDto.canCreateAsAnonymous
//  }
//}
