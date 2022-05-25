package org.migor.rich.rss.harvest.entryfilter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.harvest.entryfilter.simple.generated.SimpleArticleFilter
import java.util.*

class SimpleArticleFilterTest {

  lateinit var article: ArticleJsonDto

  @BeforeEach
  fun prepare() {
    article = ArticleJsonDto(
      id = "",
      title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit (S+)",
      url = "",
      content_text = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
      content_raw = "",
      content_raw_mime = "text/html",
      date_published = Date()
    )
  }

  @Test
  fun test() {
    Assertions.assertEquals(true, test(""))
    Assertions.assertEquals(true, test("   "))
    Assertions.assertEquals(true, test("  \n "))
    Assertions.assertEquals(false, test(" hase"))
    Assertions.assertEquals(true, test("+Lorem"))
    Assertions.assertEquals(true, test("Lorem"))
    Assertions.assertEquals(true, test("Lorem labore"))
    Assertions.assertEquals(false, test("-Lorem"))
    Assertions.assertEquals(false, test("Lorem -labore"))
    Assertions.assertEquals(false, test("-(S+)"))
  }

  private fun test(expr: String): Boolean {
    return SimpleArticleFilter(expr.byteInputStream()).Matches(article)
  }
}
