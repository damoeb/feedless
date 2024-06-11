package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.PricedProduct
import java.util.*

@Entity
@Table(
  name = "t_priced_product",
)
open class PricedProductEntity : EntityWithUUID() {

  @Column(name = "valid_from")
  open var validFrom: Date? = null

  @Column(name = "valid_to")
  open var validTo: Date? = null

  @Column(name = "sold_unit", nullable = false)
  open lateinit var unit: String

  @Column(name = "price", nullable = false)
  open var price: Double = 0.0

  @Column(name = "target_group_individual", nullable = false)
  open var individual: Boolean = false

  @Column(name = "target_group_enterprise", nullable = false)
  open var enterprise: Boolean = false

  @Column(name = "target_group_other", nullable = false)
  open var other: Boolean = false

  @Column(name = "in_stock")
  open var inStock: Int? = null

  @Column(name = StandardJpaFields.product_id, nullable = false)
  open var productId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.product_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_priced_product__to__product")
  )
  open var product: ProductEntity? = null
}

fun PricedProductEntity.toDto(): PricedProduct {
  return PricedProduct.newBuilder()
    .id(id.toString())
    .other(other)
    .enterprise(enterprise)
    .individual(individual)
    .description(this.unit)
    .price(price)
    .inStock(inStock ?: -1)
    .build()
}
