package org.migor.feedless.service

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.actions.ClickPositionActionEntity
import org.migor.feedless.actions.DomActionEntity
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.actions.ExtractBoundingBoxActionEntity
import org.migor.feedless.actions.ExtractEmit
import org.migor.feedless.actions.ExtractXpathActionEntity
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.actions.HeaderActionEntity
import org.migor.feedless.actions.ScrapeActionEntity
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.document.any
import org.migor.feedless.document.eq
import org.migor.feedless.license.LicenseService
import org.migor.feedless.secrets.UserSecretService
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.util.CryptUtil.newCorrId
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.scrape])
@MockBeans(
  value = [
    MockBean(UserSecretService::class),
    MockBean(AgentService::class),
    MockBean(LicenseService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
class ScrapeServiceTest {

  private val corrId: String = newCorrId()

  @Autowired
  lateinit var scrapeService: ScrapeService

  @MockBean
  lateinit var httpService: HttpService

  lateinit var fetchAction: FetchActionEntity

  @Test
  fun `does not prerender by default`() {
    assertThat(needsPrerendering(sourceWithActions(listOf()), 0)).isFalse()
  }

  @Test
  fun `prerendering respects current or next actions`() {
    val action = ClickPositionActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 1)).isFalse()
  }

  @Test
  fun `prerenders with click-position action`() {
    val action = ClickPositionActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with extract-box action`() {
    val action = ExtractBoundingBoxActionEntity()
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with pixel-extract action`() {
    val action = mock(ExtractXpathActionEntity::class.java)
    `when`(action.emit).thenReturn(arrayOf(ExtractEmit.pixel))
    assertThat(needsPrerendering(sourceWithActions(listOf(action)), 0)).isTrue()
  }

  @Test
  fun `prerenders with forcePrerender is true`() {
    val fetchAction = mock(FetchActionEntity::class.java)
    `when`(fetchAction.forcePrerender).thenReturn(true)
    assertThat(needsPrerendering(sourceWithActions(listOf(fetchAction)), 0)).isTrue()
  }

  @BeforeEach
  fun setUp() {
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
        any(String::class.java),
        any(Int::class.java),
        anyMap()
      )
    ).thenReturn(httpResponse)
      .thenReturn(httpResponse)
    fetchAction = mock(FetchActionEntity::class.java)
    `when`(fetchAction.url).thenReturn("http://action.url")
  }

  @Test
  fun `fetch action calls httpService`() {
    runBlocking {
      scrapeService.scrape(corrId, sourceWithActions(listOf(fetchAction)))

      verify(httpService, times(1)).httpGetCaching(
        any(String::class.java),
        any(String::class.java),
        any(Int::class.java),
        anyMap()
      )
    }
  }

  @Test
  fun `header action will provide headers for next fetchAction`() {
    runBlocking {
      val headerAction = mock(HeaderActionEntity::class.java)
      val headerName = "content-type"
      val headerValue = "application/json"
      `when`(headerAction.name).thenReturn(headerName)
      `when`(headerAction.value).thenReturn(headerValue)
      scrapeService.scrape(corrId, sourceWithActions(listOf(headerAction, fetchAction)))

      verify(httpService, times(1)).httpGetCaching(
        any(String::class.java),
        any(String::class.java),
        any(Int::class.java),
        eq(mapOf(headerName to headerValue))
      )
    }
  }

  @Test
  fun `purge action removes elements specified by xpath`() {
    runBlocking {
      val purgeAction = mock(DomActionEntity::class.java)
      `when`(purgeAction.event).thenReturn(DomEventType.purge)
      `when`(purgeAction.xpath).thenReturn("//title")

      val scrapeResponse = scrapeService.scrape(corrId, sourceWithActions(listOf(fetchAction, purgeAction)))
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
  }

//  @Test
//  fun `click-xpath action`() {
//
//  }

  @Test
  fun `extract action`() {
    runBlocking {
      val extractAction = mock(ExtractXpathActionEntity::class.java)
      `when`(extractAction.xpath).thenReturn("//title")
      `when`(extractAction.emit).thenReturn(arrayOf(ExtractEmit.html))

      val scrapeResponse = scrapeService.scrape(corrId, sourceWithActions(listOf(fetchAction, extractAction)))
      val last = scrapeResponse.outputs.last()

      val fragment = last.fragment!!.fragments!![0]
      assertThat(fragment.html!!.data).isEqualTo("<title>HTML Examples</title>")
      assertThat(fragment.text).isNull()
    }
  }
//
//  @Test
//  fun `plugin action`() {
//  }

  private fun sourceWithActions(actions: List<ScrapeActionEntity>): SourceEntity {
    val source = mock(SourceEntity::class.java)
    `when`(source.actions).thenReturn(actions.toMutableList())
    return source
  }
}