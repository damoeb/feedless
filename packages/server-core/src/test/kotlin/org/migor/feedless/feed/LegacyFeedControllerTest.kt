package org.migor.feedless.feed


import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentService
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.document.DocumentService
import org.migor.feedless.document.any
import org.migor.feedless.document.anyOrNull
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.license.LicenseService
import org.migor.feedless.mail.MailProviderService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserService
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

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
    MockBean(DocumentService::class),
    MockBean(AgentService::class),
    MockBean(SessionService::class),
    MockBean(RepositoryDAO::class),
    MockBean(LegacyFeedService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
@ActiveProfiles(profiles = ["test", AppProfiles.api, AppProfiles.legacyFeeds])
class LegacyFeedControllerTest {

  lateinit var baseEndpoint: String

  @LocalServerPort
  var port = 0

  @Autowired
  lateinit var legacyFeedService: LegacyFeedService

  lateinit var mockFeed: JsonFeed

  @BeforeEach
  fun setUp() {
    baseEndpoint = "http://localhost:$port"

    mockFeed = JsonFeed()
    mockFeed.id = "foo"
    mockFeed.title = "foo"
    mockFeed.feedUrl = "https://foo.bar/other-feed"
    mockFeed.publishedAt = Date()
    mockFeed.items = emptyList()
  }

  @Test
  fun `calling tf returns a feed`() {
    val restTemplate = TestRestTemplate()

    `when`(legacyFeedService.transformFeed(
      any(String::class.java),
      any(String::class.java),
      anyOrNull(String::class.java),
      any(String::class.java),
    )).thenReturn(mockFeed)

    val url = "${baseEndpoint}${ApiUrls.transformFeed}?url={url}&re={re}&q={q}&out={out}"
    val params = mapOf(
      "url" to "https://heise.de/some-feed.xml",
      "re" to "",
      "q" to "contains(#any, \"EM\")",
      "out" to "atom"
    )
    val headers = HttpHeaders()
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
    val entity: HttpEntity<*> = HttpEntity<String>(headers)

    val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java,
      params)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @Test
  @Tag("unstable")
  fun `calling w2f returns a feed`() {
    val restTemplate = TestRestTemplate()

    `when`(legacyFeedService.webToFeed(
      any(String::class.java),
      any(String::class.java),
      any(String::class.java),
      any(String::class.java),
      any(String::class.java),
      anyOrNull(String::class.java),
      any(Boolean::class.java),
      anyOrNull(String::class.java),
      any(String::class.java),
    )).thenReturn(mockFeed)

    val url = "${baseEndpoint}/${ApiUrls.webToFeed}?url={url}&link={link}&date={date}&context={context}&q={q}&out={out}&v={v}"
    val params = mapOf(
      "v" to 0.1,
      "url" to "https://heise.de",
      "link" to "/a[1]",
      "date" to "",
      "context" to "//div[3]/div/div[1]/section[1]/article",
      "q" to "contains(#any, \"EM\")",
      "out" to "atom"
    )
    val headers = HttpHeaders()
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
    val entity: HttpEntity<*> = HttpEntity<String>(headers)

    val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java,
      params)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @ParameterizedTest
  @CsvSource(value = [
    "stream/feed/$feedId",
    "feed/$feedId",
    "feed:$feedId",
  ])
  fun `calling legacy feed by id returns a feed`(feedUrl: String) {
    val restTemplate = TestRestTemplate()

    `when`(legacyFeedService.getFeed(
        any(String::class.java),
        any(String::class.java),
        any(String::class.java),
    )).thenReturn(mockFeed)

    val headers = HttpHeaders()
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
    val entity: HttpEntity<*> = HttpEntity<String>(headers)

    val response = restTemplate.exchange("${baseEndpoint}/${feedUrl}", HttpMethod.GET, entity, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @ParameterizedTest
  @CsvSource(value = [
    "stream/bucket/$feedId",
    "bucket/$feedId",
    "bucket:$feedId",
  ])
  fun `requesting legacy bucket will return redirect`(path: String) {
    val restTemplate = TestRestTemplate()
    `when`(legacyFeedService.getRepository(any(String::class.java))).thenReturn(ResponseEntity.ok().build())

    val response = restTemplate.getForEntity("${baseEndpoint}/$path", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }
}
