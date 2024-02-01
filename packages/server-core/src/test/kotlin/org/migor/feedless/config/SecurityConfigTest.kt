package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.data.jpa.repositories.AgentDAO
import org.migor.feedless.data.jpa.repositories.OneTimePasswordDAO
import org.migor.feedless.service.UserSecretService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.shaded.com.github.dockerjava.core.MediaType


const val actuatorPassword = "password"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
)
@MockBeans(value = [MockBean(AgentDAO::class), MockBean(UserSecretService::class), MockBean(OneTimePasswordDAO::class)])
@ActiveProfiles(profiles = ["test", "metrics"])
//@TestPropertySource(locations= ["classpath:application-test.properties"])
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
    assertEquals(actuatorResponse.statusCode, HttpStatus.UNAUTHORIZED)
    val prometheusResponse = restTemplate.getForEntity(prometheusEndpoint, String::class.java)
    assertEquals(prometheusResponse.statusCode, HttpStatus.UNAUTHORIZED)
  }

  @Test
  fun whenRequestingActuatorWithAuth_ThenSuccess() {
    val restTemplate = TestRestTemplate("actuator", actuatorPassword)
    val actuatorResponse = restTemplate.getForEntity(actuatorEndpoint, String::class.java)
    assertEquals(actuatorResponse.statusCode, HttpStatus.OK)
//    val prometheusResponse = restTemplate.getForEntity(prometheusEndpoint, String::class.java)
//    assertEquals(prometheusResponse.statusCode, HttpStatus.OK)
  }

  @Test
  fun whenCallingWhitelistedUrl_ThenSuccess() {
    val restTemplate = TestRestTemplate()
    arrayOf("graphql", "subscriptions").forEach {
      val response = restTemplate.postForEntity("${baseEndpoint}/$it", "", String::class.java)
      assertNotEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Test
  fun whenCallingLegacyUrls_ThenSuccess() {
    val restTemplate = TestRestTemplate()
    arrayOf(
      "stream/feed/2",
      "stream/feed/",
//      "api/legacy/foo", // TODO fix cause not whitelisted
      "/stream/feed/foo",
      "/stream/bucket/foo",
      "/feed/foo",
      "/feed:foo",
      ApiUrls.transformFeed,
      ApiUrls.webToFeed,
      ApiUrls.webToFeedVerbose,
      ApiUrls.webToFeedFromRule,
      ApiUrls.webToFeedFromChange,
      "/bucket/foo",
      "/bucket:foo",
//      "/f/foo"
    ).forEach {
      val response = restTemplate.getForEntity("${baseEndpoint}/$it", String::class.java)
      assertEquals(HttpStatus.OK, response.statusCode)
      assertEquals("application", response.headers.contentType?.type)
      assertEquals("xml", response.headers.contentType?.subtype)
    }
  }
}
