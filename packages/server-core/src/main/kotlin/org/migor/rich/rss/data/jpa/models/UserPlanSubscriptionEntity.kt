package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.util.*


@Entity
@Table(name = "t_user_plan_subscription")
open class UserPlanSubscriptionEntity : EntityWithUUID() {

  @Basic
  @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
  open var userId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  open var user: UserEntity? = null

  @Basic
  @Column(name = "plan_id", insertable = false, updatable = false, nullable = false)
  open var planId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "plan_id", referencedColumnName = "id")
  open var plan: PlanEntity? = null

  @Basic
  open var paidUntil: Date? = null

  @Basic
  open var startedAt: Date? = null

  @Basic
  @Column(nullable = false)
  open var recurring: Boolean = false


}

