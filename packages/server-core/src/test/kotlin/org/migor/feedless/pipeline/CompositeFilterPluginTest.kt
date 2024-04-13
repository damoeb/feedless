package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.NumberFilterOperator
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin

class CompositeFilterPluginTest {

  private lateinit var webDocument: WebDocumentEntity
  private lateinit var service: CompositeFilterPlugin
  private val corrId = "-"

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
      .org_feedless_filter(listOf())
      .build()
    val keep = service.filterEntity(corrId, webDocument, filterParams, 0)
    assertThat(keep).isTrue()
  }

  @Test
  fun includeNumberFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .org_feedless_filter(
        listOf(
          CompositeFilterParamsInput.newBuilder()
            .include(
              CompositeFieldFilterParamsInput.newBuilder()
                .index(
                  NumericalFilterParamsInput.newBuilder()
                    .operator(NumberFilterOperator.eq)
                    .value(0)
                    .build()
                )
                .build()
            )
            .build()
        )
      )
      .build()

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isTrue()
  }

  @Test
  fun includeStringFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .org_feedless_filter(
        listOf(
          CompositeFilterParamsInput.newBuilder()
            .include(
              CompositeFieldFilterParamsInput.newBuilder()
                .title(
                  StringFilterParamsInput.newBuilder()
                    .operator(StringFilterOperator.startsWidth)
                    .value("foo")
                    .build()
                )
                .build()
            )
            .build()
        )
      )
      .build()

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isTrue()
  }

  @Test
  fun excludeStringFilterWorks() {
    val filterParams = PluginExecutionParamsInput.newBuilder()
      .org_feedless_filter(
        listOf(
          CompositeFilterParamsInput.newBuilder()
            .exclude(
              CompositeFieldFilterParamsInput.newBuilder()
                .title(
                  StringFilterParamsInput.newBuilder()
                    .operator(StringFilterOperator.startsWidth)
                    .value("foo")
                    .build()
                )
                .build()
            )
            .build()
        )
      )
      .build()

    assertThat(service.filterEntity(corrId, webDocument, filterParams, 0)).isFalse()
  }
}
