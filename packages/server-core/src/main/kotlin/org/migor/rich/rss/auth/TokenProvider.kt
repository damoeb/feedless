package org.migor.rich.rss.auth

import com.nimbusds.jose.jwk.source.ImmutableSecret
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
class TokenProvider {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val attrAuthorities = "authorities"

  @PostConstruct
  fun postConstruct() {
    tokenAnonymousValidFor = parseDuration(tokenAnonymousValidForDays, defaultTokenAnonymousValidForDays)
    log.info("tokenAnonymousValidFor=${tokenAnonymousValidFor}")
  }

  private fun parseDuration(actual: String, fallback: String) = runCatching {
    actual.toLong().toDuration(DurationUnit.DAYS).inWholeMinutes
  }.getOrElse { fallback.toLong() }

  fun createJwtForAnonymous(): Jwt {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "anonymous"))).increment()
    log.debug("signedToken for anonymous")
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.ANON.value,
        JwtParameterNames.USER_ID to "",
        attrAuthorities to toAuthorities(listOf(Authority.READ)),
      )
    )
  }

  fun createJwtForUser(user: UserEntity): Jwt {
    meterRegistry.counter("issue-token", listOf(Tag.of("type", "user"))).increment()
    log.debug("signedToken for user")
    return encodeJwt(
      mapOf(
        JwtParameterNames.USER_ID to user.id.toString(),
        JwtParameterNames.TYPE to AuthTokenType.USER.value,
//        JwtParameterNames.REMOTE_ADDR to "0.0.0.0", // todo add remote addr
        attrAuthorities to toAuthorities(listOf(
          Authority.READ,
          Authority.WRITE
        )),
      ),
    )
  }

  private fun encodeJwt(claims: Map<String, Any>): Jwt {
    // https://en.wikipedia.org/wiki/JSON_Web_Token
    val jwsHeader = JwsHeader.with { "HS256" }.build()
    val claimsSet = JwtClaimsSet.builder()
      .issuer(propertyService.publicUrl)
      .claims { c -> c.putAll(claims) }
      .claims { c -> c[JwtParameterNames.ID] = "rich" }
      .claims { c -> c[JwtParameterNames.IAT] = LocalDateTime.now().atZone(ZoneId.systemDefault()).toEpochSecond() }
      .claims { c ->
        c[JwtParameterNames.EXP] = LocalDateTime.now().plus(getTokenExpiration()).atZone(ZoneId.systemDefault()).toEpochSecond()
      }
      .build()
    val params = JwtEncoderParameters.from(jwsHeader, claimsSet)
    return NimbusJwtEncoder(ImmutableSecret(getSecretKey()))
      .encode(params)
  }

  fun getTokenExpiration(): Duration = Duration.ofHours(48) // todo from proverties

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }

  private fun toAuthorities(authorities: List<Authority>): List<String> {
    return authorities.map { it.name }
  }


}
