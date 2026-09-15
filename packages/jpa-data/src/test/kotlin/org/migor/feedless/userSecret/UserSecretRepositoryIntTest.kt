package org.migor.feedless.userSecret

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles("test", "database", AppProfiles.secrets, AppProfiles.user, AppLayer.repository)
@Testcontainers
class UserSecretRepositoryIntTest {

  @Autowired
  private lateinit var userSecretRepository: UserSecretRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  private val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

  @Test
  fun `keeps the name`() {
    val secret = secretLastUsedAt(null)

    assertThat(userSecretRepository.findById(secret.id)!!.name).isEqualTo("laptop")
  }

  @Test
  fun `records the first use`() {
    val secret = secretLastUsedAt(null)

    userSecretRepository.updateLastUsedIfStale(secret.id, now, now.minusMinutes(1))

    assertThat(userSecretRepository.findById(secret.id)!!.lastUsedAt).isEqualTo(now)
  }

  @Test
  fun `records a use once the last one is stale`() {
    val secret = secretLastUsedAt(now.minusMinutes(5))

    userSecretRepository.updateLastUsedIfStale(secret.id, now, now.minusMinutes(1))

    assertThat(userSecretRepository.findById(secret.id)!!.lastUsedAt).isEqualTo(now)
  }

  @Test
  fun `skips a use while the last one is fresh`() {
    val lastUsedAt = now.minusSeconds(10)
    val secret = secretLastUsedAt(lastUsedAt)

    userSecretRepository.updateLastUsedIfStale(secret.id, now, now.minusMinutes(1))

    assertThat(userSecretRepository.findById(secret.id)!!.lastUsedAt).isEqualTo(lastUsedAt)
  }

  private fun secretLastUsedAt(lastUsedAt: LocalDateTime?): UserSecret {
    val owner = userRepository.save(User(email = "secret-${System.nanoTime()}@example.com", lastLogin = now))
    return userSecretRepository.save(
      UserSecret(
        name = "laptop",
        value = UUID.randomUUID().toString(),
        validUntil = now.plusDays(1),
        type = UserSecretType.SecretKey,
        ownerId = owner.id,
        lastUsedAt = lastUsedAt,
      )
    )
  }
}
