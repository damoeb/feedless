package org.migor.feedless.pipeline.plugins

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.scrape.LogCollector

class CompositeFilterPluginTest {

  private lateinit var item: JsonItem
  private lateinit var service: CompositeFilterPlugin

  @BeforeEach
  fun setUp() {
    service = CompositeFilterPlugin()
    item = JsonItem()
    item.title = " foo is the perfect title"
    item.text = "bar is the payload"
    item.url = ""
  }

  @Test
  fun `given no filters are present, it passes`() = runTest {
    val filter = listOf<ItemFilterParams>()
    val keep = service.filterEntity(item, filter, 0, LogCollector())
    assertThat(keep).isTrue()
  }

  @Test
  fun `given a filter expression is true, result is true`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        expression = "and(true, true)"
      )
    )
    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isTrue()
  }

  @Test
  fun `given a filter expression is false, result is false`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        expression = "and(false, true)"
      )
    )
    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isFalse()
  }

  @Test
  fun `when mixing filters, given both are true, result is true`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          include = compositeFieldFilterParamsInput()
        ),
        expression = "and(true, true)"
      )
    )
    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isTrue()
  }

  @Test
  fun `when mixing filters, given expression is false, result is false`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          include = compositeFieldFilterParamsInput()
        ),
        expression = "and(true, false)"
      )
    )
    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isFalse()
  }

  @Test
  fun `when mixing filters, given composite is false, result is false`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          exclude = compositeFieldFilterParamsInput()
        ),
        expression = "and(true, true)"
      )
    )
    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isFalse()
  }

  @Test
  fun `given a number filter is provided, it works`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          include = CompositeFieldFilterParams(
            index = NumericalFilterParams(
              operator = NumberFilterOperator.eq,
              value = 0
            )
          )
        )
      )
    )

    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isTrue()
  }

  @Test
  fun `given a string filter is provided, it works`() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          include = compositeFieldFilterParamsInput()
        )
      )
    )

    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isTrue()
  }

  @Test
  fun excludeStringFilterWorks() = runTest {
    val filter = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          exclude = compositeFieldFilterParamsInput()
        )
      )
    )

    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isFalse()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "not(contains(#any,\"Vol\"));;true",
      "startsWith(#url, \"https://www.mikrobitti.fi\");;false",
      "and( not(contains(#url, \"tiramillas\")), not(contains(#url, \"promociones\")) );;true",
//      todo "not(contains(#body, \"Movies-4K\"))';;true",
//      "endsWith(#any, \"title\");;true",
      "endsWith(#any, \"fef\");;false",
      "and(not(contains(#any, \"hans\")), contains(#any, \"foo\"));;true",
      "and(and(not(contains(#body,\" - Raw\")),not(contains(#body,\"Audio\"))),and(not(contains(#body,\"Live Action\")),not(contains(#body,\"Non-English\"))));;true",
      "and(not(contains(#any, \"not\")), and(not(contains(#any, \"must\")), contains(#any, \"must\")));;false",
    ],
    delimiterString = ";;"
  )
  fun `supports legacy filter expression`(expr: String, expected: Boolean) = runTest {
    val filter = listOf(
      ItemFilterParams(
        expression = expr
      )
    )

    assertThat(service.filterEntity(item, filter, 0, LogCollector())).isEqualTo(expected)

    val negativeFilter = listOf(
      ItemFilterParams(
        expression = "not(${expr})"
      )
    )

    assertThat(service.filterEntity(item, negativeFilter, 0, LogCollector())).isEqualTo(!expected)
  }

  private fun compositeFieldFilterParamsInput() = CompositeFieldFilterParams(
    title = StringFilterParams(
      operator = StringFilterOperator.startsWidth,
      value = "foo"
    )
  )
}
