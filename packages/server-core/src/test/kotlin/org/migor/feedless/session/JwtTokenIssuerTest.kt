package org.migor.feedless.session

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.SignedJWT
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.data.jpa.userSecret.UserSecretEntity
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.*

class JwtTokenIssuerTest {

  private lateinit var jwtTokenIssuer: JwtTokenIssuer
  private lateinit var propertyService: PropertyService
  private lateinit var meterRegistry: MeterRegistry
  private val testJwtSecret = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm"

  @BeforeEach
  fun setUp() {
    propertyService = mock(PropertyService::class.java)
    meterRegistry = SimpleMeterRegistry()

    `when`(propertyService.jwtSecret).thenReturn(testJwtSecret)
    `when`(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")

    jwtTokenIssuer = JwtTokenIssuer(
      propertyService = propertyService,
      meterRegistry = meterRegistry,
      tokenAnonymousValidForDays = "1",
      defaultTokenAnonymousValidForDays = "1"
    )
    jwtTokenIssuer.postConstruct()
  }

  @Test
  fun `createJwtForAnonymous creates a properly signed JWT`() = runTest {
    // when
    val jwt = jwtTokenIssuer.createJwtForAnonymous()

    // then
    assertThat(jwt.tokenValue).isNotNull()
    assertThat(jwt.tokenValue).isNotEmpty()

    // Verify the JWT is properly signed
    val signedJWT = SignedJWT.parse(jwt.tokenValue)
    assertThat(signedJWT.header.algorithm).isEqualTo(JWSAlgorithm.HS256)

    // Verify signature with the secret key
    val verifier: JWSVerifier = MACVerifier(testJwtSecret.toByteArray())
    assertThat(signedJWT.verify(verifier)).isTrue()

    // Verify token type claim
    assertThat(signedJWT.jwtClaimsSet.getClaim(JwtParameterNames.TYPE))
      .isEqualTo(AuthTokenType.ANONYMOUS.value)
  }

  @Test
  fun `createJwtForCapabilities creates a properly signed JWT`() = runTest {
    // given
    val userId = UserId(UUID.randomUUID())
    val capabilities = listOf(UserCapability(userId))

    // when
    val jwt = jwtTokenIssuer.createJwtForCapabilities(capabilities)

    // then
    assertThat(jwt.tokenValue).isNotNull()
    assertThat(jwt.tokenValue).isNotEmpty()

    // Verify the JWT is properly signed
    val signedJWT = SignedJWT.parse(jwt.tokenValue)
    assertThat(signedJWT.header.algorithm).isEqualTo(JWSAlgorithm.HS256)

    // Verify signature with the secret key
    val verifier: JWSVerifier = MACVerifier(testJwtSecret.toByteArray())
    assertThat(signedJWT.verify(verifier)).isTrue()

    // Verify token type claim
    assertThat(signedJWT.jwtClaimsSet.getClaim(JwtParameterNames.TYPE))
      .isEqualTo(AuthTokenType.USER.value)

    // Verify capabilities claim exists
    assertThat(signedJWT.jwtClaimsSet.getClaim(JwtParameterNames.CAPABILITIES)).isNotNull()
  }

  @Test
  fun `createJwtForApi creates a properly signed JWT`() = runTest {
    // given
    val userEntity = mock(UserEntity::class.java)
    `when`(userEntity.id).thenReturn(UUID.randomUUID())

    // when
    val jwt = jwtTokenIssuer.createJwtForApi(userEntity)

    // then
    assertThat(jwt.tokenValue).isNotNull()
    assertThat(jwt.tokenValue).isNotEmpty()

    // Verify the JWT is properly signed
    val signedJWT = SignedJWT.parse(jwt.tokenValue)
    assertThat(signedJWT.header.algorithm).isEqualTo(JWSAlgorithm.HS256)

    // Verify signature with the secret key
    val verifier: JWSVerifier = MACVerifier(testJwtSecret.toByteArray())
    assertThat(signedJWT.verify(verifier)).isTrue()

    // Verify token type claim
    assertThat(signedJWT.jwtClaimsSet.getClaim(JwtParameterNames.TYPE))
      .isEqualTo(AuthTokenType.API.value)
  }

  @Test
  fun `createJwtForService creates a properly signed JWT`() = runTest {
    // given
    val userSecretEntity = mock(UserSecretEntity::class.java)
    `when`(userSecretEntity.ownerId).thenReturn(UUID.randomUUID())

    // when
    val jwt = jwtTokenIssuer.createJwtForService(userSecretEntity)

    // then
    assertThat(jwt.tokenValue).isNotNull()
    assertThat(jwt.tokenValue).isNotEmpty()

    // Verify the JWT is properly signed
    val signedJWT = SignedJWT.parse(jwt.tokenValue)
    assertThat(signedJWT.header.algorithm).isEqualTo(JWSAlgorithm.HS256)

    // Verify signature with the secret key
    val verifier: JWSVerifier = MACVerifier(testJwtSecret.toByteArray())
    assertThat(signedJWT.verify(verifier)).isTrue()

    // Verify token type claim
    assertThat(signedJWT.jwtClaimsSet.getClaim(JwtParameterNames.TYPE))
      .isEqualTo(AuthTokenType.SERVICE.value)
  }

  @Test
  fun `JWT signature fails with wrong secret`() = runTest {
    // when
    val jwt = jwtTokenIssuer.createJwtForAnonymous()

    // then
    val signedJWT = SignedJWT.parse(jwt.tokenValue)

    // Verify signature fails with wrong secret
    val wrongSecret = "wrong-secret-key-that-is-different-from-the-original"
    val verifier: JWSVerifier = MACVerifier(wrongSecret.toByteArray())
    assertThat(signedJWT.verify(verifier)).isFalse()
  }

  @Test
  fun `JWT contains required claims`() = runTest {
    // when
    val jwt = jwtTokenIssuer.createJwtForAnonymous()

    // then
    val signedJWT = SignedJWT.parse(jwt.tokenValue)
    val claims = signedJWT.jwtClaimsSet

    // Verify required claims exist
    assertThat(claims.getClaim(JwtParameterNames.ID)).isNotNull()
    assertThat(claims.getClaim(JwtParameterNames.IAT)).isNotNull()
    assertThat(claims.getClaim(JwtParameterNames.EXP)).isNotNull()
    assertThat(claims.getClaim(JwtParameterNames.TYPE)).isNotNull()
    assertThat(claims.getClaim(JwtParameterNames.CAPABILITIES)).isNotNull()
    assertThat(claims.issuer).isEqualTo("http://localhost:8080")
  }

  @Test
  fun `JWT has proper expiration time`() = runTest {
    // when
    val jwt = jwtTokenIssuer.createJwtForAnonymous()

    // then
    assertThat(jwt.expiresAt).isNotNull()
    assertThat(jwt.expiresAt?.epochSecond).isGreaterThan(jwt.issuedAt?.epochSecond ?: 0)
  }
}

