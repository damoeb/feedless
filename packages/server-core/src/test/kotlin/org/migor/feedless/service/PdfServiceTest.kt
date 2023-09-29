package org.migor.feedless.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.web.WebToArticleTransformer
import org.springframework.util.ResourceUtils
import java.io.File

class PdfServiceTest {

  private val corrId = "test"
  private lateinit var service: PdfService
  private lateinit var pdf: File

  @BeforeEach
  fun setUp() {
    service = PdfService()
    pdf = ResourceUtils.getFile("classpath:sample.pdf")
    Assertions.assertTrue(pdf.canRead() && pdf.isFile)
  }

  @Test
  fun toHTML() {
    val html = service.toHTML(corrId, pdf)
    "<body> dive into mark".split(" ").forEach {
      Assertions.assertTrue(html.contains(it))
    }
  }

  @Test
  fun toArticle() {
    val html = service.toHTML(corrId, pdf)
    val w2a = WebToArticleTransformer()
    val article = w2a.fromHtml(html, "http://example.org")
    Assertions.assertNotNull(article)
  }
}
