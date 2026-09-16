package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.hostCooldown.HostCooldownState
import java.time.Duration
import java.time.LocalDateTime

class FakeHostCooldown : HostCooldown {
  val rows = mutableMapOf<String, HostCooldownState>()
  var successCalls = 0

  override fun find(host: String) = rows[host]

  override fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState {
    val existing = rows[host]
    val until = maxOf(existing?.blockedUntil ?: now, now.plus(retryAfter))
    return HostCooldownState(host, until, existing?.strikes ?: 0, status).also { rows[host] = it }
  }

  override fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState {
    val strikes = (rows[host]?.strikes ?: 0) + 1
    return HostCooldownState(host, now.plus(HostBlockLadder.delayFor(strikes)), strikes, status).also { rows[host] = it }
  }

  override fun recordSuccess(host: String, now: LocalDateTime): Boolean {
    successCalls++
    val existing = rows[host] ?: return false
    return if (!existing.blockedUntil.isAfter(now)) {
      rows.remove(host)
      true
    } else {
      false
    }
  }
}

class HostCooldownGuardTest {

  private val fake = FakeHostCooldown()
  private val guard = HostCooldownGuard(fake)

  @Test
  fun `an open host passes`() {
    guard.requireOpen("https://www.bueron.ch/a")
  }

  @Test
  fun `a cooling host throws before any request with the remaining time`() {
    fake.rows["www.bueron.ch"] = HostCooldownState("www.bueron.ch", LocalDateTime.now().plusMinutes(10), 0, 429)

    assertThatThrownBy { guard.requireOpen("https://WWW.bueron.ch/a") }
      .isInstanceOf(HostOverloadingException::class.java)
      .matches { (it as HostOverloadingException).nextRetryAfter > Duration.ofMinutes(9) }
  }

  @Test
  fun `a throttle records the parsed Retry-After and throws`() {
    assertThatThrownBy { guard.onThrottled("https://www.bueron.ch/a", 429, "600") }
      .isInstanceOf(HostOverloadingException::class.java)
      .hasMessageContaining("throttled by www.bueron.ch (429)")
      .matches { (it as HostOverloadingException).nextRetryAfter == Duration.ofSeconds(600) }
    assertThat(fake.rows["www.bueron.ch"]!!.lastStatus).isEqualTo(429)
  }

  @Test
  fun `a block records a strike and throws HostBlockedException`() {
    assertThatThrownBy { guard.onBlocked("https://www.bueron.ch/a", 403) }
      .isInstanceOf(HostBlockedException::class.java)
      .matches { (it as HostBlockedException).strikes == 1 && it.nextRetryAfter == Duration.ofMinutes(5) }
  }

  @Test
  fun `success only writes for hosts known to have a cooldown`() {
    guard.onSuccess("https://quiet.example/a")
    assertThat(fake.successCalls).isZero()

    // The fresh block's cooldown is still active, so the success below cannot clear it yet.
    runCatching { guard.onBlocked("https://www.bueron.ch/a", 403) }
    guard.onSuccess("https://www.bueron.ch/b")

    assertThat(fake.successCalls).isEqualTo(1)
    assertThat(fake.rows).containsKey("www.bueron.ch")
  }

  @Test
  fun `an expired row seen by requireOpen is reset on the next success`() {
    fake.rows["www.bueron.ch"] = HostCooldownState("www.bueron.ch", LocalDateTime.now().minusMinutes(1), 2, 403)

    guard.requireOpen("https://www.bueron.ch/a")
    guard.onSuccess("https://www.bueron.ch/a")

    assertThat(fake.rows).isEmpty()
  }

  @Test
  fun `a success while the cooldown is still active leaves the row`() {
    fake.rows["www.bueron.ch"] = HostCooldownState("www.bueron.ch", LocalDateTime.now().plusMinutes(10), 1, 429)

    assertThatThrownBy { guard.requireOpen("https://www.bueron.ch/a") }.isInstanceOf(HostOverloadingException::class.java)
    guard.onSuccess("https://www.bueron.ch/a")

    assertThat(fake.rows).containsKey("www.bueron.ch")
  }

  @Test
  fun `without a cooldown store nothing is checked or recorded`() {
    val bare = HostCooldownGuard(null)
    bare.requireOpen("https://www.bueron.ch/a")
    assertThatThrownBy { bare.onThrottled("https://www.bueron.ch/a", 429, null) }
      .isInstanceOf(HostOverloadingException::class.java)
  }
}
