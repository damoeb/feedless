package org.migor.feedless.plan

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
import org.migor.feedless.generated.types.FeatureBooleanValue
import org.migor.feedless.generated.types.FeatureIntValue
import org.migor.feedless.generated.types.FeatureValue
import java.util.*

enum class FeatureName {
  requestPerMinuteUpperLimitInt,
  refreshRateInMinutesLowerLimitInt,
  publicRepositoryBool,
  pluginsBool,
  legacyApiBool,

  repositoryCapacityUpperLimitInt,
  repositoryCapacityLowerLimitInt,
  repositoryRetentionMaxDaysLowerLimitInt,
  itemEmailForwardBool,
  itemWebhookForwardBool,
  canLogin,
  canActivatePlan,
  canCreateUser,
  canJoinPlanWaitList,
  canSignUp,

  scrapeRequestTimeoutMsecInt,
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
      columnNames = ["feature_group_id", StandardJpaFields.featureId]
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

  @Column(name = "feature_group_id", nullable = false)
  open lateinit var featureGroupId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "feature_group_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_feature_value__to__feature_group")
  )
  open var featureGroup: FeatureGroupEntity? = null

  @Column(name = StandardJpaFields.featureId, nullable = false)
  open lateinit var featureId: UUID

  @ManyToOne(fetch = FetchType.EAGER)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.featureId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_feature_value__to__feature")
  )
  open var feature: FeatureEntity? = null
}

fun FeatureValueEntity.toDto(): FeatureValue {
  val value = FeatureValue.newBuilder()
  value.id(id.toString())
  if (valueType == FeatureValueType.number) {
    value.numVal(
      FeatureIntValue.newBuilder()
        .value(valueInt ?: -1)
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
