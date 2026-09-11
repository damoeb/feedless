package org.migor.feedless.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentController
import org.migor.feedless.feed.FeedService
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.SessionResolver
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension


const val actuatorPassword = "password"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    UserRepository::class,
    UserSecretRepository::class,
    UserUseCase::class,
    SessionResolver::class,
    DocumentController::class,
    UserGuard::class,
    OneTimePasswordService::class,
    OAuth2AuthorizedClientService::class,
    FeedService::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    GroupUseCasePort::class,
    AnalyticsService::class
  ]
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
class SecurityConfigIntTest {

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

  // kubelet probes carry no credentials
  @ParameterizedTest
  @CsvSource(
    value = [
      "health",
      "health/liveness",
      "health/readiness",
    ]
  )
  fun whenProbingHealthWithoutAuth_ThenSuccess(path: String) {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("$actuatorEndpoint/$path", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  /** A CLI file missing from this run still answers 404, not 401/403: the CLI downloads are public whether or not feedctl was built. */
  @ParameterizedTest
  @CsvSource(
    value = [
      "cli/install.sh",
      "cli/SHA256SUMS",
      "cli/feedctl-linux-amd64",
    ]
  )
  fun whenCallingCliUrl_ThenNotUnauthorized(path: String) {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("$baseEndpoint/$path", String::class.java)
    assertThat(response.statusCode).isNotEqualTo(HttpStatus.UNAUTHORIZED)
    assertThat(response.statusCode).isNotEqualTo(HttpStatus.FORBIDDEN)
  }

  /** Only the real filter chain proves the version filter runs before HttpApiJwtFilter's 401. */
  @Test
  fun whenRequestingApiV1WithoutAuth_ThenVersionHeaderPresentOn401() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("${baseEndpoint}/api/v1/repositories", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
  }

  /** No agent profile here, so the count must still answer, as 0. */
  @Test
  fun whenRequestingStatusWithoutAuth_ThenOkWithAllFields() {
    val response = TestRestTemplate().getForEntity("${baseEndpoint}/api/v1/status", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
    val body = ObjectMapper().readTree(response.body)
    assertThat(body.fieldNames().asSequence().toSet()).containsExactlyInAnyOrder("version", "build", "agents")
    assertThat(body["version"].asText()).isEqualTo(response.headers.getFirst("X-Feedless-Version"))
    assertThat(body["build"]["commit"].isTextual).isTrue()
    assertThat(body["build"]["date"].isIntegralNumber).isTrue()
    assertThat(body["agents"].fieldNames().asSequence().toList()).containsExactly("connected")
    assertThat(body["agents"]["connected"].asInt()).isEqualTo(0)
  }

  @Test
  fun whenRequestingStatusWithAnInvalidToken_ThenStillOk() {
    val headers = HttpHeaders()
    headers.setBearerAuth("not-a-jwt")

    val response = TestRestTemplate().exchange(
      "${baseEndpoint}/api/v1/status",
      HttpMethod.GET,
      HttpEntity<Void>(headers),
      String::class.java,
    )

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun whenPostingToStatusWithoutAuth_ThenUnauthorized() {
    val response = TestRestTemplate().postForEntity("${baseEndpoint}/api/v1/status", "", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  /** Permitting GET /api/v1/status must not open any other /api/v1 path. */
  @ParameterizedTest
  @CsvSource(
    value = [
      "api/v1/user",
      "api/v1/plans",
      "api/v1/status/extra",
    ]
  )
  fun whenRequestingOtherApiV1PathWithoutAuth_ThenUnauthorized(path: String) {
    val response = TestRestTemplate().getForEntity("${baseEndpoint}/$path", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }

  /** Uptime monitors often probe with HEAD, which Spring MVC serves for the GET mapping. */
  @Test
  fun whenSendingHeadToStatusWithoutAuth_ThenOk() {
    val response = sendRaw("HEAD", "/api/v1/status")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value())
    assertThat(response.headers().firstValue("X-Feedless-Version")).isPresent()
  }

  /** No neighbour of /api/v1/status may answer the status; sent raw, so a firewall 400 is as good as a 401. */
  @ParameterizedTest
  @CsvSource(
    value = [
      "GET,/api/v1/status/",
      "GET,/api/v1/status/x",
      "GET,/api/v1/status;x=y",
      "GET,/api/v1/st%61tus",
      "POST,/api/v1/status",
      "PUT,/api/v1/status",
      "DELETE,/api/v1/status",
    ]
  )
  fun whenRequestingAStatusVariantWithoutAuth_ThenNotTheStatus(method: String, path: String) {
    val response = sendRaw(method, path)

    assertThat(response.statusCode()).describedAs("$method $path").isNotEqualTo(HttpStatus.OK.value())
    assertThat(response.body()).describedAs("$method $path").doesNotContain("\"agents\"").doesNotContain("\"build\"")
  }

  private fun sendRaw(method: String, path: String): java.net.http.HttpResponse<String> {
    val request = java.net.http.HttpRequest.newBuilder(java.net.URI("$baseEndpoint$path"))
      .method(method, java.net.http.HttpRequest.BodyPublishers.noBody())
      .build()
    return java.net.http.HttpClient.newHttpClient().send(request, java.net.http.HttpResponse.BodyHandlers.ofString())
  }

  @Test
  fun whenCallingNonApiV1Url_ThenVersionHeaderAbsent() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.postForEntity("${baseEndpoint}/graphql", "", String::class.java)

    assertThat(response.headers.getFirst("X-Feedless-Version")).isNull()
  }

  /** CorsFilter answers preflights itself, so the header reaches OPTIONS only if the filter runs ahead of it. */
  @Test
  fun whenSendingCorsPreflightToApiV1_ThenVersionHeaderPresent() {
    val restTemplate = TestRestTemplate()
    val headers = HttpHeaders()
    headers.set(HttpHeaders.ORIGIN, "http://localhost:4200")
    headers.set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
    val request = HttpEntity<Void>(headers)

    val response = restTemplate.exchange(
      "${baseEndpoint}/api/v1/user",
      HttpMethod.OPTIONS,
      request,
      String::class.java,
    )

    assertThat(response.statusCode.is2xxSuccessful).isTrue()
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
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
