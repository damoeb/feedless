package org.migor.feedless.api.throttle

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.runBlocking
import org.aspectj.lang.ProceedingJoinPoint
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.capabilities
import org.migor.feedless.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.throttle} && ${AppLayer.api}")
class IpThrottleService(
  private val authService: AuthService
) {
  private val log = LoggerFactory.getLogger(IpThrottleService::class.simpleName)

  private val ip2BucketCache: MutableMap<String, Bucket> = ConcurrentHashMap()

  fun resolveTokenBucket(jwt: Jwt): Bucket {
    val userId =
      jwt.capabilities().find { it.authority == UserCapability.ID }
        ?.let { UserCapability.fromString(it.payload).userId } ?: throw BadRequestException("invalid jwt)")
    return ip2BucketCache.computeIfAbsent(userId.value.toString()) {
      Bucket.builder()
        .addLimit(resolveRateLimitFromApiKey(jwt))
        .build()
    }
  }

  fun resolveIpBucket(remoteAddr: String): Bucket {
    log.debug("throttle by ip $remoteAddr")
    return ip2BucketCache.computeIfAbsent(remoteAddr) {
      Bucket.builder()
        .addLimit(resolveRateLimitFromIp(remoteAddr))
        .build()
    }
  }

  // see https://www.baeldung.com/spring-bucket4j
  fun tryAquire(joinPoint: ProceedingJoinPoint): Boolean {
    val response = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response!!
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

    return if (authService.isWhitelisted(request)) {
      true
    } else {
      val buckets = resolveRateBuckets(request)
      val probes = buckets.map { it.tryConsumeAndReturnRemaining(1) }
      if (probes.all { it.isConsumed }) {
        response.addHeader("X-Rate-Limit-Remaining", probes.minOf { it.remainingTokens }.toString())
        true
      } else {
        val waitForRefill: Long = probes.maxOf { it.nanosToWaitForRefill }
        throw HostOverloadingException(
          "You have exhausted your API Request Quota",
          Duration.ofNanos(waitForRefill)
        )
      }
    }
  }

  fun resolveRateLimitFromApiKey(token: Jwt): Bandwidth {
    return when (AuthTokenType.valueOf(token.getClaim<String>(JwtParameterNames.TYPE).uppercase())) {
      AuthTokenType.SERVICE -> Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)))
      AuthTokenType.USER -> Bandwidth.classic(300, Refill.intervally(300, Duration.ofMinutes(1)))
      AuthTokenType.API -> Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
      AuthTokenType.ANONYMOUS -> Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
    }
  }

  fun resolveRateLimitFromIp(remoteAddr: String): Bandwidth {
    return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
  }

  private fun resolveRateBuckets(request: HttpServletRequest): List<Bucket> {
    return runBlocking {
      runCatching {
        val token = authService.interceptToken(request)
        listOf(resolveTokenBucket(token))
      }.getOrElse {
        val remoteAddr = HttpUtil.getRemoteAddr(request)
        listOf(resolveIpBucket(remoteAddr))
      }
    }
  }
}

private fun Jwt.getUserCapability(): UserCapability? {
  return this.capabilities().find { it.authority == UserCapability.ID }?.let { UserCapability.fromString(it.payload) }
}
