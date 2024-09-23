package org.migor.feedless.api.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.common.HttpService
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.session.PermissionService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

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
@MockBeans(
  MockBean(HttpService::class),
  MockBean(UserService::class),
  MockBean(UserDAO::class),
  MockBean(ServerConfigResolver::class),
  MockBean(UserSecretService::class),
  MockBean(PermissionService::class),
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableWebSocketsConfiguration::class,
)
class QueryResolverTest {

  @Autowired
  lateinit var dgsQueryExecutor: DgsQueryExecutor

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

}
