package org.migor.feedless.user

import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

data class User(
  val id: UserId,
  val email: String,
  val firstName: String?,
  val lastName: String?,
  val country: String?,
  val hasValidatedEmail: Boolean,
  val validatedEmailAt: LocalDateTime?,
  val admin: Boolean,
  val anonymous: Boolean,
  val lastLogin: LocalDateTime,
  val karma: Int,
  val spammingSubmissions: Boolean,
  val spammingVotes: Boolean,
  val shadowBanned: Boolean,
  val banned: Boolean,
  val bannedUntil: LocalDateTime?,
  val hasAcceptedTerms: Boolean,
  val acceptedTermsAt: LocalDateTime?,
  val locked: Boolean,
  val purgeScheduledFor: LocalDateTime?,
  val dateFormat: String?, // todo make nullable=false
  val timeFormat: String?,
  val inboxRepositoryId: RepositoryId?,
  val notificationsLastViewedAt: LocalDateTime?
)
