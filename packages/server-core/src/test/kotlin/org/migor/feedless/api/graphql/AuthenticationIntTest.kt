package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.session.PermissionService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClient


const val rootEmail = "fooEmail"
const val rootSecretKey = "barBarBarKey"

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.rootEmail=$rootEmail",
    "app.rootSecretKey=$rootSecretKey",
    "app.apiGatewayUrl=https://localhost",
    "app.actuatorPassword=s3cr3t",
  ],
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.authRoot,
  AppProfiles.session,
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    PermissionService::class,
  ]
)
@Import(DisableDatabaseConfiguration::class)
class AuthenticationTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockitoBean
  lateinit var userService: UserService

  @MockitoBean
  lateinit var userSecretService: UserSecretService

  @BeforeEach
  fun setUp() {
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun `authAnonymous works`() = runTest {
    val graphQLMutation = DgsClient.buildMutation {
      authAnonymous {
        token
        corrId
      }
    }

    val response = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
      .extractValue<LinkedHashMap<String, Any>>("data.authAnonymous")

    val auth = ObjectMapper().convertValue(response, Map::class.java)

    assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isNotEmpty()
  }

  @Test
  fun `authUser works`() = runTest {
    // given
    val mockUser = UserEntity()
    mockUser.admin = true
    Mockito.`when`(userService.findByEmail(anyString())).thenReturn(mockUser)
    val mockSecretKey = UserSecretEntity()
    Mockito.`when`(userSecretService.findBySecretKeyValue(anyString(), anyString())).thenReturn(mockSecretKey)

    // when
    val graphQLMutation = DgsClient.buildMutation {
      authUser(data = AuthUserInput(email = rootEmail, secretKey = rootSecretKey)) {
        token
        corrId
      }
    }

    val response = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
      .extractValue<LinkedHashMap<String, Any>>("data.authUser")

    // then
    val auth = ObjectMapper().convertValue(response, Map::class.java)
    assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isNotEmpty()
  }
}
