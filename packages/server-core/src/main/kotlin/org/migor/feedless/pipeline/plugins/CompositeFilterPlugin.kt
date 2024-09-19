package org.migor.feedless.pipeline.plugins

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.filter.generated.FilterByExpression
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.NumberFilterOperator
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.StringFilterOperator
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class CompositeFilterPlugin : FilterEntityPlugin {

  private val log = LoggerFactory.getLogger(CompositeFilterPlugin::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  override fun id(): String = FeedlessPlugins.org_feedless_filter.name
  override fun listed() = true
  override fun name(): String = "Filter"

  override suspend fun filterEntity(
    item: JsonItem,
    params: PluginExecutionParamsInput,
    index: Int,
    logCollector: LogCollector,
  ): Boolean {
    val corrId = coroutineContext.corrId()
    logCollector.log("applying filters to item #$index url=${item.url}")
    val keep = params.org_feedless_filter?.let { plugins ->
      plugins.all { plugin ->
        plugin.composite?.let {
          it.exclude?.let { !matches(item, it, index) } ?: true
            && it.include?.let { matches(item, it, index) } ?: true
        } ?: true
          &&
          matchesExpression(plugin.expression, item)

      }
    } ?: true

    if (keep) {
      logCollector.log("keeping item #$index")
    } else {
      logCollector.log("dropping item #$index")
    }

    return keep
  }

  private suspend fun matchesExpression(expression: String?, item: JsonItem): Boolean {
    val corrId = coroutineContext.corrId()
    return expression?.let {
      val q = tryConvertFromLegacy(it)
      try {
        log.debug("[$corrId] expression '${q}'")
        FilterByExpression(q.reader()).matches(item)
      } catch (e: Exception) {
        log.warn("[$corrId] matchesExpression failed for expression '${q}'", e)
        false
      }
    } ?: true
  }

  private suspend fun tryConvertFromLegacy(legacyExpression: String): String {
    val corrId = coroutineContext.corrId()
    val fields = mapOf(
      "#body" to "content",
      "#title" to "title",
      "#url" to "url",
      "#any" to "any",
    )
    return if (fields.keys.any { legacyExpression.contains(it) } || legacyExpression.contains("not(")) {
      var converted = legacyExpression
        .replace("not(", "!(")
        .replace("\")", "')")
        .replace(" \"", " '")
        .replace(",\"", ", '")
      fields.forEach { (old, new) -> converted = converted.replace(old, new) }

      log.debug("[$corrId] converted expression '$legacyExpression' -> '$converted'")
      converted
    } else {
      legacyExpression
    }
  }

  fun matches(
    item: JsonItem,
    filterParams: CompositeFieldFilterParamsInput,
    index: Int
  ): Boolean {
    return arrayOf(
      filterParams.content?.let { applyStringOperation(StringUtils.trimToEmpty(item.text), it) },
      filterParams.title?.let { applyStringOperation(StringUtils.trimToEmpty(item.title), it) },
      filterParams.link?.let { applyStringOperation(item.url, it) },
      filterParams.index?.let { applyNumberOperation(index, it) },
    ).filterNotNull()
      .all { it }
  }

  private fun applyNumberOperation(index: Int, filterParams: NumericalFilterParamsInput): Boolean {
    return when (filterParams.operator) {
      NumberFilterOperator.eq -> index == filterParams.value
      NumberFilterOperator.gt -> index > filterParams.value
      NumberFilterOperator.lt -> index < filterParams.value
    }
  }

  private fun applyStringOperation(fieldValue: String, filterParams: StringFilterParamsInput): Boolean {
    val value = filterParams.value
    return when (filterParams.operator) {
      StringFilterOperator.startsWidth -> fieldValue.startsWith(value, true)
      StringFilterOperator.contains -> fieldValue.contains(value, true)
      StringFilterOperator.matches -> fieldValue.matches(Regex(value))
      StringFilterOperator.endsWith -> fieldValue.endsWith(value, true)
    }
  }
}
