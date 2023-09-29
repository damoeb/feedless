package org.migor.feedless.config

import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

const val actuatorPassword = "password"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"]
)
@ActiveProfiles(profiles = ["test", "metrics"])
//@TestPropertySource(locations= ["classpath:application-test.properties"])
class SecurityConfigTest {

  lateinit var actuatorEndpoint: String
  lateinit var prometheusEndpoint: String

  @LocalServerPort
  var port = 0

  @BeforeEach
  fun setUp() {
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
}
