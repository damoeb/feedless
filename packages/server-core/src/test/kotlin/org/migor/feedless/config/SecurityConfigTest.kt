package org.migor.feedless.config

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentService
import org.migor.feedless.license.LicenseService
import org.migor.feedless.mail.MailProviderService
import org.migor.feedless.user.UserService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


const val actuatorPassword = "password"
const val feedId = "feed-id"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
)
@MockBeans(
  value = [
    MockBean(UserService::class),
    MockBean(LicenseService::class),
    MockBean(DocumentService::class),
    MockBean(MailProviderService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
@ActiveProfiles(profiles = ["test", AppProfiles.api, AppProfiles.feed, "metrics"])
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
  @CsvSource(value = [
    "graphql",
    "subscriptions",
  ])
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
