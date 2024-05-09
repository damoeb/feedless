package org.migor.feedless.subscription

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.plan.PlanEntity
import org.migor.feedless.user.UserEntity
import java.util.*


@Entity
@Table(name = "t_user_plan_subscription")
open class UserPlanSubscriptionEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.userId, nullable = false)
  open lateinit var userId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var user: UserEntity? = null

  @Column(name = StandardJpaFields.planId, nullable = false)
  open lateinit var planId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.planId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var plan: PlanEntity? = null

  open var paidUntil: Date? = null

  open var startedAt: Date? = null

  @Column(nullable = false)
  open var recurring: Boolean = false


}

