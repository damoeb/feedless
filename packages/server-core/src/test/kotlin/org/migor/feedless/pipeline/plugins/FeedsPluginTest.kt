package org.migor.feedless.pipeline.plugins

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ExecuteActionEntity
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.feed.FeedParserService
import org.migor.feedless.feed.discovery.GenericFeedLocator
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.license.LicenseService
import org.migor.feedless.repository.any
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.service.LogCollector
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedParserOptions
import org.migor.feedless.web.GenericFeedRule
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.scrape])
@MockBeans(
  value = [
    MockBean(UserSecretService::class),
    MockBean(AgentService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
    MockBean(LicenseService::class),
  ]
)
class FeedsPluginTest {

  @Autowired
  lateinit var feedsPlugin: FeedsPlugin

  @MockBean
  lateinit var feedParserService: FeedParserService

  @MockBean
  lateinit var genericFeedLocator: GenericFeedLocator

  val corrId = "test"
  val logCollector = LogCollector()

  @ParameterizedTest
  @CsvSource(
    value = [
      "text/xml",
      "text/rss+xml",
      "application/rss+xml",
      "application/rdf+xml",
      "application/rss+xml",
      "application/rss+xml; charset=UTF-8",
      "application/atom+xml",
      "application/xml"
    ]
  )
  fun `given a native feed`(mimeType: String) = runTest {
    val data = HttpResponse(
      contentType = mimeType,
      url = "https://test.example",
      statusCode = 200,
      responseBody = "".toByteArray()
    )

    val mockFeed = JsonFeed()
    mockFeed.title = ""
    mockFeed.feedUrl = ""
    mockFeed.publishedAt = LocalDateTime.now()
    mockFeed.items = emptyList()

    `when`(feedParserService.parseFeed(any(String::class.java), any(HttpResponse::class.java)))
      .thenReturn(mockFeed)

    // when
    val result = feedsPlugin.transformFragment(corrId, ExecuteActionEntity(), data, logCollector)

    // then
    assertThat(result.feeds!!.nativeFeeds!!.size).isEqualTo(1)
    assertThat(result.feeds!!.genericFeeds.size).isEqualTo(0)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "text/html; charset=UTF-8",
      "text/html",
    ]
  )
  fun `given a website`(mimeType: String) = runTest {
    val data = HttpResponse(
      contentType = mimeType,
      url = "https://test.example",
      statusCode = 200,
      responseBody = "".toByteArray()
    )

    val mockGenericFeed = GenericFeedRule(
      linkXPath = "",
      extendContext = ExtendContext.NONE,
      contextXPath = "",
      count = 0,
      dateIsStartOfEvent = false,
      dateXPath = "",
      paginationXPath = "",
      score = 0.0
    )
    `when`(
      genericFeedLocator.locateInDocument(
        any(String::class.java), any(Document::class.java), any(String::class.java), any(
          GenericFeedParserOptions::class.java
        )
      )
    )
      .thenReturn(listOf(mockGenericFeed))

    // when
    val result = feedsPlugin.transformFragment(corrId, ExecuteActionEntity(), data, logCollector)

    // then
    assertThat(result.feeds!!.nativeFeeds!!.size).isEqualTo(0)
    assertThat(result.feeds!!.genericFeeds.size).isEqualTo(1)
  }

  @Test
  fun `given a invalid mime types`() = runTest {
    val data = HttpResponse(
      contentType = "application/pdf",
      url = "https://test.example",
      statusCode = 200,
      responseBody = "".toByteArray()
    )

    // when
    val result = feedsPlugin.transformFragment(corrId, ExecuteActionEntity(), data, logCollector)

    // then
    assertThat(result.feeds!!.nativeFeeds!!.size).isEqualTo(0)
    assertThat(result.feeds!!.genericFeeds.size).isEqualTo(0)
  }
}
