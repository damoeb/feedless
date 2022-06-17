package org.migor.rich.rss.plan

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import org.migor.rich.rss.service.AuthToken
import org.migor.rich.rss.service.AuthTokenType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  fun resolveRateLimitFromApiKey(token: AuthToken): Bandwidth {
    return when(token.type) {
      AuthTokenType.WEB -> Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofHours(1)))
      AuthTokenType.ANON -> Bandwidth.classic(40, Refill.intervally(40, Duration.ofHours(1)))
      AuthTokenType.LEGACY -> Bandwidth.classic(40, Refill.intervally(40, Duration.ofHours(1)))
    }
  }

  fun resolveRateLimitFromIp(remoteAddr: String): Bandwidth {
    return Bandwidth.classic(80, Refill.intervally(80, Duration.ofHours(1)))
  }
}
