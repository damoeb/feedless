package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentController
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.SessionResolver
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


const val actuatorPassword = "password"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
)
@MockBeans(
  MockBean(ServerConfigResolver::class),
  MockBean(UserDAO::class),
  MockBean(UserSecretDAO::class),
  MockBean(UserService::class),
  MockBean(SessionResolver::class),
  MockBean(DocumentController::class),
  MockBean(PermissionService::class),
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.session,
  AppProfiles.feed,
  "metrics"
)
@Import(DisableDatabaseConfiguration::class)
class SecurityConfigTest {

  lateinit var baseEndpoint: String
  lateinit var actuatorEndpoint: String
  lateinit var prometheusEndpoint: String

  @LocalServerPort
  var port = 0

  @BeforeEach
  fun setUp() {
    baseEndpoint = "http://localhost:$port"
    actuatorEndpoint = "http://localhost:$port/actuator"
    prometheusEndpoint = "$actuatorEndpoint/prometheus"
  }

  @Test
  fun whenRequestingActuatorWithoutAuth_ThenFail() {
    val restTemplate = TestRestTemplate()
    val actuatorResponse = restTemplate.getForEntity(actuatorEndpoint, String::class.java)
    assertThat(actuatorResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    val prometheusResponse = restTemplate.getForEntity(prometheusEndpoint, String::class.java)
    assertThat(prometheusResponse.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun whenRequestingActuatorWithAuth_ThenSuccess() {
    val restTemplate = TestRestTemplate("actuator", actuatorPassword)
    val actuatorResponse = restTemplate.getForEntity(actuatorEndpoint, String::class.java)
    assertThat(actuatorResponse.statusCode).isEqualTo(HttpStatus.OK)
    // todo fix
//    val prometheusResponse = restTemplate.getForEntity(prometheusEndpoint, String::class.java)
//    assertEquals(prometheusResponse.statusCode, HttpStatus.OK)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "graphql",
      "subscriptions",
    ]
  )
  fun whenCallingWhitelistedUrl_ThenSuccess(inputPathPrefix: String) {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.postForEntity("${baseEndpoint}/$inputPathPrefix", "", String::class.java)
    assertThat(HttpStatus.FORBIDDEN).isNotEqualTo(response.statusCode)
  }

//  @ParameterizedTest
//  @CsvSource(value = [
////    "bucket/$feedId",
////    "bucket/$feedId/atom",
////    "bucket:$feedId",
////    "bucket:$feedId/atom",
////    "stream/bucket/$feedId",
////    "stream/bucket/$feedId/atom",
//    "stream/feed/$feedId",
//    "stream/feed/$feedId/atom",
//    "feed/$feedId",
//    "feed/$feedId/atom",
//    "feed:$feedId",
////    "feed:$feedId/atom",
//    ApiUrls.transformFeed,
//    ApiUrls.webToFeed,
////    "article/foo",
////    "a/foo",
//  ])
//  fun whenCallingUrl_ThenSuccess(path: String) {
//    val restTemplate = TestRestTemplate()
//    val response = restTemplate.getForEntity("${baseEndpoint}/$path", String::class.java)
//    assertEquals(HttpStatus.OK, response.statusCode)
//  }
}
