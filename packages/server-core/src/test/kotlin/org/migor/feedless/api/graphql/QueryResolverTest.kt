package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.Session
import org.migor.feedless.license.LicenseService
import org.migor.feedless.pipeline.PluginService
import org.migor.feedless.plan.OrderService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.service.ScrapeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

// see https://netflix.github.io/dgs/query-execution-testing/

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.api, AppProfiles.database])
@MockBeans(value = [
  MockBean(PluginService::class),
  MockBean(ScrapeService::class),
  MockBean(LicenseService::class),
  MockBean(ProductService::class),
  MockBean(OrderService::class),
])
@Testcontainers
class QueryResolverTest {

  @Autowired
  lateinit var dgsQueryExecutor: DgsQueryExecutor

  @Test
  fun `fetchProfile for anonymous works`() {
    val response = dgsQueryExecutor.executeAndExtractJsonPath<HashMap<String, Any>>(
      """
            query {
                session {
                    isAnonymous
                    userId
                }
            }
        """.trimIndent(),
      "data.session"
    )

    val session = ObjectMapper().convertValue(response, Session::class.java)

    Assertions.assertThat(session.isAnonymous).isTrue()
    Assertions.assertThat(session.isLoggedIn).isFalse()
    Assertions.assertThat(session.userId).isBlank()
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
