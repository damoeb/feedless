package org.migor.rss.rich.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.rss.rich.filter.generated.TakeEntryIfRunner
import org.migor.rss.rich.model.SourceEntry

class TakeEntryIfRunnerTest {

  var entry: SourceEntry? = null

  @BeforeEach
  fun prepare() {
    entry = SourceEntry()
    entry!!.title = "What is Lorem Ipsum?"
    entry!!.content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    entry!!.contentHtml = "<a href=\"http://foo.com\">bar</a>"
  }

  @Test
  fun testBoolExpr() {
    assertEquals(true, runner("true and true"))
    assertEquals(true, runner("true and true or false"))
    assertEquals(false, runner("true and false"))
    assertEquals(true, runner("true or false"))
    assertEquals(false, runner("false or false"))
    assertEquals(true, runner("true"))
    assertEquals(false, runner("false"))
    assertEquals(true, runner("not(false)"))
    assertEquals(false, runner("not(not(false))"))
  }

  @Test
  fun testNumberExpr() {
    assertEquals(false, runner("3 > 4"))
    assertEquals(true, runner("5 > 4"))
    assertEquals(false, runner("not(3 < 4)"))
    assertEquals(true, runner("linkCount > 0"))
    assertEquals(false, runner("score > 0"))
  }

  @Test
  fun testStringExpr() {
    assertEquals(true, runner("endsWith(title, '?')"))
    assertEquals(false, runner("not(endsWith(title, '?'))"))
    assertEquals(true, runner("endsWith(content, content)"))
    assertEquals(true, runner("len('abchh') > 4"))
    assertEquals(true, runner("words(content) > 4"))
    assertEquals(false, runner("sentences(content) < 4"))
  }

  private fun runner(expr: String) = TakeEntryIfRunner(expr.byteInputStream()).takeIf(entry)
}
