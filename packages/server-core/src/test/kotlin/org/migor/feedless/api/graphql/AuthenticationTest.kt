package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.license.LicenseService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.OrderService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.secrets.UserSecretEntity
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.reactive.function.client.WebClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = [
  "test",
  AppProfiles.api,
  AppProfiles.database,
  AppProfiles.authRoot,
])
@MockBeans(
  value = [
    MockBean(PluginService::class),
    MockBean(LicenseService::class),
    MockBean(ProductService::class),
    MockBean(OrderService::class),
    MockBean(ScrapeService::class)
])
@Testcontainers
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
  fun `authAnonymous works`() {
    val response = monoGraphQLClient.reactiveExecuteQuery(
      """
        mutation {
            authAnonymous {
                token
            }
        }
        """.trimIndent()
    ).blockOptional()
      .orElseThrow()
      .extractValue<LinkedHashMap<String, Any>>("data.authAnonymous")

    val auth = ObjectMapper().convertValue(response, Authentication::class.java)

    Assertions.assertThat(auth.token).isNotEmpty()
  }

  @Test
  fun `authUser works`() {
    // given
    val mockUser = UserEntity()
    mockUser.root = true
    Mockito.`when`(userService.findByEmail(anyString())).thenReturn(mockUser)
    val mockSecretKey = UserSecretEntity()
    Mockito.`when`(userSecretService.findBySecretKeyValue(anyString(), anyString())).thenReturn(mockSecretKey)

    // when
    val response = monoGraphQLClient.reactiveExecuteQuery(
      """
        mutation {
            authUser(data: {email: "fooEmail", secretKey: "barKey"}) {
                token
            }
        }
        """.trimIndent()
    ).blockOptional()
      .orElseThrow()
      .extractValue<LinkedHashMap<String, Any>>("data.authUser")

    // then
    val auth = ObjectMapper().convertValue(response, Authentication::class.java)
    Assertions.assertThat(auth.token).isNotEmpty()
  }

  companion object {

    @Container
    private val postgres = PostgreSQLContainer("postgres:15")
      .withDatabaseName("feedless")
      .withUsername("postgres")
      .withPassword("admin")

    @JvmStatic
    @DynamicPropertySource
    fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { "jdbc:tc:postgresql:15://localhost:${postgres.firstMappedPort}/${postgres.databaseName}?TC_REUSABLE=true" }
      registry.add("spring.datasource.username", postgres::getUsername)
      registry.add("spring.datasource.password", postgres::getPassword)
    }
  }
}
