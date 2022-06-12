package org.migor.rich.rss.http

import io.github.bucket4j.Bucket
import org.aspectj.lang.ProceedingJoinPoint
import org.migor.rich.rss.plan.PlanService
import org.migor.rich.rss.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest


@Service
@Profile("stateless")
class InMemoryRequestThrottleService: RequestThrottleService() {
  private val log = LoggerFactory.getLogger(InMemoryRequestThrottleService::class.simpleName)

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  @Autowired
  lateinit var planService: PlanService

  @Autowired
  lateinit var authService: AuthService

  fun resolveBucket(request: HttpServletRequest): Bucket {
    val token = kotlin.runCatching { authService.interceptToken(request) }.getOrNull()
    val cacheKey = Optional.ofNullable(token).orElse(request.remoteAddr)
    return cache.computeIfAbsent(cacheKey) { newBucket(token, request.remoteAddr) }
  }

  private fun newBucket(token: String?, remoteAddr: String): Bucket {
    return runCatching {
      val decoded = authService.validateAuthToken("-", token, remoteAddr)
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromApiKey(decoded))
        .build()
    }.getOrElse {
      Bucket.builder()
        .addLimit(planService.resolveRateLimitFromIp(remoteAddr))
        .build()
    }
  }

  // see https://www.baeldung.com/spring-bucket4j
  override fun tryConsume(joinPoint: ProceedingJoinPoint): Boolean {
    val response = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).response!!
    val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

    val bucket: Bucket = resolveBucket(request)
    val probe = bucket.tryConsumeAndReturnRemaining(1)
    return if (probe.isConsumed) {
      response.addHeader("X-Rate-Limit-Remaining", java.lang.String.valueOf(probe.remainingTokens))
      true
    } else {
      val waitForRefill: Long = probe.nanosToWaitForRefill / 1000000000
      response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitForRefill.toString())
      response.sendError(
        HttpStatus.TOO_MANY_REQUESTS.value(),
        "You have exhausted your API Request Quota"
      )
      false
    }
  }
}
