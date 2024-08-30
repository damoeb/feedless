package org.migor.feedless.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.agent.AgentEntity
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.User
import org.migor.feedless.plan.OrderEntity
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.secrets.OneTimePasswordEntity
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.subscription.PlanEntity
import java.sql.Timestamp
import java.util.*

@Entity
@Table(
  name = "t_user",
  uniqueConstraints = [
    UniqueConstraint(name = "UniqueUser", columnNames = [StandardJpaFields.email])]
)
open class UserEntity : EntityWithUUID() {

  fun hasFinalizedProfile(): Boolean {
    return StringUtils.isNotBlank(this.email) && hasAcceptedTerms
  }

  @Email
  @Column(nullable = false, name = StandardJpaFields.email)
  open lateinit var email: String

  @Column(name = "first_name")
  @Size(max = 150)
  open var firstName: String? = null

  @Column(name = "last_name")
  @Size(max = 150)
  open var lastName: String? = null

  @Column(name = "country")
  @Size(max = 150)
  open var country: String? = null

  open var githubId: String? = null

  @Column(nullable = false, name = "has_validated_email")
  open var hasValidatedEmail: Boolean = false

  @Column(name = "validated_email_at")
  open var validatedEmailAt: Timestamp? = null

  @Column(nullable = false, name = "total_usage_mb")
  open var usageTotalMb: Double = 0.0

  @Column(nullable = false, name = "is_root")
  open var root: Boolean = false

  @Column(nullable = false, name = "is_anonymous")
  open var anonymous: Boolean = false

  @Column(name = "last_login")
  open var lastLogin: Timestamp = Timestamp(System.currentTimeMillis())

  @Column(nullable = false, name = "karma")
  open var karma: Int = 0

  @Column(nullable = false, name = "is_spamming_submissions")
  open var spammingSubmissions: Boolean = false

  @Column(nullable = false, name = "is_spamming_votes")
  open var spammingVotes: Boolean = false

  @Column(nullable = false, name = "is_shaddow_banned")
  open var shaddowBanned: Boolean = false

  @Column(nullable = false, name = "is_banned")
  open var banned: Boolean = false

  @Column(name = "is_banned_until")
  open var bannedUntil: Timestamp? = null

  @Column(nullable = false, name = "hasapprovedterms")
  open var hasAcceptedTerms: Boolean = false

  @Column(name = "approved_terms_at")
  open var acceptedTermsAt: Date? = null

  @Column(nullable = false, name = "is_locked")
  open var locked: Boolean = false

  @Column(name = "purge_scheduled_for")
  open var purgeScheduledFor: Timestamp? = null

  @Column(name = "date_format")
  open var dateFormat: String? = null // todo make nullable=false

  @Column(name = "time_format")
  open var timeFormat: String? = null

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "notification_repository_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_user__to__notifications_repository")
  )
  open var notificationRepository: RepositoryEntity? = null

  @Column(name = "notification_repository_id")
  open var notificationRepositoryId: UUID? = null

  @Column(name = "notifications_last_viewed_at")
  open var notificationsLastViewedAt: Date? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var oneTimePasswords: MutableList<OneTimePasswordEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var subscriptions: MutableList<PlanEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var userSecrets: MutableList<UserSecretEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "ownerId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var agents: MutableList<AgentEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "userId", orphanRemoval = true)
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var orders: MutableList<OrderEntity> = mutableListOf()
}


fun UserEntity.toDTO(): User =
  User(
    id = id.toString(),
    createdAt = createdAt.time,
    purgeScheduledFor = purgeScheduledFor?.time,
    hasAcceptedTerms = hasAcceptedTerms,
    hasCompletedSignup = hasFinalizedProfile(),
    email = StringUtils.trimToEmpty(email),
    emailValidated = hasValidatedEmail,
    firstName = StringUtils.trimToEmpty(firstName),
    lastName = StringUtils.trimToEmpty(lastName),
    country = StringUtils.trimToEmpty(country),
    notificationRepositoryId = notificationRepositoryId?.toString(),
    secrets = emptyList()
//          .dateFormat(propertyService.dateFormat)
//          .timeFormat(propertyService.timeFormat)
  )
