package org.migor.feedless.pipeline.plugins

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.mockito.Mockito
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.springframework.util.ResourceUtils
import java.util.*

internal class PrivacyPluginTest {

  lateinit var plugin: PrivacyPlugin
  lateinit var mockHttpService: HttpService

  private val pdfUrl = "https://some.pdf/2405.06631"

  private val document = Jsoup.parse(
    """<html><body><div class="article-layout__header-container">
  <header class="a-article-header">
    <h1 class="a-article-header__title">
      Missing Link: Was es mit der radikalen Theorie zur Dunklen Energie auf sich hat
    </h1>
    <figure>
      <img
        src="https://heise.cloudimg.io/v7/_www-heise-de_/imgs/18/4/1/5/4/5/6/9/noirlab2310a-396394ddb135ff22.jpeg?force_format=avif%2Cwebp%2Cjpeg&amp;org_if_sml=1&amp;q=85&amp;width=610"
        width="1643" height="924" alt="" style="aspect-ratio: 1643 / 924; object-fit: cover;">
      <figcaption class="a-caption "><p class="a-caption__text">

        Das Schwarze Loch im Zentrum der elliptischen Riesengalaxie Messier 87 in einer neuen KI-unterstützten
        Bearbeitung von L. Medeiros (Institute for Advanced Study), D. Psaltis (Georgia Tech), T. Lauer (NSF’s
        NOIRLab), and F. Ozel (Georgia Tech). Here is <a href="$pdfUrl">the paper</a>.

      </p>
        <p class="a-caption__source">
          (Bild:&nbsp;<a href="https://noirlab.edu/public/images/noirlab2310a/" target="_blank"
                         rel="external noopener">NORLab</a>)
        </p>
      </figcaption>
    </figure>
  </header>
</div></body></html>
""".trimIndent(),
    "https://www.heise.de/hintergrund/Missing-Link-Was-es-mit-der-radikalen-Theorie-zur-Dunklen-Energie-auf-sich-hat-8988403.html"
  )


  @BeforeEach
  fun setUp() {
    plugin = PrivacyPlugin()
    val mockPropertyService = mock(PropertyService::class.java)
    Mockito.`when`(mockPropertyService.apiGatewayUrl).thenReturn("https://localhost:8080/")
    plugin.propertyService = mockPropertyService
    mockHttpService = mock(HttpService::class.java)
    plugin.httpService = mockHttpService
  }

  @Disabled
  @ParameterizedTest
  @CsvSource(
    value = [
      "png",
      "jpeg",
      "webp",
    ]
  )
  fun inlineImages(inputImageType: String) = runTest {
    val mockHttpResponse = HttpResponse(
      contentType = "image/$inputImageType",
      url = "",
      statusCode = 200,
      responseBody = ResourceUtils.getFile("classpath:images//sample.$inputImageType").readBytes(),
    )

    Mockito.`when`(mockHttpService.httpGet(anyString(), anyString(), anyInt(), Mockito.isNull()))
      .thenAnswer { mockHttpResponse }

    val (markup, _) = plugin.extractAttachments("", UUID.randomUUID(), document)
    val images = Jsoup.parse(markup).select("img[src]")
    Assertions.assertTrue(images.isNotEmpty())
    val src = images.first()!!.attr("src")
    Assertions.assertTrue(src.startsWith("data:image/"))
    Assertions.assertTrue(src.length > 50)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "pdf",
    ]
  )
  fun attachPdf(inputFileType: String) = runTest {
    val mockPdfResponse = HttpResponse(
      contentType = "application/$inputFileType",
      url = "",
      statusCode = 200,
      responseBody = ResourceUtils.getFile("classpath:images//sample.$inputFileType").readBytes(),
    )

    val mockHtmlResponse = HttpResponse(
      contentType = "text/html",
      url = "",
      statusCode = 200,
      responseBody = "".toByteArray(),
    )

    Mockito.`when`(mockHttpService.httpGet(anyString(), anyString(), anyInt(), Mockito.isNull())).thenAnswer { input ->
      run {
        if (input.arguments[1] == pdfUrl) {
          mockPdfResponse
        } else {
          mockHtmlResponse
        }
      }
    }

    val (markup, attachments) = plugin.extractAttachments("", UUID.randomUUID(), document)
    assertThat(attachments.size).isEqualTo(1)
    val attachment = attachments[0]
    assertThat(attachment.contentType).isEqualTo("application/pdf")

    assertThat(markup).doesNotContain(pdfUrl)
    assertThat(markup).containsSequence(attachment.id.toString())
  }
}
