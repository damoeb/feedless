package org.migor.feedless.plugins

import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.CompositeFilterField
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterType
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
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
    params: PluginExecutionParamsInput
  ): Boolean {
    log.info("[$corrId] filter ${webDocument.url}")
    return params.org_feedless_filter?.let {
      it.all {
        when (it.type!!) {
          CompositeFilterType.include -> matches(webDocument, it)
          CompositeFilterType.exclude -> !matches(webDocument, it)
        }
      }

    } ?: true
  }

  private fun matches(it: WebDocumentEntity, filterParams: CompositeFilterParamsInput): Boolean {
    val fieldValue = it.get(filterParams.field).trim()
    val value = filterParams.value
    return when (filterParams.operator!!) {
      StringFilterOperator.startsWidth -> fieldValue.startsWith(value, true)
      StringFilterOperator.contains -> fieldValue.contains(value, true)
      StringFilterOperator.matches -> fieldValue.matches(Regex(value))
      StringFilterOperator.endsWith -> fieldValue.endsWith(value, true)
    }
  }
}

private fun WebDocumentEntity.get(field: CompositeFilterField): String {
  return when (field) {
    CompositeFilterField.title -> contentTitle!!
    CompositeFilterField.content -> contentText!!
    CompositeFilterField.link -> url
  }
}
