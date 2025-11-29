package org.migor.feedless.session

import com.google.gson.Gson
import com.nimbusds.jose.jwk.source.ImmutableSecret
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.AgentCapability
import org.migor.feedless.capability.Capability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration


@Service
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class JwtTokenIssuer(
  val propertyService: PropertyService,
  val meterRegistry: MeterRegistry,
  @Value("\${auth.token.anonymous.validForDays}")
  val tokenAnonymousValidForDays: String,
  @Value("\${default.auth.token.anonymous.validForDays}")
  val defaultTokenAnonymousValidForDays: String
) {
  private val log = LoggerFactory.getLogger(JwtTokenIssuer::class.simpleName)

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
  }

  fun createJwtForAnonymous(): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "anonymous"))).increment()
    log.debug("signedToken for anonymous")
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.ANONYMOUS.value,
        JwtParameterNames.CAPABILITIES to toAuthorities(listOf(UserCapability(UserId(UUID.randomUUID())))),
      ),
      getExpiration(AuthTokenType.ANONYMOUS)
    )
  }

  fun createJwtForCapabilities(capabilities: List<Capability<out Any>>): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "user"))).increment()
    log.debug("signedToken for user")
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.USER.value,
        JwtParameterNames.CAPABILITIES to capabilities.associate {
          it.capabilityId to Gson().toJson(it.capabilityPayload)
        },
      ),
      getExpiration(AuthTokenType.USER)
    )
  }

  fun createJwtForAnonymousFeed(host: String): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "api"))).increment()
    log.debug("signedToken for service")
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.ANONYMOUS.value,
        JwtParameterNames.HOST to host,
      ),
    )
  }

  fun createJwtForApi(user: User): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "api"))).increment()
    log.debug("signedToken for service")
    val capabilities: List<Capability<out Any>> = listOf(UserCapability(user.id));
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.API.value,
        JwtParameterNames.CAPABILITIES to toAuthorities(capabilities),
      ),
      getExpiration(AuthTokenType.SERVICE)
    )
  }

  fun createJwtForService(securityKey: UserSecret): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "agent"))).increment()
    log.debug("signedToken for agent")
    val capabilities: List<Capability<out Any>> = listOf(
      AgentCapability(""),
      UserCapability(securityKey.ownerId)
    );
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.SERVICE.value,
        JwtParameterNames.CAPABILITIES to toAuthorities(capabilities),
      ),
      getExpiration(AuthTokenType.SERVICE)
    )
  }

  fun getExpiration(authority: AuthTokenType): Duration {
    // todo from properties
    return when (authority) {
      AuthTokenType.ANONYMOUS -> 1.days
      AuthTokenType.USER -> 48.hours
      AuthTokenType.SERVICE -> 356.days
      AuthTokenType.API -> 48.hours
    }
  }

  private fun encodeJwt(claims: Map<String, Any>, expiresIn: Duration? = null): Jwt {
    // https://en.wikipedia.org/wiki/JSON_Web_Token
    val jwsHeader = JwsHeader.with { "HS256" }.build()
    var claimsSet = JwtClaimsSet.builder()
      .issuer(propertyService.apiGatewayUrl)
      .claims { c -> c.putAll(claims) }
      .claims { c -> c[JwtParameterNames.ID] = "feedless" }
      .claims { c -> c[JwtParameterNames.IAT] = LocalDateTime.now().toMillis() }
      .build()

    expiresIn?.let {
      claimsSet = JwtClaimsSet.from(claimsSet)
        .claims { c ->
          c[JwtParameterNames.EXP] = LocalDateTime.now().plus(expiresIn.toJavaDuration()).toMillis()
        }
        .build()
    }
    val params = JwtEncoderParameters.from(jwsHeader, claimsSet)
    return NimbusJwtEncoder(ImmutableSecret(getSecretKey()))
      .encode(params)
  }

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }

  private fun toAuthorities(capabilities: List<Capability<out Any>>): Map<String, String> {
    return capabilities.associate {
      it.capabilityId.value to Gson().toJson(it.capabilityPayload)
    }
  }

  private fun parseDuration(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }
}
