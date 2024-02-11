package org.migor.feedless.plugins

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.CompositeFilterField
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterType
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator

class CompositeFilterPluginTest {

  private lateinit var webDocument: WebDocumentEntity
  private lateinit var service: CompositeFilterPlugin
  val corrId = "-"

  @BeforeEach
  fun setUp() {
    service = CompositeFilterPlugin()
    webDocument = WebDocumentEntity()
    webDocument.contentTitle = " foo is the perfect title"
    webDocument.contentText = ""
    webDocument.url = ""
  }

  @Test
  fun noFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .filters(listOf())
      .build()
    val keep = service.filterEntity(corrId, webDocument, filterParams)
    assertThat(keep).isTrue()
  }

  @Test
  fun includeFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .filters(
        listOf(
          CompositeFilterParamsInput.newBuilder()
            .type(CompositeFilterType.include)
            .field(CompositeFilterField.title)
            .operator(StringFilterOperator.startsWidth)
            .value("foo")
            .build()
        )
      )
      .build()

    assertThat(service.filterEntity(corrId, webDocument, filterParams)).isTrue()
  }

  @Test
  fun excludeFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .filters(
        listOf(
          CompositeFilterParamsInput.newBuilder()
            .type(CompositeFilterType.exclude)
            .field(CompositeFilterField.title)
            .operator(StringFilterOperator.startsWidth)
            .value("foo")
            .build()
        )
      )
      .build()

    assertThat(service.filterEntity(corrId, webDocument, filterParams)).isFalse()
  }
}
