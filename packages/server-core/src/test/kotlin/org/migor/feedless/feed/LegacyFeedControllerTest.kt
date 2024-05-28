package org.migor.feedless.feed


import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
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

const val feedId = "feed-id"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockBeans(
  value = [
    MockBean(UserService::class),
    MockBean(LicenseService::class),
    MockBean(MailProviderService::class),
  ]
)
@ActiveProfiles(profiles = ["test", AppProfiles.api, AppProfiles.feed])
class LegacyFeedControllerTest {

  lateinit var baseEndpoint: String

  @LocalServerPort
  var port = 0

  @BeforeEach
  fun setUp() {
    baseEndpoint = "http://localhost:$port"
  }

  @ParameterizedTest
  @CsvSource(value = [
    "stream/feed/$feedId",
    "stream/feed/$feedId",
    "stream/feed/$feedId",
    "feed/$feedId",
    "feed:$feedId",
    ApiUrls.transformFeed,
    ApiUrls.webToFeed,
    ApiUrls.webToFeedVerbose,
    ApiUrls.webToFeedFromRule,
    ApiUrls.webToFeedFromChange,
  ])
  fun `calling eol urls will return eol feed`(path: String) {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("${baseEndpoint}/$path", String::class.java)
    assertEquals(HttpStatus.OK, response.statusCode)
//    assertEquals("application", response.headers.contentType?.type)
//    assertEquals("xml", response.headers.contentType?.subtype)
  }

  @ParameterizedTest
  @CsvSource(value = [
    "stream/bucket/$feedId",
    "bucket/$feedId",
    "bucket:$feedId",
  ])
  fun `calling legacy urls will return redirect`(path: String) {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("${baseEndpoint}/$path", String::class.java)
    assertEquals(HttpStatus.OK, response.statusCode)
//    assertEquals("application", response.headers.contentType?.type)
//    assertEquals("xml", response.headers.contentType?.subtype)
  }
}
