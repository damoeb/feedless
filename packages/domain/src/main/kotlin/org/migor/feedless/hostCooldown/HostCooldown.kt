package org.migor.feedless.hostCooldown

import java.time.Duration
import java.time.LocalDateTime

data class HostCooldownState(
  val host: String,
  val blockedUntil: LocalDateTime,
  val strikes: Int,
  val lastStatus: Int?,
)

/** Persisted per-host backoff, shared by every core instance. */
interface HostCooldown {
  fun find(host: String): HostCooldownState?

  /** Never shortens an existing cooldown; strikes are left untouched. */
  fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState

  /** Adds a strike and extends the cooldown by [HostRequestBackoffLadder]. */
  fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState

  /** Deletes the cooldown only if it has already expired; returns whether a row was deleted. */
  fun recordSuccess(host: String, now: LocalDateTime): Boolean
}

object HostRequestBackoffLadder {
  private val steps = listOf(
    Duration.ofMinutes(5),
    Duration.ofMinutes(30),
    Duration.ofHours(2),
    Duration.ofHours(12),
    Duration.ofHours(24),
  )

  fun delayFor(strikes: Int): Duration = steps[(strikes - 1).coerceIn(0, steps.lastIndex)]
}
