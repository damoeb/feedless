package org.migor.feedless.service

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.actions.ClickPositionAction
import org.migor.feedless.actions.DomAction
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExtractBoundingBoxAction
import org.migor.feedless.actions.ExtractXpathAction
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.actions.HeaderAction
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.agent.AgentService
import org.migor.feedless.any
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.attachment.AttachmentDAO
import org.migor.feedless.eq
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.needsPrerendering
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.source.SourceUseCase
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@ActiveProfiles(
  "test",
  AppProfiles.scrape,
  AppLayer.service,
)
@MockitoBean(
  types = [
    PropertyService::class,
    AgentService::class,
    AttachmentDAO::class,
    SourceRepository::class,
    SourceUseCase::class,
    StatelessAuthService::class,
  ]
)
@Import(DisableDatabaseConfiguration::class)
class ScrapeServiceIntTest {

  @Autowired
  lateinit var scrapeService: ScrapeService

  @MockitoBean
  lateinit var httpService: HttpService

  lateinit var fetchAction: FetchAction

  @Test
  fun `does not prerender by default`() {
    assertThat(needsPrerendering(sourceWithActions(listOf()), 0)).isFalse()
  }

  @Test
  fun `prerendering respects current or next actions`() {
    val action = ClickPositionAction(
      sourceId = SourceId(),
      x = 0,
      y = 0,
    )
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 1)).isFalse()
  }

  @Test
  fun `prerenders with click-position action`() {
    val action = ClickPositionAction(
      sourceId = SourceId(),
      x = 0,
      y = 0,
    )
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with extract-box action`() {
    val action = ExtractBoundingBoxAction(
      sourceId = SourceId(),
      x = 0,
      y = 0,
      h = 0,
      w = 0,
      fragmentName = ""
    )
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with pixel-extract action`() {
    val action = mock(ExtractXpathAction::class.java)
    `when`(action.emit).thenReturn(arrayOf(ExtractEmit.pixel))
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with forcePrerender is true`() {
    val fetchAction = mock(FetchAction::class.java)
    `when`(fetchAction.forcePrerender).thenReturn(true)
    assertThat(needsPrerendering(sourceWithActions(listOf(fetchAction)), 0)).isTrue()
  }

  @BeforeEach
  fun setUp() {
    runBlocking {
      val httpResponse = HttpResponse(
        "text/html", "https://base-url.example", 200, """<!DOCTYPE html>
<html lang="en-US">
<head>
  <title>HTML Examples</title>
  <meta name="theme-color" content="#ffffff">
</head>
  <body>
  <a href="./foo">Link</a>
  </body>
</html>
""".toByteArray()
      )
      `when`(
        httpService.httpGetCaching(
          any(String::class.java),
          any(Int::class.java),
          anyMap()
        )
      ).thenReturn(httpResponse)
        .thenReturn(httpResponse)
      fetchAction = mock(FetchAction::class.java)
      `when`(fetchAction.url).thenReturn("http://action.url")
    }
  }

  @Test
  fun `fetch action calls httpService`() = runTest {
    scrapeService.scrape(sourceWithActions(listOf(fetchAction)), LogCollector())

    verify(httpService, times(1)).httpGetCaching(
      any(String::class.java),
      any(Int::class.java),
      anyMap()
    )
  }

  @Test
  fun `header action will provide headers for next fetchAction`() = runTest {
    val headerAction = mock(HeaderAction::class.java)
    val headerName = "content-type"
    val headerValue = "application/json"
    `when`(headerAction.name).thenReturn(headerName)
    `when`(headerAction.value).thenReturn(headerValue)
    scrapeService.scrape(sourceWithActions(listOf(headerAction, fetchAction)), LogCollector())

    verify(httpService, times(1)).httpGetCaching(
      any(String::class.java),
      any(Int::class.java),
      eq(mapOf(headerName to headerValue))
    )
  }

  @Test
  fun `purge action removes elements specified by xpath`() = runTest {
    val purgeAction = mock(DomAction::class.java)
    `when`(purgeAction.event).thenReturn(DomEventType.purge)
    `when`(purgeAction.xpath).thenReturn("//title")

    val scrapeResponse =
      scrapeService.scrape(sourceWithActions(listOf(fetchAction, purgeAction)), LogCollector())
    val last = scrapeResponse.outputs.last()

    val html = { v: String ->
      val document = Jsoup.parse(v)
      document.outputSettings(Document.OutputSettings().indentAmount(0))
      document.html()
    }

    assertThat(html(last.fragment!!.fragments!![0].html!!.data)).isEqualTo(
      html(
        """<!doctype html>
  <html lang="en-US">
   <head>
    <meta name="theme-color" content="#ffffff">
   </head>
   <body><a href="https://base-url.example/foo">Link</a>
   </body>
  </html>"""
      )
    )
  }

//  @Test
//  fun `click-xpath action`() {
//
//  }

  @Test
  fun `extract action`() = runTest {
    val extractAction = mock(ExtractXpathAction::class.java)
    `when`(extractAction.xpath).thenReturn("//title")
    `when`(extractAction.emit).thenReturn(arrayOf(ExtractEmit.html))

    val scrapeResponse =
      scrapeService.scrape(sourceWithActions(listOf(fetchAction, extractAction)), LogCollector())
    val last = scrapeResponse.outputs.last()

    val fragment = last.fragment!!.fragments!![0]
    assertThat(fragment.html!!.data).isEqualTo("<title>HTML Examples</title>")
    assertThat(fragment.text).isNull()
  }
//
//  @Test
//  fun `plugin action`() {
//  }

  private fun sourceWithActions(actions: List<ScrapeAction>): Source {
    val source = mock(Source::class.java)
    `when`(source.actions).thenReturn(actions.toMutableList())
    return source
  }
}
