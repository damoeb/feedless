package org.migor.feedless.api

import io.github.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.aspectj.lang.ProceedingJoinPoint
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.api.auth.AuthService
import org.migor.feedless.api.auth.JwtParameterNames
import org.migor.feedless.service.PlanService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.migor.feedless.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


@Service
@Profile("!${AppProfiles.nothrottle} && ${AppProfiles.database}")
class InMemoryRequestThrottleService : RequestThrottleService() {
  private val log = LoggerFactory.getLogger(InMemoryRequestThrottleService::class.simpleName)

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  @Autowired
  lateinit var planService: PlanService

  @Autowired
  lateinit var authService: AuthService

  fun resolveTokenBucket(jwt: Jwt): Bucket {
    val userId = StringUtils.trimToNull(jwt.getClaim(JwtParameterNames.USER_ID)) ?: throw BadRequestException("invalid jwt)")
    return cache.computeIfAbsent(userId) {
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromApiKey(jwt))
        .build()
    }
  }

  fun resolveIpBucket(remoteAddr: String): Bucket {
    log.info("throttle by ip $remoteAddr")
    return cache.computeIfAbsent(remoteAddr) {
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromIp(remoteAddr))
        .build()
    }
  }

  // see https://www.baeldung.com/spring-bucket4j
  override fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean {
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
        throw HostOverloadingException(newCorrId(), "You have exhausted your API Request Quota", Duration.ofNanos(waitForRefill))
      }
    }
  }

  private fun resolveRateBuckets(request: HttpServletRequest): List<Bucket> {
    return runCatching {
      val jwt = authService.interceptJwt(request)
      listOf(resolveTokenBucket(jwt))
    }.getOrElse {
      val remoteAddr = HttpUtil.getRemoteAddr(request)
      listOf(resolveIpBucket(remoteAddr))
    }
  }
}
