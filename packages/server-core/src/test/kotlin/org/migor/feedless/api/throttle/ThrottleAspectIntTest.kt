package org.migor.feedless.api.throttle

import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableSecurityConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.any2
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.license.LicenseUseCase
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.secrets.UserSecretUseCase
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.PermissionService
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
  profiles = [
    "test",
    AppLayer.api,
    AppLayer.service,
    AppProfiles.session,
    AppProfiles.security,
    AppProfiles.throttle,
  ]
)
@MockitoBean(
  types = [
    LicenseUseCase::class,
    UserUseCase::class,
    DocumentUseCase::class,
    HttpService::class,
    PropertyService::class,
    PermissionService::class,
    UserSecretUseCase::class,
    UserRepository::class,
    OneTimePasswordService::class,
  ]
)
@Import(DisableDatabaseConfiguration::class, DisableSecurityConfiguration::class, DisableWebSocketsConfiguration::class)
class ThrottleAspectIntTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @MockitoBean
  lateinit var authService: AuthService

  @MockitoBean
  lateinit var jwtTokenIssuer: JwtTokenIssuer

  @LocalServerPort
  private var port: Int = 0

  @BeforeEach
  fun setUp() {

    runTest {
      val jwt = mock(Jwt::class.java)
      `when`(jwt.tokenValue).thenReturn("jwt")
      `when`(jwt.expiresAt).thenReturn(LocalDateTime.now().toInstant(ZoneOffset.UTC))
      `when`(jwtTokenIssuer.createJwtForAnonymous()).thenReturn(jwt)
      `when`(jwtTokenIssuer.getExpiration(any2())).thenReturn(2.hours)
      `when`(authService.isWhitelisted(any2())).thenReturn(false)
    }
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun `throttle kicks in`() = runTest(timeout = 60.seconds) {
    val graphQLMutation = DgsClient.buildMutation {
      authAnonymous {
        token
        corrId
      }
    }

    val responses = (1..101).map {
      monoGraphQLClient.reactiveExecuteQuery(graphQLMutation).blockOptional()
        .orElseThrow()
    }

    val lastResponse = responses.last()
    assertThat(responses.dropLast(1).none { it.hasErrors() }).isTrue()
    assertThat(lastResponse.errors.size).isEqualTo(1)
    assertThat(lastResponse.errors.first().message).contains("HostOverloadingException")
  }
}

