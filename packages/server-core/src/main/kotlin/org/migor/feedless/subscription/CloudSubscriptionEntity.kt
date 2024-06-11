package org.migor.feedless.subscription

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
import org.migor.feedless.plan.ProductEntity
import org.migor.feedless.user.UserEntity
import java.util.*


@Entity
@Table(name = "t_cloud_subscription")
open class CloudSubscriptionEntity : EntityWithUUID() {

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
  open var startedAt: Date? = null

  @Column(name = "terminated_at")
  open var terminatedAt: Date? = null


}

