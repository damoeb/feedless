package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.User
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_user")
open class UserEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  open lateinit var email: String

  @Basic
  @Column(nullable = false)
  open var emailValidated: Boolean = false

  @Basic
  @Column(nullable = false)
  open lateinit var name: String

  @Basic
  @Column(nullable = false)
  open var isRoot: Boolean = false

  @Basic
  @Column(nullable = false)
  open var hasApprovedTerms: Boolean = false

  @Basic
  @Column(nullable = false)
  open var locked: Boolean = false

  @Basic
  open var approvedTermsAt: Timestamp? = null

  @Basic
  open var purgeScheduledFor: Timestamp? = null

  @Basic
  @Column(name = "date_format")
  open var dateFormat: String? = null // todo make nullable=false

  @Basic
  @Column(name = "time_format", nullable = true)
  open var timeFormat: String? = null

  @Basic
  @Column(name = "plan_id", insertable = false, updatable = false)
  open var planId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH])
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(name = "plan_id", referencedColumnName = "id", foreignKey = ForeignKey(name = "fk_user__plan"))
  open var plan: PlanEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var oneTimePasswords: MutableList<OneTimePasswordEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var planSubscriptions: MutableList<UserPlanSubscriptionEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var userSecrets: MutableList<UserSecretEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var agents: MutableList<AgentEntity> = mutableListOf()
}


fun UserEntity.toDto(): User =
  User.newBuilder()
    .id(this.id.toString())
    .createdAt(this.createdAt.time)
    .name(this.name)
    .purgeScheduledFor(this.purgeScheduledFor?.time)
    .acceptedTermsAndServices(this.hasApprovedTerms)
    .build()
