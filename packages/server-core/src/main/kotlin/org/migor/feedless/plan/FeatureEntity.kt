package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.Feature
import java.util.*


@Entity
@Table(
  name = "t_feature", uniqueConstraints = [
    UniqueConstraint(
      name = "UniqueFeaturePerProduct",
      columnNames = [StandardJpaFields.product_id, StandardJpaFields.name]
    )]
)
open class FeatureEntity : EntityWithUUID() {

  @Column(nullable = false, name = StandardJpaFields.name, length = 50)
  open lateinit var name: String

  @Column(length = 50, name="scope")
  open var scope: String? = null

  @Column(name = StandardJpaFields.product_id, nullable = false)
  open lateinit var productId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.product_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false
  )
  open var product: ProductEntity? = null
}

fun FeatureEntity.toDto(): Feature {
  return Feature.newBuilder()
    .name(name)
    .build()
}
