package org.migor.feedless.jpa.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.jpa.product.ProductEntity
import org.migor.feedless.jpa.user.UserEntity
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "t_plan")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
open class PlanEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.userId, nullable = false)
  open lateinit var userId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_subscription__to__user")
  )
  open var user: UserEntity? = null

  @Column(name = StandardJpaFields.product_id, nullable = false)
  open lateinit var productId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.product_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_subscription__to__product")
  )
  open var product: ProductEntity? = null

  @Column(name = "started_at")
  open var startedAt: LocalDateTime? = null

  @Column(name = "terminated_at")
  open var terminatedAt: LocalDateTime? = null


}
