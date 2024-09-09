package org.migor.feedless.web

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.service.LogCollector
import org.migor.feedless.web.WebExtractService.Companion.MIME_URL
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import java.util.*

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WebExtractServiceTest {

  @Mock
  lateinit var dateClaimer: DateClaimer

  @InjectMocks
  lateinit var webExtractService: WebExtractService

  val logCollector = LogCollector()

  val html = """
  <html><body>
  <div>
    <a href="https://foo.bar">foo-text</a>
  </div>
  </body></html>
      """


  @Test
  fun `given a html, the text can be extracted`() {
    runTest {
      val corrId = ""
      val extract = DOMExtract(
        fragmentName = "foo",
        xpath = DOMElementByXPath(value = "./"),
        emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
      )
      val element = Jsoup.parse(html.trimIndent())

      val response = webExtractService.extract(corrId, extract, element, Locale.GERMAN, logCollector)
      assertThat(response.fragments!!.first().text!!.data).isEqualTo("foo-text")
    }
  }

  @Test
  fun `given a html, the url can be extracted`() {
    runTest {
      val corrId = ""
      val extract = DOMExtract(
        fragmentName = "foo",
        xpath = DOMElementByXPath(value = "//a"),
        emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
      )
      val element = Jsoup.parse(html.trimIndent())

      val response = webExtractService.extract(corrId, extract, element, Locale.GERMAN, logCollector)
      val data = response.fragments!!.first().data!!
      assertThat(data.data).isEqualTo("https://foo.bar")
      assertThat(data.mimeType).isEqualTo(MIME_URL)
    }
  }
}
