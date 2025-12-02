package org.migor.feedless.data.jpa.pricedProduct

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
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.product.ProductEntity
import org.migor.feedless.product.PricedProduct
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
@Table(
  name = "t_priced_product",
)
open class PricedProductEntity : EntityWithUUID() {

  @Column(name = "valid_from")
  open var validFrom: LocalDateTime? = null

  @Column(name = "valid_to")
  open var validTo: LocalDateTime? = null

  @Column(name = "sold_unit", nullable = false)
  open lateinit var unit: String

  @Column(name = "price", nullable = false)
  open var price: Double = 0.0

  @Column(name = "in_stock")
  open var inStock: Int? = null

  @Column(nullable = false, name = "recurring_interval")
  @Enumerated(EnumType.STRING)
  open var recurringInterval: ChronoUnit = ChronoUnit.YEARS

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

fun PricedProductEntity.toDomain(): PricedProduct {
  return PricedProductMapper.INSTANCE.toDomain(this)
}

fun PricedProduct.toEntity(): PricedProductEntity {
  return PricedProductMapper.INSTANCE.toEntity(this)
}
