package org.migor.feedless.pipeline.plugins

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.NumberFilterOperator
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.mockito.Mockito.mock

class CompositeFilterPluginTest {

  private lateinit var item: JsonItem
  private lateinit var service: CompositeFilterPlugin
  private val corrId = "test"

  @BeforeEach
  fun setUp() {
    service = CompositeFilterPlugin()
    service.propertyService = mock(PropertyService::class.java)
    item = JsonItem()
    item.title = " foo is the perfect title"
    item.contentText = "bar is the payload"
    item.url = ""
  }

  @Test
  fun `given no filters are present, it passes`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf()
    )
    val keep = service.filterEntity(corrId, item, filter, 0)
    assertThat(keep).isTrue()
  }

  @Test
  fun `given a filter expression is true, result is true`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          expression = "and(true, true)"
        )
      )
    )
    assertThat(service.filterEntity(corrId, item, filter, 0)).isTrue()
  }

  @Test
  fun `given a filter expression is false, result is false`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          expression = "and(false, true)"
        )
      )
    )
    assertThat(service.filterEntity(corrId, item, filter, 0)).isFalse()
  }

  @Test
  fun `when mixing filters, given both are true, result is true`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            include = compositeFieldFilterParamsInput()
          ),
          expression = "and(true, true)"
        )
      )
    )
    assertThat(service.filterEntity(corrId, item, filter, 0)).isTrue()
  }

  @Test
  fun `when mixing filters, given expression is false, result is false`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            include = compositeFieldFilterParamsInput()
          ),
          expression = "and(true, false)"
        )
      )
    )
    assertThat(service.filterEntity(corrId, item, filter, 0)).isFalse()
  }

  @Test
  fun `when mixing filters, given composite is false, result is false`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            exclude = compositeFieldFilterParamsInput()
          ),
          expression = "and(true, true)"
        )
      )
    )
    assertThat(service.filterEntity(corrId, item, filter, 0)).isFalse()
  }

  @Test
  fun `given a number filter is provided, it works`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            include = CompositeFieldFilterParamsInput(
              index = NumericalFilterParamsInput(
                operator = NumberFilterOperator.eq,
                value = 0
              )
            )
          )
        )
      )
    )

    assertThat(service.filterEntity(corrId, item, filter, 0)).isTrue()
  }

  @Test
  fun `given a string filter is provided, it works`() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            include = compositeFieldFilterParamsInput()
          )
        )
      )

    )

    assertThat(service.filterEntity(corrId, item, filter, 0)).isTrue()
  }

  @Test
  fun excludeStringFilterWorks() {
    val filter = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        ItemFilterParamsInput(
          composite = CompositeFilterParamsInput(
            exclude = compositeFieldFilterParamsInput()
          )
        )
      )
    )

    assertThat(service.filterEntity(corrId, item, filter, 0)).isFalse()
  }

  private fun compositeFieldFilterParamsInput() = CompositeFieldFilterParamsInput(
    title = StringFilterParamsInput(
      operator = StringFilterOperator.startsWidth,
      value = "foo"
    )
  )
}
