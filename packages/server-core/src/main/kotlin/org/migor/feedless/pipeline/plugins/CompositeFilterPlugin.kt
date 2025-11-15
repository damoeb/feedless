package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.filter.generated.FilterByExpression
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.FilterEntityPlugin
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.coroutines.coroutineContext

data class ItemFilterParams(
  @SerializedName("composite") val composite: CompositeFilterParams? = null,
  @SerializedName("expression") val expression: String? = null,
)

data class CompositeFilterParams(
  @SerializedName("exclude") val exclude: CompositeFieldFilterParams? = null,
  @SerializedName("include") val include: CompositeFieldFilterParams? = null,
)

typealias CompositeFilterPluginParams = List<ItemFilterParams>

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class CompositeFilterPlugin : FilterEntityPlugin<CompositeFilterPluginParams?> {

  private val log = LoggerFactory.getLogger(CompositeFilterPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_filter.name
  override fun listed() = true
  override fun name(): String = "Filter"

  override suspend fun filterEntity(
    item: JsonItem,
    params: CompositeFilterPluginParams?,
    index: Int,
    logCollector: LogCollector,
  ): Boolean {
    logCollector.log("applying filters to item #$index url=${item.url}")
    val keep = params?.let { plugins ->
      plugins.all { plugin ->
        plugin.composite?.let {
          it.exclude?.let { !matches(item, it, index) } != false
            && it.include?.let { matches(item, it, index) } != false
        } != false
          &&
          matchesExpression(plugin.expression, item)
      }
    } != false

    if (keep) {
      logCollector.log("keeping item #$index")
    } else {
      logCollector.log("dropping item #$index")
    }

    return keep
  }

  override suspend fun filterEntity(
    item: JsonItem,
    jsonParams: String?,
    index: Int,
    logCollector: LogCollector
  ): Boolean {
    return this.filterEntity(item, fromJson(jsonParams), index, logCollector)
  }

  override suspend fun fromJson(jsonParams: String?): CompositeFilterPluginParams? {
    return jsonParams?.let {
      val listType = object : TypeToken<List<ItemFilterParams>>() {}.type
      Gson().fromJson(jsonParams, listType)
    }
  }

  private suspend fun matchesExpression(expression: String?, item: JsonItem): Boolean {
    val corrId = coroutineContext.corrId()
    return expression?.let {
      val q = tryConvertFromLegacy(it)
      try {
        log.debug("[$corrId] expression '${q}'")
        FilterByExpression(q.reader()).matches(item)
      } catch (e: Exception) {
        log.warn("[$corrId] matchesExpression failed for expression '${q}'")
        false
      }
    } != false
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
    filterParams: CompositeFieldFilterParams,
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

  private fun applyNumberOperation(index: Int, filterParams: NumericalFilterParams): Boolean {
    return when (filterParams.operator) {
      NumberFilterOperator.eq -> index == filterParams.value
      NumberFilterOperator.gt -> index > filterParams.value
      NumberFilterOperator.lt -> index < filterParams.value
    }
  }

  private fun applyStringOperation(fieldValue: String, filterParams: StringFilterParams): Boolean {
    val value = filterParams.value
    return when (filterParams.operator) {
      StringFilterOperator.startsWidth -> fieldValue.startsWith(value, true)
      StringFilterOperator.contains -> fieldValue.contains(value, true)
      StringFilterOperator.matches -> fieldValue.matches(Regex(value))
      StringFilterOperator.endsWith -> fieldValue.endsWith(value, true)
    }
  }
}
