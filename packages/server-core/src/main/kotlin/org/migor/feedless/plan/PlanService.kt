package org.migor.feedless.plan

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtParameterNames
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
@Profile(AppProfiles.database)
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  @Autowired
  private lateinit var featureGroupDAO: FeatureGroupDAO

  fun resolveRateLimitFromApiKey(token: Jwt): Bandwidth {
    return when (AuthTokenType.valueOf(token.getClaim<String>(JwtParameterNames.TYPE).uppercase())) {
      AuthTokenType.AGENT -> Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)))
      AuthTokenType.USER -> Bandwidth.classic(300, Refill.intervally(300, Duration.ofMinutes(1)))
      AuthTokenType.API -> Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
      AuthTokenType.ANON -> Bandwidth.classic(200, Refill.intervally(200, Duration.ofMinutes(1)))
    }
  }

  fun resolveRateLimitFromIp(remoteAddr: String): Bandwidth {
    return Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))
  }

  fun findById(id: String): Optional<FeatureGroupEntity> {
    return featureGroupDAO.findById(UUID.fromString(id))
  }
}
