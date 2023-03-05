package org.migor.rich.rss.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  fun resolveRateLimitFromApiKey(token: OAuth2AuthenticationToken): Bandwidth {
    val tokenType = token.principal.attributes[JwtParameterNames.TYPE] as AuthTokenType
    return when (tokenType) {
      AuthTokenType.USER -> Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofMinutes(20)))
      AuthTokenType.ANON -> Bandwidth.classic(10000, Refill.intervally(40, Duration.ofMinutes(20)))
      else -> throw RuntimeException("type ${tokenType} cannot be throttled")
    }
  }

  fun resolveRateLimitFromIp(remoteAddr: String): Bandwidth {
    log.info("rateLimit ip $remoteAddr")
    return Bandwidth.classic(80, Refill.intervally(80, Duration.ofMinutes(10)))
  }
}
