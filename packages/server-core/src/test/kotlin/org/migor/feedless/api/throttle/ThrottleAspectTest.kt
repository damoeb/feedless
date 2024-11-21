package org.migor.feedless.api.throttle

import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableSecurityConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentService
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.license.LicenseService
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient

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
@MockBeans(
  value = [
    MockBean(LicenseService::class),
    MockBean(UserService::class),
    MockBean(DocumentService::class),
    MockBean(HttpService::class),
    MockBean(PropertyService::class),
    MockBean(PermissionService::class),
    MockBean(UserSecretService::class),
    MockBean(UserDAO::class),
  ]
)
@Import(DisableDatabaseConfiguration::class, DisableSecurityConfiguration::class, DisableWebSocketsConfiguration::class)
class ThrottleAspectTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @MockBean
  lateinit var authService: AuthService

  @MockBean
  lateinit var tokenProvider: TokenProvider

  @LocalServerPort
  private var port: Int = 0

  @BeforeEach
  fun setUp() {

    runTest {
      val jwt = mock(Jwt::class.java)
      `when`(jwt.tokenValue).thenReturn("jwt")
      `when`(tokenProvider.createJwtForAnonymous()).thenReturn(jwt)
    }
    `when`(authService.isWhitelisted(any2())).thenReturn(false)

    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun `throttle kicks in`() = runTest {
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

