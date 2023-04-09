package org.migor.rich.rss.service

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Refill
import org.migor.rich.rss.auth.AuthTokenType
import org.migor.rich.rss.auth.JwtParameterNames
import org.migor.rich.rss.data.jpa.models.PlanEntity
import org.migor.rich.rss.data.jpa.repositories.PlanDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class PlanService {
  private val log = LoggerFactory.getLogger(PlanService::class.simpleName)

  @Autowired
  lateinit var planDAO: PlanDAO

  fun resolveRateLimitFromApiKey(token: Jwt): Bandwidth {
    return when (AuthTokenType.valueOf(token.getClaim<String>(JwtParameterNames.TYPE).uppercase())) {
      AuthTokenType.AGENT -> Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofMinutes(10)))
      AuthTokenType.USER -> Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofMinutes(20)))
      AuthTokenType.ANON -> Bandwidth.classic(10000, Refill.intervally(40, Duration.ofMinutes(20)))
    }
  }

  fun resolveRateLimitFromIp(remoteAddr: String): Bandwidth {
    return Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(1)))
  }

  fun findAll(): List<PlanEntity> {
    return planDAO.findAll()
  }

  fun findById(id: String): Optional<PlanEntity> {
    return planDAO.findById(UUID.fromString(id))
  }
}
