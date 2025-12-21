package org.migor.feedless.user

import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

data class User(
  val id: UserId = UserId(),
  val email: String,
  val firstName: String? = null,
  val lastName: String? = null,
  val country: String? = null,
  val hasValidatedEmail: Boolean = false,
  val validatedEmailAt: LocalDateTime? = null,
  val admin: Boolean = false,
  val anonymous: Boolean = false,
  val lastLogin: LocalDateTime,
  val karma: Int = 0,
  val spammingSubmissions: Boolean = false,
  val spammingVotes: Boolean = false,
  val shadowBanned: Boolean = false,
  val banned: Boolean = false,
  val bannedUntil: LocalDateTime? = null,
  val hasAcceptedTerms: Boolean = false,
  val acceptedTermsAt: LocalDateTime? = null,
  val locked: Boolean = false,
  val purgeScheduledFor: LocalDateTime? = null,
  val dateFormat: String? = null, // todo make nullable=false
  val timeFormat: String? = null,
  val inboxRepositoryId: RepositoryId? = null,
  val notificationsLastViewedAt: LocalDateTime? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
