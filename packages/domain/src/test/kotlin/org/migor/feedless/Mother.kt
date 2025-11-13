package org.migor.feedless

import org.migor.feedless.otp.OneTimePassword
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

  fun randomLocalDateTime(): LocalDateTime {
    val minEpoch = LocalDateTime.of(2020, 1, 1, 0, 0).toEpochSecond(ZoneOffset.UTC)
    val maxEpoch = LocalDateTime.of(2030, 12, 31, 23, 59).toEpochSecond(ZoneOffset.UTC)
    val randomEpoch = minEpoch + (random.nextDouble() * (maxEpoch - minEpoch)).toLong()
    return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC)
  }

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

  fun randomOneTimePassword(userId: UUID? = null): OneTimePassword {
    return OneTimePassword(
      id = randomUUID(),
      password = randomString().substring(0, 6),
      validUntil = LocalDateTime.now().plusMinutes(10),
      userId = userId ?: randomUUID()
    )
  }

}
