package org.migor.rich.rss.http

import io.github.bucket4j.Bucket
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.auth.AuthService
import org.migor.rich.rss.auth.JwtParameterNames
import org.migor.rich.rss.service.PlanService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.ConcurrentHashMap


@Service
@Profile("!${AppProfiles.nothrottle}")
class InMemoryRequestThrottleService : RequestThrottleService() {
  private val log = LoggerFactory.getLogger(InMemoryRequestThrottleService::class.simpleName)

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  @Autowired
  lateinit var planService: PlanService

  @Autowired
  lateinit var authService: AuthService

  fun resolveTokenBucket(token: OAuth2AuthenticationToken): Bucket {
    val userId = token.principal.attributes[JwtParameterNames.USER_ID] as String
    return cache.computeIfAbsent(userId) {
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromApiKey(token))
        .build()
    }
  }

  fun resolveIpBucket(remoteAddr: String): Bucket {
    val cacheKey = remoteAddr
    return cache.computeIfAbsent(cacheKey) {
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromIp(remoteAddr))
        .build()
    }
  }

  // see https://www.baeldung.com/spring-bucket4j
  override fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean {
    val response = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response!!
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

    val buckets = resolveBuckets(request)
    val probes = buckets.map { it.tryConsumeAndReturnRemaining(1) }
    return if (probes.all { it.isConsumed }) {
      response.addHeader("X-Rate-Limit-Remaining", probes.minOf { it.remainingTokens }.toString())
      true
    } else {
      val waitForRefill: Long = probes.maxOf { it.nanosToWaitForRefill }
      log.info("throttle ${waitForRefill}")
      throw HostOverloadingException("You have exhausted your API Request Quota", waitForRefill)
    }
  }

  private fun resolveBuckets(request: HttpServletRequest): List<Bucket> {
    val remoteAddr = HttpUtil.getRemoteAddr(request);
    val ipBucket: Bucket = resolveIpBucket(remoteAddr)
    return runCatching {
      val corrId = newCorrId()
      val token = authService.decodeToken(corrId, authService.interceptToken(corrId, request))
      val tokenBucket: Bucket = resolveTokenBucket(token)
      listOf(ipBucket, tokenBucket)
    }.getOrElse {
      listOf(ipBucket)
    }
  }
}
