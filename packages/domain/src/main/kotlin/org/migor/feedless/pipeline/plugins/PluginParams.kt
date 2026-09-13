package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.scrape.GenericFeedRule

data class DiffRecordsParams(
  @SerializedName("nextItemMinIncrement")
  val nextItemMinIncrement: Double,
  @SerializedName("compareBy")
  val compareBy: CompareBy,
  @SerializedName("inlineDiffImage")
  val inlineDiffImage: Boolean? = null,
  @SerializedName("inlineLatestImage")
  val inlineLatestImage: Boolean? = null,
  @SerializedName("inlinePreviousImage")
  val inlinePreviousImage: Boolean? = null,
)

data class CompareBy(
  @SerializedName("fragmentNameRef")
  val fragmentNameRef: String? = null,
  @SerializedName("field")
  val `field`: RecordField,
)

enum class RecordField {
  pixel,
  text,
  markup,
}

data class ItemFilterParams(
  @SerializedName("composite") val composite: CompositeFilterParams? = null,
  @SerializedName("expression") val expression: String? = null,
)

data class CompositeFilterParams(
  @SerializedName("exclude") val exclude: CompositeFieldFilterParams? = null,
  @SerializedName("include") val include: CompositeFieldFilterParams? = null,
)

typealias CompositeFilterPluginParams = List<ItemFilterParams>

data class ConditionalTag(
  @SerializedName("tag") val tag: String,
  @SerializedName("filter") val filter: CompositeFieldFilterParams,
)

data class CompositeFieldFilterParams(
  @SerializedName("index") val index: NumericalFilterParams? = null,
  @SerializedName("title") val title: StringFilterParams? = null,
  @SerializedName("content") val content: StringFilterParams? = null,
  @SerializedName("link") val link: StringFilterParams? = null,
)

typealias ConditionalTagPluginParams = List<ConditionalTag>

data class FeedPluginParams(
  val generic: GenericFeedRule? = null,
)

data class FulltextPluginParams(
  @SerializedName("readability") val readability: Boolean,
  @SerializedName("summary") val summary: Boolean,
  @SerializedName("inheritParams") val inheritParams: Boolean,
  @SerializedName("onErrorRemove") val onErrorRemove: Boolean? = null,
)

data class EventsReportPluginParams(
  val language: String,
  val from: String,
  val to: String,
  val subject: String,
)

fun EventsReportPluginParams.toPluginExecutionJson(): PluginExecutionJson {
  return PluginExecutionJson(
    paramsJsonString = Gson().toJson(this)
  )
}
