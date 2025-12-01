package org.migor.feedless.feed


import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableSecurityConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.anyOrNull
import org.migor.feedless.anyOrNull2
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.feed.parser.json.JsonFeed
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.socket.WebSocketHandler
import java.time.LocalDateTime

const val feedId = "d6b2f9df-3a15-4dbd-9789-fb62a6d58d0f"

@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    WebSocketHandler::class,
    FeedParserService::class,
  ]
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppProfiles.feed,
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableSecurityConfiguration::class,
)
class FeedControllerIntTest {

  lateinit var baseEndpoint: String

  @LocalServerPort
  var port = 0

  @MockitoBean
  lateinit var feedService: FeedService

  @MockitoBean
  lateinit var analyticsService: AnalyticsService

  lateinit var mockFeed: JsonFeed

  @BeforeEach
  fun setUp() {
    baseEndpoint = "http://localhost:$port"

    mockFeed = JsonFeed()
    mockFeed.id = "foo"
    mockFeed.title = "foo"
    mockFeed.feedUrl = "https://foo.bar/other-feed"
    mockFeed.publishedAt = LocalDateTime.now()
    mockFeed.items = emptyList()
  }

  @Test
  fun `calling tf returns a feed`() = runTest {
    val restTemplate = TestRestTemplate()

    `when`(
      feedService.transformFeed(
        any2(),
        anyOrNull2(),
        any2(),
        any2(),
      )
    ).thenReturn(mockFeed)

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

    val response = restTemplate.exchange(
      url, HttpMethod.GET, entity, String::class.java,
      params
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @Test
  fun `calling w2f returns a feed`() = runTest {
    val restTemplate = TestRestTemplate()

    `when`(
      feedService.webToFeed(
        any2(),
        any2(),
        any(Boolean::class.java),
        anyOrNull(String::class.java),
        any2(),
        any(String::class.java),
      )
    ).thenReturn(mockFeed)

    val url =
      "${baseEndpoint}/${ApiUrls.webToFeed}?url={url}&link={link}&date={date}&context={context}&q={q}&out={out}&v={v}"
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

    val response = restTemplate.exchange(
      url, HttpMethod.GET, entity, String::class.java,
      params
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "stream/feed/$feedId",
      "feed/$feedId",
      "feed:$feedId",
    ]
  )
  fun `calling legacy feed by id returns a feed`(feedUrl: String) = runTest {
    val restTemplate = TestRestTemplate()

    `when`(
      feedService.getFeed(
        any2(),
        any2(),
      )
    ).thenReturn(mockFeed)

    val headers = HttpHeaders()
    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
    val entity: HttpEntity<*> = HttpEntity<String>(headers)

    val response = restTemplate.exchange("${baseEndpoint}/${feedUrl}", HttpMethod.GET, entity, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.headers.contentType?.type).isEqualTo("application")
    assertThat(response.headers.contentType?.subtype).isEqualTo("xml")
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "stream/bucket/$feedId",
      "bucket/$feedId",
      "bucket:$feedId",
    ]
  )
  fun `requesting legacy bucket will return redirect`(path: String) {
    val restTemplate = TestRestTemplate()
    `when`(feedService.getRepository(any2())).thenReturn(ResponseEntity.ok().build())

    val response = restTemplate.getForEntity("${baseEndpoint}/$path", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }
}
