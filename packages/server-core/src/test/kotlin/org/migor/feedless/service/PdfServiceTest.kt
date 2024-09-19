package org.migor.feedless.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.common.PdfService
import org.migor.feedless.scrape.WebToArticleTransformer
import org.springframework.util.ResourceUtils
import java.io.File

class PdfServiceTest {

  private lateinit var service: PdfService
  private lateinit var pdf: File

  @BeforeEach
  fun setUp() {
    service = PdfService()
    pdf = ResourceUtils.getFile("classpath:sample.pdf")
    Assertions.assertTrue(pdf.canRead() && pdf.isFile)
  }

  @Test
  fun toHTML() = runTest {
    val html = service.toHTML(pdf)
    "<body> dive into mark".split(" ").forEach {
      Assertions.assertTrue(html.contains(it))
    }
  }

  @Test
  fun toArticle() = runTest {
    val html = service.toHTML(pdf)
    val w2a = WebToArticleTransformer()
    val article = w2a.fromHtml(html, "http://example.org")
    Assertions.assertNotNull(article)
  }
}
