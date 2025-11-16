package org.migor.feedless.pipeline.plugins

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.any2
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.data.jpa.source.actions.ExecuteActionEntity
import org.migor.feedless.data.jpa.source.actions.PluginExecutionJsonEntity
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.generated.types.ConditionalTagInput
import org.migor.feedless.generated.types.DiffRecordsParamsInput
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.scrape.LogCollector
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
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginExecutionJsonEntity2(
  @JsonProperty("org_feedless_feed") val org_feedless_feed: FeedParamsInput? = null,
  @JsonProperty("org_feedless_diff_email_forward") val org_feedless_diff_records: DiffRecordsParamsInput? = null,
  @JsonProperty("org_feedless_filter") val org_feedless_filter: List<ItemFilterParamsInput>? = null,
  @JsonProperty("jsonData") val jsonData: String? = null,
  @JsonProperty("org_feedless_fulltext") val org_feedless_fulltext: FulltextPluginParamsInput? = null,
  @JsonProperty("org_feedless_conditional_tag") val org_feedless_conditional_tag: List<ConditionalTagInput>? = null,
) {
}

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

    val s = Gson().toJson(
      PluginExecutionJsonEntity2(
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

    val action = mock(ExecuteActionEntity::class.java)
    `when`(action.executorParams).thenReturn(
      PluginExecutionJsonEntity(
        paramsJsonString = Gson().toJson(
          FeedParamsInput(
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
    )
    `when`(
      webToFeedTransformer.getFeedBySelectors(
        any2(),
        any2(),
        any2(),
        any2(),
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
      .parseFeed(any2())
    verify(webToFeedTransformer, times(1))
      .getFeedBySelectors(
        any2(),
        any2(),
        any2(),
        any2()
      )
  }

  @Test
  fun `extracts native feed`() = runTest {
    val action = mock(ExecuteActionEntity::class.java)
    `when`(action.executorParams).thenReturn(PluginExecutionJsonEntity(paramsJsonString = ""))
    `when`(feedParserService.parseFeed(any2())).thenReturn(jsonFeed)
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
      .parseFeed(any2())
    verify(webToFeedTransformer, times(0))
      .getFeedBySelectors(
        any2(),
        any2(),
        any2(),
        any2()
      )
  }
}
