package org.migor.feedless.jpa.source.actions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.migor.feedless.generated.types.ConditionalTagInput
import org.migor.feedless.generated.types.DiffRecordsParamsInput
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.ItemFilterParamsInput


@JsonIgnoreProperties(ignoreUnknown = true)
data class PluginExecutionJsonEntity(
  @JsonProperty("org_feedless_feed") val org_feedless_feed: FeedParamsInput? = null,
  @JsonProperty("org_feedless_diff_email_forward") val org_feedless_diff_records: DiffRecordsParamsInput? = null,
  @JsonProperty("org_feedless_filter") val org_feedless_filter: List<ItemFilterParamsInput>? = null,
  @JsonProperty("jsonData") val jsonData: String? = null,
  @JsonProperty("org_feedless_fulltext") val org_feedless_fulltext: FulltextPluginParamsInput? = null,
  @JsonProperty("org_feedless_conditional_tag") val org_feedless_conditional_tag: List<ConditionalTagInput>? = null,
) {
}
