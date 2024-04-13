package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.types.Session
import org.migor.feedless.session.SessionService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

// see https://netflix.github.io/dgs/query-execution-testing/

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.database])
@MockBeans(value = [MockBean(JavaMailSender::class), MockBean(SessionService::class)])
@Testcontainers
class QueryResolverTest {

    @Autowired
    lateinit var dgsQueryExecutor: DgsQueryExecutor

    @Test
    fun `fetchProfile for anonymous works`() {
        val response = dgsQueryExecutor.executeAndExtractJsonPath<HashMap<String, Any>>(
            """
            query {
                profile {
                    isAnonymous
                    userId
                }
            }
        """.trimIndent(),
            "data.profile"
        )

        val profile = ObjectMapper().convertValue(response, Session::class.java)

        Assertions.assertThat(profile.isAnonymous).isTrue()
        Assertions.assertThat(profile.isLoggedIn).isFalse()
        Assertions.assertThat(profile.userId).isBlank()
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
