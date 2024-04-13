package org.migor.feedless.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.agent.AgentEntity
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.PlanEntity
import org.migor.feedless.data.jpa.models.UserPlanSubscriptionEntity
import org.migor.feedless.generated.types.User
import org.migor.feedless.secrets.UserSecretEntity
import java.sql.Timestamp
import java.util.*

@Entity
@Table(
  name = "t_user",
  uniqueConstraints = [
    UniqueConstraint(name = "UniqueUser", columnNames = [StandardJpaFields.email, StandardJpaFields.product])]
)
open class UserEntity : EntityWithUUID() {

  @Column(name = StandardJpaFields.email)
  open var email: String? = null

  open var githubId: String? = null

  @Column(nullable = false)
  open var hasValidatedEmail: Boolean = false

  open var validatedEmailAt: Timestamp? = null

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  open lateinit var usesAuthSource: AuthSource

  @Column(nullable = false, name = StandardJpaFields.product)
  open lateinit var product: ProductName

  @Column(nullable = false)
  open var root: Boolean = false

  @Column(nullable = false)
  open var anonymous: Boolean = false

  @Column(nullable = false)
  open var hasAcceptedTerms: Boolean = false

  open var acceptedTermsAt: Timestamp? = null

  @Column(nullable = false)
  open var locked: Boolean = false

  open var purgeScheduledFor: Timestamp? = null

  @Column
  open var dateFormat: String? = null // todo make nullable=false

  @Column
  open var timeFormat: String? = null

  @Column(name = "plan_id", nullable = true)
  open var planId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  @JoinColumn(
    name = "plan_id",
    referencedColumnName = "id",
    foreignKey = ForeignKey(name = "fk_user__plan"),
    insertable = false,
    updatable = false
  )
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
    .id(id.toString())
    .createdAt(createdAt.time)
    .purgeScheduledFor(purgeScheduledFor?.time)
    .hasAcceptedTerms(hasAcceptedTerms)
//          .dateFormat(propertyService.dateFormat)
//          .timeFormat(propertyService.timeFormat)
//          .minimalFeatureState(FeatureState.experimental)

    .build()
