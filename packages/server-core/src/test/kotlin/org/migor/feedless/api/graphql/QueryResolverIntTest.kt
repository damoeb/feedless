package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.common.HttpService
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.secrets.UserSecretUseCase
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

// see https://netflix.github.io/dgs/query-execution-testing/

@SpringBootTest
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppLayer.security,
  AppProfiles.session,
  AppProfiles.properties,
)
@MockitoBean(
  types = [
    HttpService::class,
    UserUseCase::class,
    UserRepository::class,
    ServerConfigResolver::class,
    UserSecretUseCase::class,
    UserSecretRepository::class,
    UserGuard::class,
    UserGroupAssignmentRepository::class,
    OAuth2AuthorizedClientService::class
  ]
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableWebSocketsConfiguration::class,
)
class QueryResolverIntTest {

  @Autowired
  lateinit var dgsQueryExecutor: DgsQueryExecutor

  @BeforeEach
  fun setUp() {
    mockSecurityContext()
  }

  @Test
  fun `fetchProfile for anonymous works`() {
    val graphQLQuery = DgsClient.buildQuery {
      session {
        isAnonymous
        userId
        isLoggedIn
      }
    }
    val response = dgsQueryExecutor.executeAndExtractJsonPath<HashMap<String, Any>>(graphQLQuery, "data.session")

    val session = ObjectMapper().convertValue(response, Map::class.java)

    assertThat(session[DgsConstants.SESSION.IsAnonymous] as Boolean).isTrue()
    assertThat(session[DgsConstants.SESSION.IsLoggedIn] as Boolean).isFalse()
    assertThat(session[DgsConstants.SESSION.UserId] as String?).isBlank()
  }

  private fun mockSecurityContext() {
    val authentication = TestingAuthenticationToken("", "", "WRITE")
    val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
    `when`(securityContext.authentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }
}
