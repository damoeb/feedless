package org.migor.feedless.pipeline.plugins

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.NumberFilterOperator
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput

class CompositeFilterPluginTest {

  private lateinit var webDocument: DocumentEntity
  private lateinit var service: CompositeFilterPlugin
  private val corrId = "test"

  @BeforeEach
  fun setUp() {
    service = CompositeFilterPlugin()
    webDocument = DocumentEntity()
    webDocument.contentTitle = " foo is the perfect title"
    webDocument.contentText = "bar is the payload"
    webDocument.url = ""
  }

  @Test
  fun `given no filters are present, it passes`() {
    val filterParams = PluginExecutionParamsInput(
      org_feedless_filter = listOf()
    )
    val keep = service.filterEntity(corrId, webDocument, filterParams, 0)
    assertThat(keep).isTrue()
  }

  @Test
  fun `given a number filter is provided, it works`() {
    val filterParams = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        CompositeFilterParamsInput(
          include = CompositeFieldFilterParamsInput(
            index = NumericalFilterParamsInput(
              operator = NumberFilterOperator.eq,
              value = 0
            )
          )
        )
      )

    )

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isTrue()
  }

  @Test
  fun `given a string filter is provided, it works`() {
    val filterParams = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        CompositeFilterParamsInput(
          include = CompositeFieldFilterParamsInput(
            title = StringFilterParamsInput(
              operator = StringFilterOperator.startsWidth,
              value = "foo"
            )

          )

        )
      )

    )

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isTrue()
  }

  @Test
  fun excludeStringFilterWorks() {
    val filterParams = PluginExecutionParamsInput(
      org_feedless_filter = listOf(
        CompositeFilterParamsInput(
          exclude = CompositeFieldFilterParamsInput(
            title = StringFilterParamsInput(
              operator = StringFilterOperator.startsWidth,
              value = "foo"
            )
          )

        )
      )

    )

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isFalse()
  }
}
