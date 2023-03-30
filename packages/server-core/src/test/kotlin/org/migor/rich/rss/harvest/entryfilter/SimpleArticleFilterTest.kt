package org.migor.rich.rss.harvest.entryfilter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.harvest.entryfilter.simple.generated.SimpleArticleFilter

class SimpleArticleFilterTest {

  lateinit var article: RichArticle

  @BeforeEach
  fun prepare() {
    article = RichArticle()
    article.title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit (S+)"
    article.url = ""
    article.contentText = "Sed do eiusmod tempor incididunt Staffel 1 ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    article.contentRaw = ""
    article.contentRawMime = "text/html"
//      publishedAt = Date()
  }

  @Test
  fun testSyntax() {
    Assertions.assertEquals(true, test(""))
    Assertions.assertEquals(true, test("   "))
    Assertions.assertEquals(true, test("  \n "))
    Assertions.assertEquals(false, test(" hase"))
//    Assertions.assertEquals(true, test("+Lorem"))
    Assertions.assertEquals(true, test("Lorem"))
    Assertions.assertEquals(true, test("Lorem labore"))
    Assertions.assertEquals(false, test("-Lorem"))
    Assertions.assertEquals(false, test("-lorem"))
    Assertions.assertEquals(false, test("-lore"))
    Assertions.assertEquals(true, test("lore"))
    Assertions.assertEquals(false, test("Lorem -labore"))
    Assertions.assertEquals(false, test("-(S+)"))
    Assertions.assertEquals(true, test("Staffel 1"))
    Assertions.assertEquals(true, test("Staffel -2"))
  }

  private fun test(expr: String): Boolean {
    return SimpleArticleFilter(expr.byteInputStream()).Matches(article.title, article.contentText)
  }
}
