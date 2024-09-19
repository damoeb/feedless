package org.migor.feedless.transform

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.WebExtractService
import org.migor.feedless.scrape.WebExtractService.Companion.MIME_URL
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebExtractServiceTest {

  private lateinit var webExtractService: WebExtractService

  private val logCollector = LogCollector()

  private val html = """
  <html><body>
  <div>
    <a href="https://foo.bar">foo-text</a>
  </div>
  </body></html>
      """

  @BeforeEach
  fun setUp() {
    webExtractService = WebExtractService(mock(DateClaimer::class.java))
  }

  @Test
  fun `given a html, the text can be extracted`() = runTest {
    val extract = DOMExtract(
      fragmentName = "foo",
      xpath = DOMElementByXPath(value = "./"),
      emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
    )
    val element = Jsoup.parse(html.trimIndent())

    val response = webExtractService.extract(extract, element, Locale.GERMAN, logCollector)
    assertThat(response.fragments!!.first().text!!.data).isEqualTo("foo-text")
  }


  @Test
  fun `given a html, the url can be extracted`() = runTest {
    val extract = DOMExtract(
      fragmentName = "foo",
      xpath = DOMElementByXPath(value = "//a"),
      emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
    )
    val element = Jsoup.parse(html.trimIndent())

    val response = webExtractService.extract(extract, element, Locale.GERMAN, logCollector)
    val data = response.fragments!!.first().data!!
    assertThat(data.data).isEqualTo("https://foo.bar")
    assertThat(data.mimeType).isEqualTo(MIME_URL)
  }

}
