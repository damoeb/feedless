package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.NumberFilterOperator
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CompositeFilterPlugin : FilterEntityPlugin {

  private val log = LoggerFactory.getLogger(CompositeFilterPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_filter.name
  override fun listed() = true
  override fun name(): String = "Filter"

  override fun filterEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    params: PluginExecutionParamsInput,
    index: Int
  ): Boolean {
    log.info("[$corrId] filter ${webDocument.url}")
    return params.org_feedless_filter?.let { plugins ->
      plugins.all { plugin ->
        plugin.exclude?.let { !matches(webDocument, it, index) } ?: true
          && plugin.include?.let { matches(webDocument, it, index) } ?: true
      }
    } ?: true
  }

  private fun matches(
    webDocument: WebDocumentEntity,
    filterParams: CompositeFieldFilterParamsInput,
    index: Int
  ): Boolean {
    return arrayOf(
      filterParams.content?.let { applyStringOperation(webDocument.contentText!!.trim(), it) },
      filterParams.title?.let { applyStringOperation(webDocument.contentTitle!!.trim(), it) },
      filterParams.link?.let { applyStringOperation(webDocument.url, it) },
      filterParams.index?.let { applyNumberOperation(index, it) },
    ).filterNotNull()
      .all { it }
  }

  private fun applyNumberOperation(index: Int, filterParams: NumericalFilterParamsInput): Boolean {
    return when (filterParams.operator!!) {
      NumberFilterOperator.eq -> index == filterParams.value
      NumberFilterOperator.gt -> index > filterParams.value
      NumberFilterOperator.lt -> index < filterParams.value
    }
  }

  private fun applyStringOperation(fieldValue: String, filterParams: StringFilterParamsInput): Boolean {
    val value = filterParams.value
    return when (filterParams.operator!!) {
      StringFilterOperator.startsWidth -> fieldValue.startsWith(value, true)
      StringFilterOperator.contains -> fieldValue.contains(value, true)
      StringFilterOperator.matches -> fieldValue.matches(Regex(value))
      StringFilterOperator.endsWith -> fieldValue.endsWith(value, true)
    }
  }
}
