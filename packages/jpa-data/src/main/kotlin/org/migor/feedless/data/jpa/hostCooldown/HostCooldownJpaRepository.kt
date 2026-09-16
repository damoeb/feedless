package org.migor.feedless.data.jpa.hostCooldown

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.migor.feedless.hostCooldown.HostCooldownState
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
class HostCooldownJpaRepository(private val hostCooldownDAO: HostCooldownDAO) : HostCooldown {

  override fun find(host: String): HostCooldownState? =
    hostCooldownDAO.findById(host).getOrNull()?.toDomain()

  @Transactional
  override fun recordThrottled(host: String, status: Int, retryAfter: Duration, now: LocalDateTime): HostCooldownState {
    hostCooldownDAO.upsertThrottled(host, now.plus(retryAfter), status, now)
    return find(host)!!
  }

  // The upsert holds the row lock until commit, so the strike read below is this transaction's own.
  @Transactional
  override fun recordBlocked(host: String, status: Int, now: LocalDateTime): HostCooldownState {
    hostCooldownDAO.upsertStrike(host, status, now)
    val strikes = find(host)!!.strikes
    hostCooldownDAO.extendBlockedUntil(host, now.plus(HostBlockLadder.delayFor(strikes)))
    return find(host)!!
  }

  @Transactional
  override fun recordSuccess(host: String) {
    hostCooldownDAO.deleteById(host)
  }
}
