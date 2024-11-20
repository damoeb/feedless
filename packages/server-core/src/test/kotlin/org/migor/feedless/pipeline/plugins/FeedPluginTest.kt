package org.migor.feedless.pipeline.plugins

import kotlinx.coroutines.test.runTest
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.actions.PluginExecutionJsonEntity
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.repository.any
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.Selectors
import org.migor.feedless.scrape.WebToFeedTransformer
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.net.URI
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeedPluginTest {

  private lateinit var jsonFeed: JsonFeed

  @Mock
  lateinit var feedParserService: FeedParserService

  @Mock
  lateinit var webToFeedTransformer: WebToFeedTransformer

  @InjectMocks
  lateinit var feedPlugin: FeedPlugin

  val logCollector = LogCollector()

  @BeforeEach
  fun setUp() {
    jsonFeed = JsonFeed()
    jsonFeed.id = ""
    jsonFeed.title = ""
    jsonFeed.feedUrl = "https://example.org"
    jsonFeed.publishedAt = LocalDateTime.now()
    jsonFeed.items = emptyList()
  }

  @Test
  fun `extracts generic feed`() = runTest {
    val action = mock(ExecuteActionEntity::class.java)
    `when`(action.executorParams).thenReturn(
      PluginExecutionJsonEntity(
        org_feedless_feed = FeedParamsInput(
          generic = SelectorsInput(
            contextXPath = "",
            dateIsStartOfEvent = false,
            dateXPath = "",
            paginationXPath = "",
            extendContext = ExtendContentOptions.NONE,
            linkXPath = ""
          )
        )
      )
    )
    `when`(
      webToFeedTransformer.getFeedBySelectors(
        any(Selectors::class.java),
        any(Document::class.java),
        any(URI::class.java),
        any(LogCollector::class.java),
      )
    ).thenReturn(jsonFeed)
    val httpResponse = HttpResponse(
      contentType = "text/html",
      url = "https://example.org",
      statusCode = 200,
      responseBody = "html".toByteArray()
    )


    // when
    feedPlugin.transformFragment(action, httpResponse, logCollector)

    // then
    verify(feedParserService, times(0))
      .parseFeed(any(HttpResponse::class.java))
    verify(webToFeedTransformer, times(1))
      .getFeedBySelectors(
        any(Selectors::class.java),
        any(Document::class.java),
        any(URI::class.java),
        any(LogCollector::class.java)
      )
  }

  @Test
  fun `extracts native feed`() = runTest {
    val action = mock(ExecuteActionEntity::class.java)
    `when`(action.executorParams).thenReturn(PluginExecutionJsonEntity(org_feedless_feed = FeedParamsInput()))
    `when`(feedParserService.parseFeed(any(HttpResponse::class.java))).thenReturn(jsonFeed)
    val httpResponse = HttpResponse(
      contentType = "application/rss+xml",
      url = "https://example.org",
      statusCode = 200,
      responseBody = "html".toByteArray()
    )

    // when
    feedPlugin.transformFragment(action, httpResponse, logCollector)

    // then
    verify(feedParserService, times(1))
      .parseFeed(any(HttpResponse::class.java))
    verify(webToFeedTransformer, times(0))
      .getFeedBySelectors(
        any(Selectors::class.java),
        any(Document::class.java),
        any(URI::class.java),
        any(LogCollector::class.java)
      )
  }
}
