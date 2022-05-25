package org.migor.rich.rss.harvest.entryfilter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.harvest.entryfilter.complex.generated.TakeEntryIfRunner

class TakeEntryIfRunnerTest {

  var entry: Article? = null

  @BeforeEach
  fun prepare() {
    entry = Article()
    entry!!.title = "What is Lorem Ipsum?"
    entry!!.url = "http://example.com"
    entry!!.contentText = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    entry!!.contentRaw = "<a href=\"http://foo.com\">bar</a>"
    entry!!.contentRawMime = "text/html"
  }

  @Test
  fun testBoolExpr() {
    assertEquals(true, test("true && true"))
    assertEquals(true, test("true && true || false"))
    assertEquals(false, test("true && false"))
    assertEquals(true, test("true || false"))
    assertEquals(false, test("false || false"))
    assertEquals(true, test("true"))
    assertEquals(false, test("false"))
    assertEquals(true, test("not(false)"))
    assertEquals(false, test("not(not(false))"))
  }

  @Test
  fun testNumberExpr() {
    assertEquals(false, test("3 > 4"))
    assertEquals(true, test("5 > 4"))
    assertEquals(false, test("not(3 < 4)"))
    assertEquals(true, test("linkCount > 0"))
    assertEquals(false, test("score > 0"))
  }

  @Test
  fun testStringExpr() {
    assertEquals(true, test("endsWith(title, '?')"))
    assertEquals(false, test("not(endsWith(title, '?'))"))
    assertEquals(true, test("endsWith(content, content)"))
    assertEquals(true, test("len('abchh') > 4"))
//    assertEquals(true, test("words(content) > 4"))
//    assertEquals(false, test("sentences(content) < 4"))
  }

  @Test
  fun testComplexExpr() {
    assertEquals(true, test("endsWith(title, '?')"))
    assertEquals(true, test("true && linkCount > 0"))
  }

  private fun test(expr: String) = TakeEntryIfRunner(expr.byteInputStream()).takeIf(entry)
}
