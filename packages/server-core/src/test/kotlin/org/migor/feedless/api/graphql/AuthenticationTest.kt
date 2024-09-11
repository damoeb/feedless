package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
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
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
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
@MockBeans(
    MockBean(ServerConfigResolver::class),
    MockBean(UserDAO::class),
)
@Import(DisableDatabaseConfiguration::class)
//@Testcontainers
class AuthenticationTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockBean
  lateinit var userService: UserService

  @MockBean
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

    Assertions.assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isNotEmpty()
  }

  @Test
  fun `authUser works`() = runTest {
    // given
    val mockUser = UserEntity()
    mockUser.root = true
    Mockito.`when`(userService.findByEmail(anyString())).thenReturn(mockUser)
    val mockSecretKey = UserSecretEntity()
    Mockito.`when`(userSecretService.findBySecretKeyValue(anyString(), anyString())).thenReturn(mockSecretKey)

    // when
    val graphQLMutation = DgsClient.buildMutation {
      authUser(data = AuthUserInput(email = "fooEmail", secretKey = "barKey")) {
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
    Assertions.assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isNotEmpty()
  }

//  companion object {
//
//    @Container
//    private val postgres = PostgreSQLContainer("postgres:15")
//      .withDatabaseName("feedless")
//      .withUsername("postgres")
//      .withPassword("admin")
//
//    @JvmStatic
//    @DynamicPropertySource
//    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
//      registry.add("spring.datasource.url") { "jdbc:tc:postgresql:15://localhost:${postgres.firstMappedPort}/${postgres.databaseName}?TC_REUSABLE=true" }
//      registry.add("spring.datasource.username", postgres::getUsername)
//      registry.add("spring.datasource.password", postgres::getPassword)
//    }
//  }
}
