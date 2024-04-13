package org.migor.feedless.data.jpa.models

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
import org.migor.feedless.user.UserEntity
import java.util.*


@Entity
@Table(name = "t_user_plan_subscription")
open class UserPlanSubscriptionEntity : EntityWithUUID() {

  @Column(name = "user_id", nullable = false)
  open lateinit var userId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "user_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_ups__user")
  )
  open var user: UserEntity? = null

  @Column(name = "plan_id", nullable = false)
  open lateinit var planId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "plan_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_ups__plan")
  )
  open var plan: PlanEntity? = null

  open var paidUntil: Date? = null

  open var startedAt: Date? = null

  @Column(nullable = false)
  open var recurring: Boolean = false


}

