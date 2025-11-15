package org.migor.feedless.data.jpa.featureValue

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.feature.FeatureEntity
import org.migor.feedless.data.jpa.featureGroup.FeatureGroupEntity
import org.migor.feedless.feature.FeatureValueType
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
  repositoriesMaxCountActiveInt,
  sourceMaxCountPerRepositoryInt,

  @Deprecated("will be removed")
  scrapeRequestActionMaxCountInt,
  repositoriesMaxCountTotalInt,
}

@Entity
@Table(
  name = "t_feature_value",
  uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueFeaturePerPlan",
      columnNames = [StandardJpaFields.featureGroupId, StandardJpaFields.featureId]
    )
  ],
  indexes = [
    Index(name = "feature_group_id__idx", columnList = StandardJpaFields.featureGroupId),
    Index(name = "feature_id__idx", columnList = StandardJpaFields.featureId),
  ]
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
open class FeatureValueEntity : EntityWithUUID() {

  @Column(name = "value_int")
  open var valueInt: Long? = null

  @Column(name = "value_bool")
  open var valueBoolean: Boolean? = null

  @Column(nullable = false, length = 50, name = "value_type")
  @Enumerated(EnumType.STRING)
  open lateinit var valueType: FeatureValueType

  @Column(name = StandardJpaFields.featureGroupId, nullable = false)
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
