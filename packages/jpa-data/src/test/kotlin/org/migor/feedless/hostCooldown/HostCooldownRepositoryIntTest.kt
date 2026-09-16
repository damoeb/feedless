package org.migor.feedless.hostCooldown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.data.jpa.JpaDataTestApplication
import org.migor.feedless.data.jpa.hostCooldown.HostCooldownDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest(classes = [JpaDataTestApplication::class])
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles("test", "database", AppProfiles.source, AppLayer.repository)
@Testcontainers
class HostCooldownRepositoryIntTest {

  @Autowired
  private lateinit var hostCooldown: HostCooldown

  @Autowired
  private lateinit var hostCooldownDAO: HostCooldownDAO

  private val host = "www.bueron.ch"
  private val now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)

  @BeforeEach
  fun setUp() {
    hostCooldownDAO.deleteAll()
  }

  @Test
  fun `throttle creates a cooldown without a strike`() {
    val state = hostCooldown.recordThrottled(host, 429, Duration.ofMinutes(10), now)

    assertThat(state.blockedUntil).isEqualTo(now.plusMinutes(10))
    assertThat(state.strikes).isEqualTo(0)
    assertThat(state.lastStatus).isEqualTo(429)
    assertThat(hostCooldown.find(host)).isEqualTo(state)
  }

  @Test
  fun `a shorter throttle never shortens an existing cooldown`() {
    hostCooldown.recordThrottled(host, 429, Duration.ofHours(1), now)

    val state = hostCooldown.recordThrottled(host, 503, Duration.ofMinutes(5), now)

    assertThat(state.blockedUntil).isEqualTo(now.plusHours(1))
    assertThat(state.lastStatus).isEqualTo(503)
  }

  @Test
  fun `blocks climb the ladder`() {
    val first = hostCooldown.recordBlocked(host, 403, now)
    val second = hostCooldown.recordBlocked(host, 403, now)
    val third = hostCooldown.recordBlocked(host, 401, now)

    assertThat(listOf(first.strikes, second.strikes, third.strikes)).containsExactly(1, 2, 3)
    assertThat(third.blockedUntil).isEqualTo(now.plusHours(2))
    assertThat(third.lastStatus).isEqualTo(401)
  }

  @Test
  fun `a throttle keeps existing strikes`() {
    hostCooldown.recordBlocked(host, 403, now)

    assertThat(hostCooldown.recordThrottled(host, 429, Duration.ofMinutes(1), now).strikes).isEqualTo(1)
  }

  @Test
  fun `success deletes the cooldown`() {
    hostCooldown.recordBlocked(host, 403, now)

    hostCooldown.recordSuccess(host)

    assertThat(hostCooldown.find(host)).isNull()
  }

  @Test
  fun `success on an unknown host is a no-op`() {
    hostCooldown.recordSuccess("unknown.example")

    assertThat(hostCooldownDAO.count()).isZero()
  }
}
