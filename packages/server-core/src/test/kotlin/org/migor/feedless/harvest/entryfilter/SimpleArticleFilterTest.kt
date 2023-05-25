package org.migor.feedless.harvest.entryfilter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.harvest.entryfilter.simple.SimpleArticle
import org.migor.feedless.harvest.entryfilter.simple.generated.SimpleArticleFilter

class SimpleArticleFilterTest {

  lateinit var article: SimpleArticle

  @BeforeEach
  fun prepare() {
    article = SimpleArticle(
      title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit (S+)",
      url = "example.com",
      body = "Sed do eiusmod tempor incididunt Staffel 1 ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
    )
  }

  @Test
  fun testSyntax() {
    Assertions.assertEquals(true, test(""))
    Assertions.assertEquals(true, test("   "))
    Assertions.assertEquals(true, test("  \n "))
//    Assertions.assertEquals(false, test(" hase"))
    Assertions.assertEquals(true, test("\"Lorem\""))
    Assertions.assertEquals(false, test("\"not inside\""))
    Assertions.assertEquals(true, test("eq(len(\"foo\"), 3)"))
    Assertions.assertEquals(true, test("gt(len(#url), 3)"))
    Assertions.assertEquals(true, test("gt(len(#body), 3)"))
    Assertions.assertEquals(true, test("contains(\"Whatever I say\", \"say\")"))
    Assertions.assertEquals(true, test("gt(len(#title), 3)"))
    Assertions.assertEquals(true, test("endsWith(#title, \"(S+)\")"))
    Assertions.assertEquals(true, test("or(endsWith(#title, \"foo\"), contains(#body, \"tempor\"))"))
    Assertions.assertEquals(false, test("not(endsWith(#title, \"(S+)\"))"))
    Assertions.assertEquals(true, test("startsWith(#title, \"Lorem\")"))
    Assertions.assertEquals(true, test("contains(#title, \"elit\")"))
//  todo  Assertions.assertEquals(false, test("gt(len(#links), 0)"))
    Assertions.assertEquals(true, test("and(contains(#title, \"elit\"), contains(#url, \"example.com\"))"))
  }

  private fun test(expr: String): Boolean {
    return SimpleArticleFilter(expr.byteInputStream()).matches(article)
  }
}
