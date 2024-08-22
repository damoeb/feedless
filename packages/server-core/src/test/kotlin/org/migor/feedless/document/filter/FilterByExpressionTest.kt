package org.migor.feedless.document.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.document.filter.generated.FilterByExpression
import org.migor.feedless.feed.parser.json.JsonItem


class FilterByExpressionTest {

  var entry: JsonItem? = null

  @BeforeEach
  fun prepare() {
    entry = JsonItem()
    entry!!.title = "What is Lorem Ipsum?"
    entry!!.url = "http://example.com"
    entry!!.contentText =
      "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum."
    entry!!.contentHtml = "<a href=\"http://foo.com\">bar</a>"
  }

  @Test
  fun testBoolExpr() {
    assertThat(evaluateExpression("and(true, true)")).isTrue()
    assertThat(evaluateExpression("or(and(true, true), false)")).isTrue()
    assertThat(evaluateExpression("and(true, false)")).isFalse()
    assertThat(evaluateExpression("or(true, false)")).isTrue()
    assertThat(evaluateExpression("or(false, false)")).isFalse()
    assertThat(evaluateExpression("true")).isTrue()
    assertThat(evaluateExpression("false")).isFalse()
    assertThat(evaluateExpression("!(false)")).isTrue()
    assertThat(evaluateExpression("!(!(false))")).isFalse()
  }

  @Test
  fun testNumberExpr() {
    assertThat(evaluateExpression("3 > 4")).isFalse()
    assertThat(evaluateExpression("5 > 4")).isTrue()
    assertThat(evaluateExpression("!(3 < 4)")).isFalse()
    assertThat(evaluateExpression("linkCount == 0")).isTrue()
//    assertThat(test("score > 0"))
  }

  @Test
  fun testStringExpr() {
    assertThat(evaluateExpression("endsWith(title, '?')")).isTrue()
    assertThat(evaluateExpression("contains(title, 'Ipsum')")).isTrue()
    assertThat(evaluateExpression("contains('Ipsum')")).isTrue()
    assertThat(evaluateExpression("or(!(contains(title, 'Ipsum')), contains(title, 'Ipsum'))")).isTrue()
    assertThat(evaluateExpression("and(!(contains(title, 'qwf')), !(contains(title, 'gwef')))")).isTrue()
    assertThat(evaluateExpression("contains(content, 'Ipsum')")).isTrue()
    assertThat(evaluateExpression("!(endsWith(title, '?'))")).isFalse()
    assertThat(evaluateExpression("endsWith(content, content)")).isTrue()
    assertThat(evaluateExpression("len('abchh') > 4")).isTrue()
    assertThat(evaluateExpression("words(content) > 4")).isTrue()
//    assertEquals(false, test("sentences(content) < 4")).isTrue()
  }

  @Test
  fun testComplexExpr() {
    assertThat(evaluateExpression("endsWith(title, '?')")).isTrue()
    assertThat(evaluateExpression("and(true, linkCount > 0)")).isFalse()
  }

  private fun evaluateExpression(expr: String) = FilterByExpression(expr.byteInputStream()).matches(entry)
}
