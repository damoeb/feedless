package org.migor.feedless.common

import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.hostCooldown.HostBlockLadder
import org.migor.feedless.hostCooldown.HostCooldown
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/** Null [hostCooldown] means the persistence profiles are off; backpressure then only lives in the thrown delay. */
@Service
class HostCooldownGuard(private val hostCooldown: HostCooldown?) {

  private val log = LoggerFactory.getLogger(HostCooldownGuard::class.simpleName)

  // Hosts with a row, so a success on any other host costs no database write.
  private val hostsWithRow: MutableSet<String> = ConcurrentHashMap.newKeySet()

  fun requireOpen(url: String) {
    val host = hostOf(url) ?: return
    val state = hostCooldown?.find(host) ?: return
    hostsWithRow.add(host)
    val remaining = Duration.between(LocalDateTime.now(), state.blockedUntil)
    if (remaining.isPositive) {
      throw HostOverloadingException("host $host cooling down until ${state.blockedUntil}", remaining)
    }
  }

  fun onThrottled(url: String, status: Int, retryAfterHeader: String?): Nothing {
    val host = hostOf(url)
    val delay = RetryAfter.parse(retryAfterHeader)
    if (host != null && hostCooldown != null) {
      hostCooldown.recordThrottled(host, status, delay, LocalDateTime.now())
      hostsWithRow.add(host)
    }
    log.info("throttled by $host ($status), retry in $delay")
    throw HostOverloadingException("throttled by $host ($status), retry in ${delay.toMinutes()}m", delay)
  }

  fun onBlocked(url: String, status: Int): Nothing {
    val host = hostOf(url)
    val now = LocalDateTime.now()
    val state = host?.let { hostCooldown?.recordBlocked(it, status, now) }
    state?.let { hostsWithRow.add(it.host) }
    val strikes = state?.strikes ?: 1
    val delay = state?.let { Duration.between(now, it.blockedUntil) } ?: HostBlockLadder.delayFor(strikes)
    log.info("blocked by $host ($status, strike $strikes), retry in $delay")
    throw HostBlockedException(host ?: url, status, strikes, delay)
  }

  fun onSuccess(url: String) {
    val host = hostOf(url) ?: return
    if (hostCooldown != null && hostsWithRow.contains(host)) {
      val deleted = hostCooldown.recordSuccess(host, LocalDateTime.now())
      // A row survives an in-flight success while its cooldown is still active; only drop set membership once it's actually gone.
      if (deleted || hostCooldown.find(host) == null) {
        hostsWithRow.remove(host)
      }
    }
  }
}
