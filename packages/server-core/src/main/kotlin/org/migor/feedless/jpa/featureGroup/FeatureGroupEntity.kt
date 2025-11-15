package org.migor.feedless.jpa.featureGroup

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.Feature
import org.migor.feedless.generated.types.FeatureGroup
import org.migor.feedless.jpa.featureValue.FeatureValueEntity
import org.migor.feedless.jpa.product.ProductEntity
import java.util.*

enum class PlanName {
  system,
  free,
  basic,
}


@Entity
@Table(
  name = "t_feature_group",
  uniqueConstraints = [
    UniqueConstraint(name = "uniquename", columnNames = [StandardJpaFields.name])]
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
open class FeatureGroupEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.name, nullable = true)
  open lateinit var name: String

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var features: MutableList<FeatureValueEntity> = mutableListOf()

  @Column(name = "parent_feature_group_id", nullable = true)
  open var parentFeatureGroupId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "parent_feature_group_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_child__to__parent")
  )
  open var parentFeatureGroup: FeatureGroupEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "featureGroupId")
  @OnDelete(action = OnDeleteAction.CASCADE)
  open var products: MutableList<ProductEntity> = mutableListOf()
}

fun FeatureGroupEntity.toDto(features: List<Feature>): FeatureGroup {
  return FeatureGroup(
    id = id.toString(),
    name = name,
    features = features,
    parentId = parentFeatureGroupId?.toString(),
  )
}
