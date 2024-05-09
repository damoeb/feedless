package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.user.UserEntity
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

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var plans: MutableList<FeatureValueEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var features: MutableList<FeatureEntity> = mutableListOf()

  @Column(name = "parent_product_id", nullable = true, insertable = false, updatable = false)
  open var parentProductId: UUID? = null

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "parent_product_id",
    referencedColumnName = "id",
  )
  open var parentProduct: ProductEntity? = null

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open var ownerId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var owner: UserEntity? = null
}
