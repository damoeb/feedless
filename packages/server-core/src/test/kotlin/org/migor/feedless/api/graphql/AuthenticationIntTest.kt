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
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthUserInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.PermissionService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClient
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


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
class AuthenticationIntTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockitoBean
  lateinit var userRepository: UserRepository

  @MockitoBean
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var propertyService: PropertyService

  @MockitoBean
  lateinit var userSecretRepository: UserSecretRepository

  private lateinit var jwtDecoder: JwtDecoder

  @MockitoBean
  lateinit var authorizedClientService: OAuth2AuthorizedClientService

  @BeforeEach
  fun setUp() {
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)

    // Create JWT decoder with the same secret key used to sign tokens
    val secretKey: SecretKey = SecretKeySpec(propertyService.jwtSecret.encodeToByteArray(), "HmacSHA256")
    this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).build()
  }

  @Test
  fun `authAnonymous works`() = runTest {
    val token = monoGraphQLClient.requestAnonymousJwt()

    assertThat(token).isNotEmpty()
  }

  @Test
  fun `authUser works`() = runTest {
    // given
    val mockUser = mock(User::class.java)
    `when`(mockUser.admin).thenReturn(true)
    `when`(userRepository.findByEmail(anyString())).thenReturn(mockUser)
    val mockSecretKey = mock(UserSecret::class.java)
    `when`(userSecretRepository.findBySecretKeyValue(anyString(), anyString())).thenReturn(mockSecretKey)

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
    val actualToken = auth[DgsConstants.AUTHENTICATION.Token] as String
    assertThat(actualToken).isNotEmpty()

    // Verify the JWT is valid and can be decoded
    val jwt = jwtDecoder.decode(actualToken)
    assertThat(jwt).isNotNull()
    assertThat(jwt.tokenValue).isEqualTo(actualToken)
    assertThat(jwt.issuer.toString()).isEqualTo(propertyService.apiGatewayUrl)
  }
}

suspend fun WebClientGraphQLClient.requestAnonymousJwt(): String {
  val graphQLMutation = DgsClient.buildMutation {
    authAnonymous {
      token
      corrId
    }
  }

  val response = this.reactiveExecuteQuery(graphQLMutation)
    .toFuture()
    .await()
    .extractValue<LinkedHashMap<String, Any>>("data.authAnonymous")

  val auth = ObjectMapper().convertValue(response, Map::class.java)
  val token = auth[DgsConstants.AUTHENTICATION.Token] as String

  return token
}
