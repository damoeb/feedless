package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.Vertical
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.toDto
import org.migor.feedless.feature.FeatureGroupEntity
import org.migor.feedless.generated.types.Product
import java.util.*

@Entity
@Table(
  name = "t_product",
  uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueProduct",
      columnNames = [StandardJpaFields.name]
    )]
)
open class ProductEntity : EntityWithUUID() {

  @Column(nullable = false, name = StandardJpaFields.name, length = 50)
  open lateinit var name: String

  @Column(nullable = false, name = "description", length = 300)
  open lateinit var description: String

  @Column(nullable = false, name = "is_cloud")
  open var saas: Boolean = false

  @Column(nullable = false, name = "is_available")
  open var available: Boolean = false

  @Column(nullable = false, name = "is_base_product")
  open var baseProduct: Boolean = false

  @Column(name = "self_hosting_individual", nullable = false)
  open var selfHostingIndividual: Boolean = false

  @Column(name = "self_hosting_enterprise", nullable = false)
  open var selfHostingEnterprise: Boolean = false

  @Column(name = "self_hosting_other", nullable = false)
  open var selfHostingOther: Boolean = false

  @Column(name = "part_of")
  @Enumerated(EnumType.STRING)
  open var partOf: Vertical? = null

  @Column(name = "feature_group_id")
  open lateinit var featureGroupId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "feature_group_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_product__to__feature_group")
  )
  open var featureGroup: FeatureGroupEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "productId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var prices: MutableList<PricedProductEntity> = mutableListOf()

}


fun ProductEntity.toDTO(): Product {
  return Product(
    id = id.toString(),
    name = name,
    description = description,
    isCloud = saas,
    individual = selfHostingIndividual,
    enterprise = selfHostingEnterprise,
    other = selfHostingOther,
    partOf = partOf?.toDto(),
    featureGroupId = featureGroupId.toString(),
    prices = emptyList(),
  )
}
