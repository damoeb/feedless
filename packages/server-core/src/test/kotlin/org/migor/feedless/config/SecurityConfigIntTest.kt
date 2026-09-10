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

  /**
   * The version header is set by [org.migor.feedless.http.HttpApiVersionHeaderFilter], which
   * must run ahead of [org.migor.feedless.http.HttpApiJwtFilter] in the real Spring Security
   * filter chain so it lands on the 401 that filter writes via `sendError` before any
   * controller runs — a unit test of either filter in isolation cannot prove that ordering.
   */
  @Test
  fun whenRequestingApiV1WithoutAuth_ThenVersionHeaderPresentOn401() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("${baseEndpoint}/api/v1/repositories", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
  }

  @Test
  fun whenCallingNonApiV1Url_ThenVersionHeaderAbsent() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.postForEntity("${baseEndpoint}/graphql", "", String::class.java)

    assertThat(response.headers.getFirst("X-Feedless-Version")).isNull()
  }

  /**
   * CorsFilter answers a valid preflight itself, without ever calling filterChain.doFilter(),
   * so the version filter has to be registered ahead of CorsFilter (not merely ahead of
   * HttpApiJwtFilter/BasicAuthenticationFilter, both later in the chain) for the header to
   * reach an OPTIONS preflight response. `http://localhost:4200` is the origin the default
   * `app.cors.allowedOrigins` config allows (see application.yaml), so CorsFilter answers this
   * preflight with 2xx/OK rather than rejecting it.
   */
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
