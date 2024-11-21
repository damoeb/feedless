package org.migor.feedless.session

import com.nimbusds.jose.jwk.source.ImmutableSecret
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import jakarta.annotation.PostConstruct
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.user.UserEntity
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
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.properties.Delegates
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class TokenProvider(
  val propertyService: PropertyService,
  val meterRegistry: MeterRegistry,
  @Value("\${auth.token.anonymous.validForDays}")
  val tokenAnonymousValidForDays: String,
  @Value("\${default.auth.token.anonymous.validForDays}")
  val defaultTokenAnonymousValidForDays: String
) {
  private val log = LoggerFactory.getLogger(TokenProvider::class.simpleName)

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

  suspend fun createJwtForAnonymous(): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "anonymous"))).increment()
    log.debug("signedToken for anonymous")
    return encodeJwt(
      mapOf(
        JwtParameterNames.TYPE to AuthTokenType.ANON.value,
        JwtParameterNames.USER_ID to "",
        attrAuthorities to toAuthorities(listOf(Authority.ANONYMOUS)),
      ),
      Duration.ofHours(12)
    )
  }

  suspend fun createJwtForUser(user: UserEntity): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "user"))).increment()
    log.debug("signedToken for user")
    return encodeJwt(
      mapOf(
        JwtParameterNames.USER_ID to user.id.toString(),
        JwtParameterNames.TYPE to AuthTokenType.USER.value,
//        JwtParameterNames.REMOTE_ADDR to "0.0.0.0", // todo add remote addr
        attrAuthorities to toAuthorities(
          listOf(
            Authority.ANONYMOUS,
            Authority.USER
          )
        ),
      ),
      getUserTokenExpiration()
    )
  }

  suspend fun createJwtForApi(user: UserEntity): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "api"))).increment()
    log.debug("signedToken for api")
    return encodeJwt(
      mapOf(
        JwtParameterNames.USER_ID to user.id.toString(),
        JwtParameterNames.TYPE to AuthTokenType.API.value,
//        JwtParameterNames.REMOTE_ADDR to "0.0.0.0", // todo add remote addr
        attrAuthorities to toAuthorities(
          listOf(
            Authority.ANONYMOUS
          )
        ),
      ),
      getApiTokenExpiration()
    )
  }

  suspend fun createJwtForAgent(securityKey: UserSecretEntity): Jwt {
    meterRegistry.counter(AppMetrics.issueToken, listOf(Tag.of("type", "agent"))).increment()
    log.debug("signedToken for agent")
    return encodeJwt(
      mapOf(
        JwtParameterNames.USER_ID to securityKey.ownerId.toString(),
        JwtParameterNames.TYPE to AuthTokenType.AGENT.value,
        attrAuthorities to toAuthorities(
          listOf(
            Authority.PROVIDE_HTTP_RESPONSE
          )
        ),
      ),
      Duration.ofDays(356)
    )
  }

  private suspend fun encodeJwt(claims: Map<String, Any>, expiresIn: Duration): Jwt {
    // https://en.wikipedia.org/wiki/JSON_Web_Token
    val jwsHeader = JwsHeader.with { "HS256" }.build()
    val claimsSet = JwtClaimsSet.builder()
      .issuer(propertyService.apiGatewayUrl)
      .claims { c -> c.putAll(claims) }
      .claims { c -> c[JwtParameterNames.ID] = "rich" }
      .claims { c -> c[JwtParameterNames.IAT] = LocalDateTime.now().toMillis() }
      .claims { c ->
        c[JwtParameterNames.EXP] = LocalDateTime.now().plus(expiresIn).toMillis()
      }
      .build()
    val params = JwtEncoderParameters.from(jwsHeader, claimsSet)
    return NimbusJwtEncoder(ImmutableSecret(getSecretKey()))
      .encode(params)
  }

  fun getUserTokenExpiration(): Duration = Duration.ofHours(48) // todo from proverties
  fun getApiTokenExpiration(): Duration = Duration.ofDays(356)

  private fun getSecretKey(): SecretKey {
    return SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
  }

  private fun toAuthorities(authorities: List<Authority>): List<String> {
    return authorities.map { it.name }
  }


}
