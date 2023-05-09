package org.migor.feedless.harvest.entryfilter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.harvest.entryfilter.complex.generated.ComplexArticleFilter

class ComplexArticleFilterTest {

  var entry: WebDocumentEntity? = null

  @BeforeEach
  fun prepare() {
    entry = WebDocumentEntity()
    entry!!.title = "What is Lorem Ipsum?"
    entry!!.url = "http://example.com"
    entry!!.contentText =
      "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    entry!!.contentRaw = "<a href=\"http://foo.com\">bar</a>"
    entry!!.contentRawMime = "text/html"
  }

  @Test
  fun testBoolExpr() {
    assertEquals(true, test("and(true, true)"))
    assertEquals(true, test("or(and(true, true), false)"))
    assertEquals(false, test("and(true, false)"))
    assertEquals(true, test("or(true, false)"))
    assertEquals(false, test("or(false, false)"))
    assertEquals(true, test("true"))
    assertEquals(false, test("false"))
    assertEquals(true, test("!(false)"))
    assertEquals(false, test("!(!(false))"))
  }

  @Test
  fun testNumberExpr() {
    assertEquals(false, test("3 > 4"))
    assertEquals(true, test("5 > 4"))
    assertEquals(false, test("!(3 < 4)"))
    assertEquals(true, test("linkCount == 0"))
//    assertEquals(false, test("score > 0"))
  }

  @Test
  fun testStringExpr() {
    assertEquals(true, test("endsWith(title, '?')"))
    assertEquals(true, test("contains(title, 'Ipsum')"))
    assertEquals(true, test("contains('Ipsum')"))
    assertEquals(true, test("or(!(contains(title, 'Ipsum')), contains(title, 'Ipsum'))"))
    assertEquals(true, test("and(!(contains(title, 'qwf')), !(contains(title, 'gwef')))"))
    assertEquals(true, test("contains(content, 'Ipsum')"))
    assertEquals(false, test("!(endsWith(title, '?'))"))
    assertEquals(true, test("endsWith(content, content)"))
    assertEquals(true, test("len('abchh') > 4"))
    assertEquals(true, test("words(content) > 4"))
//    assertEquals(false, test("sentences(content) < 4"))
  }

  @Test
  fun testComplexExpr() {
    assertEquals(true, test("endsWith(title, '?')"))
    assertEquals(false, test("and(true, linkCount > 0)"))
  }

  private fun test(expr: String) = ComplexArticleFilter(expr.byteInputStream()).matches(entry)
}
