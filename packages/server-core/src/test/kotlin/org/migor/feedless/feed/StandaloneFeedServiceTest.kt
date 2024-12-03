package org.migor.feedless.feed

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.anyOrNull2
import org.migor.feedless.repository.eq
import org.migor.feedless.scrape.HttpFetchOutput
import org.migor.feedless.scrape.ScrapeActionOutput
import org.migor.feedless.scrape.ScrapeOutput
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToFeedTransformer
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

class StandaloneFeedServiceTest {

  private lateinit var standaloneFeedService: StandaloneFeedService
  private lateinit var feedParserService: FeedParserService
  private lateinit var filterPlugin: CompositeFilterPlugin
  private lateinit var featureService: FeatureService
  private lateinit var userService: UserService
  private lateinit var webToFeedTransformer: WebToFeedTransformer
  private lateinit var scrapeService: ScrapeService
  private lateinit var sourceService: SourceService
  private lateinit var documentService: DocumentService

  @BeforeEach
  fun beforeEach() = runTest {
    feedParserService = mock(FeedParserService::class.java)
    filterPlugin = mock(CompositeFilterPlugin::class.java)
    featureService = mock(FeatureService::class.java)
    `when`(featureService.isDisabled(eq(FeatureName.legacyApiBool), anyOrNull2())).thenReturn(false)

    userService = mock(UserService::class.java)
    val adminUser = mock(UserEntity::class.java)
    `when`(adminUser.id).thenReturn(UUID.randomUUID())
    `when`(userService.findAdminUser()).thenReturn(adminUser)

    webToFeedTransformer = mock(WebToFeedTransformer::class.java)
    scrapeService = mock(ScrapeService::class.java)
    sourceService = mock(SourceService::class.java)
    documentService = mock(DocumentService::class.java)

    standaloneFeedService = StandaloneFeedService(
      mock(PropertyService::class.java),
      webToFeedTransformer,
      feedParserService,
      scrapeService,
      mock(RepositoryService::class.java),
      userService,
      sourceService,
      featureService,
      documentService,
      filterPlugin,
    )
  }

  @Test
  fun `getFeed returns documents`() = runTest {
    val source = mock(SourceEntity::class.java)
    `when`(source.title).thenReturn("title")
    `when`(source.createdAt).thenReturn(LocalDateTime.now())

    `when`(sourceService.findById(any2())).thenReturn(Optional.of(source))
    `when`(documentService.findAllBySourceId(any2(), any2())).thenReturn(listOf())

    val feed = standaloneFeedService.getFeed(mock(UUID::class.java), "feedUrl")
    assertThat(feed).isNotNull()
  }

  @Test
  fun `webToFeed will filter`() = runTest {
    // given
    `when`(filterPlugin.filterEntity(any2(), any2(), any(Int::class.java), any2())).thenReturn(true)
    val feed = createJsonFeed()
    `when`(webToFeedTransformer.getFeedBySelectors(any2(), any2(), any2(), any2())).thenReturn(feed)

    val scrapeOutput = mock(ScrapeOutput::class.java)

    val httpResponse = HttpResponse(
      contentType = "text/html",
      url = "",
      statusCode = 200,
      responseBody = "".toByteArray(),
    )
    val httpFetch = mock(HttpFetchOutput::class.java)
    `when`(httpFetch.response).thenReturn(httpResponse)
    val scrapeAction = ScrapeActionOutput(index = 0, fetch = httpFetch)
    `when`(scrapeOutput.outputs).thenReturn(listOf(scrapeAction))
    `when`(scrapeService.scrape(any2(), any2())).thenReturn(scrapeOutput)

    // when
    standaloneFeedService.webToFeed("url",
      "linkXPath",
      "extendContext",
      "contextXPath",
      "dateXPath",
      prerender = false,
    "filter",
    "feedUrl")


    verify(filterPlugin, times(2)).filterEntity(any2(), any2(), any(Int::class.java), any2())
  }

  @Test
  fun `transformFeed will filter`() = runTest {
    // given
    val feed = createJsonFeed()
    `when`(feedParserService.parseFeedFromUrl(any2())).thenReturn(feed)
    `when`(filterPlugin.filterEntity(any2(), any2(), any(Int::class.java), any2())).thenReturn(true)

    standaloneFeedService.transformFeed("nativeFeedUrl", "filter", "feedUrl")

    verify(filterPlugin, times(2)).filterEntity(any2(), any2(), any(Int::class.java), any2())
  }

  private fun createJsonFeed(): JsonFeed {
    val feed = mock(JsonFeed::class.java)
    `when`(feed.items).thenReturn(listOf(mock(JsonItem::class.java), mock(JsonItem::class.java)))
    return feed
  }
}
