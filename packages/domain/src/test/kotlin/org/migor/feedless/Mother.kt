package org.migor.feedless

import org.migor.feedless.user.User
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.UUID.randomUUID

object Mother {
  private val random = Random();

  fun randomUser(): User {
    val user = User(
      id = randomUUID(),
      email = randomString(),
      firstName = randomNullableString(),
      lastName = randomNullableString(),
      country = randomNullableString(),
      hasValidatedEmail = randomBoolean(),
      validatedEmailAt = randomNullableLocalDateTime(),
      admin = randomBoolean(),
      anonymous = randomBoolean(),
      lastLogin = randomLocalDateTime(),
      karma = randomInt(),
      spammingSubmissions = randomBoolean(),
      spammingVotes = randomBoolean(),
      shadowBanned = randomBoolean(),
      banned = randomBoolean(),
      bannedUntil = randomNullableLocalDateTime(),
      hasAcceptedTerms = randomBoolean(),
      acceptedTermsAt = randomNullableLocalDateTime(),
      locked = randomBoolean(),
      purgeScheduledFor = randomNullableLocalDateTime(),
      dateFormat = randomNullableString(),
      timeFormat = randomNullableString(),
      inboxRepositoryId = randomNullableUUID(),
      notificationsLastViewedAt = randomNullableLocalDateTime()
    )

    return user
  }

  fun randomInt() = random.nextInt()
  fun randomNullableUUID(): UUID? {
    return if (randomBoolean()) {
      randomUUID()
    } else {
      null
    }
  }

  fun randomLocalDateTime(): LocalDateTime = LocalDateTime.ofEpochSecond(random.nextLong(), 0, ZoneOffset.UTC)
  fun randomNullableLocalDateTime(): LocalDateTime? {
    return if (randomBoolean()) {
      randomLocalDateTime()
    } else {
      null
    }
  }

  fun randomBoolean() = random.nextBoolean()
  fun randomString() = randomUUID().toString()
  fun randomNullableString() = randomUUID().toString()

}
