package org.migor.feedless.document.filter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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

  @ParameterizedTest
  @CsvSource(value = [
    "and(true, true);; true",
    "!(!(true));; true",
    "((false));; false",
    "or(and(true, true), false);; true",
    "or(true, false);; true",
    "true;; true",
    "!(false);; true",
    "and(true, false);; false",
    "or(false, false);; false",
    "false;; false",
    "!(!(false));; false",
  ], delimiterString = ";;")
  fun testBoolExpr(expr: String, expected: Boolean) {
    assertThat(evaluateExpression(expr)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(value = [
    "3 > 4;; false",
    "5 > 4;; true",
    "!(3 < 4);; false",
    "linkCount == 0;; true",
    ], delimiterString = ";;")
  fun testNumberExpr(expr: String, expected: Boolean) {
    assertThat(evaluateExpression(expr)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(value = [
    "contains(title, 'Ipsum-12');; false",
    "contains(any,'Ipsum-12');; false",
    "!(contains(any, 'Ipsum-12'));; true",
//    todo "startsWith(any, 'What');; true",
    "!(contains(content, 'Movies-4K'));;true",
    "startsWith(url, 'http://exam');; true",
    "startsWith(title, 'What is ');; true",
    "contains('Ipsum');; true",
    "or(!(contains(title, 'Ipsum')), contains(title, 'Ipsum'));; true",
    "and(!(contains(title, 'qwf')), !(contains(title, 'gwef')));; true",
    "contains(content, 'Ipsum');; true",
    "!(endsWith(title, '?'));; false",
    "endsWith(content, content);; true",
    "len('abchh') > 4;; true",
    "words(content) > 4;; true",
  ],
    delimiterString = ";;")
  fun testStringExpr(expr: String, expected: Boolean) {
    assertThat(evaluateExpression(expr)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(value = [
    "endsWith(title, '?');; true",
    "and(true, linkCount > 0);; false",
  ], delimiterString = ";;")
  fun testComplexExpr(expr: String, expected: Boolean) {
    assertThat(evaluateExpression(expr)).isEqualTo(expected)
  }

  private fun evaluateExpression(expr: String) = FilterByExpression(expr.byteInputStream()).matches(entry)
}
