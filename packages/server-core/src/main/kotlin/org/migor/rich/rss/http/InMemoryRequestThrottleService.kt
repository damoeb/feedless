package org.migor.rich.rss.http

import io.github.bucket4j.Bucket
import org.aspectj.lang.ProceedingJoinPoint
import org.migor.rich.rss.api.ApiParams
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.plan.PlanService
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.AuthToken
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest


@Service
@Profile("!nothrottle")
class InMemoryRequestThrottleService: RequestThrottleService() {
  private val log = LoggerFactory.getLogger(InMemoryRequestThrottleService::class.simpleName)

  private val cache: MutableMap<String, Bucket> = ConcurrentHashMap()

  @Autowired
  lateinit var planService: PlanService

  @Autowired
  lateinit var authService: AuthService

  fun resolveTokenBucket(cacheKey: String, token: AuthToken): Bucket {
    return cache.computeIfAbsent(cacheKey) { Bucket.builder()
      .addLimit(planService.resolveRateLimitFromApiKey(token))
      .build()
    }
  }

  fun resolveIpBucket(remoteAddr: String): Bucket {
    val cacheKey = remoteAddr
    return cache.computeIfAbsent(cacheKey) { Bucket.builder()
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
      throw HostOverloadingException("You have exhausted your API Request Quota", waitForRefill)
    }
  }

  private fun resolveBuckets(request: HttpServletRequest): List<Bucket> {
    val remoteAddr = request.remoteAddr
    val ipBucket: Bucket = resolveIpBucket(remoteAddr)
    return runCatching {
      val rawToken = authService.interceptToken(newCorrId(), request)
      val corrId = resolveCorrId(request)
      val token = authService.validateAuthToken(corrId, rawToken, remoteAddr)
      val tokenBucket: Bucket = resolveTokenBucket(rawToken, token)
      listOf(ipBucket, tokenBucket)
    }.getOrElse {
      listOf(ipBucket)
    }
  }

  private fun resolveCorrId(request: HttpServletRequest): String {
    val corrId = Optional.ofNullable(request.getParameter( ApiParams.corrId)).orElse(newCorrId())
    request.parameterMap[ ApiParams.corrId] = arrayOf(corrId)
    return corrId
  }
}
