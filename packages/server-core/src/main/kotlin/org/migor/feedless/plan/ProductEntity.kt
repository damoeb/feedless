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
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ProductCategory
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
  open var isCloudProduct: Boolean = false

  @Column(nullable = false, name = "is_base_product")
  open var baseProduct: Boolean = false

  @Column(name = "part_of")
  @Enumerated(EnumType.STRING)
  open var partOf: ProductCategory? = null

  @Column(name = "feature_group_id")
  open var featureGroupId: UUID? = null

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

  @PrePersist
  fun prePersist() {
    if (isCloudProduct && featureGroup == null) {
      throw IllegalArgumentException("when isCloudProject=true you must define a feature group")
    }
  }
}


fun ProductEntity.toDTO(): Product {
  return Product.newBuilder()
    .id(id.toString())
    .name(name)
    .description(description)
    .isCloud(isCloudProduct)
    .partOf(partOf?.toDTO())
    .featureGroupId(featureGroupId?.toString())
    .build()
}

private fun ProductCategory?.toDTO(): org.migor.feedless.generated.types.ProductCategory {
  return when(this) {
    ProductCategory.rssProxy -> org.migor.feedless.generated.types.ProductCategory.rssProxy
    else -> throw IllegalArgumentException("Unsupported product name $this")
  }
}
