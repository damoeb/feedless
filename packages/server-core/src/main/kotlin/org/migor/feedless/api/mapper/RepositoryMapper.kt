package org.migor.feedless.api.mapper

import com.google.gson.Gson
import org.migor.feedless.api.toDto
import org.migor.feedless.generated.types.FeedParams
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.pipeline.plugins.DiffRecordsParams
import org.migor.feedless.pipeline.plugins.FulltextPluginParams
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.Repository
import org.migor.feedless.util.toMillis
import org.migor.feedless.generated.types.Harvest as HarvestDto
import org.migor.feedless.generated.types.PluginExecution as PluginExecutionDto
import org.migor.feedless.generated.types.PluginExecutionParams as PluginExecutionParamsDto
import org.migor.feedless.generated.types.Repository as RepositoryDto

fun Repository.toDto(currentUserIsOwner: Boolean): RepositoryDto {
  return RepositoryDto(
    id = id.uuid.toString(),
    ownerId = ownerId.uuid.toString(),
    product = product.toDto(),
    disabledFrom = disabledFrom?.toMillis(),
    plugins = plugins.map { it.toDto() },
    retention = toRetention(),
    shareKey = if (currentUserIsOwner) shareKey else "",
    visibility = visibility.toDto(),
    createdAt = createdAt.toMillis(),
    lastUpdatedAt = lastUpdatedAt.toMillis(),
    nextUpdateAt = triggerScheduledNextAt?.toMillis(),
    refreshCron = sourcesSyncCron,
    tags = tags.asList(),
    documentCount = documentCountSinceCreation.toLong(),
    sourcesCount = 0, // todo implementx
    sourcesCountWithProblems = 0, // todo implementx
    annotations = null,
    archived = archived,
    description = description,
    title = title,
    currentUserIsOwner = currentUserIsOwner,
    pushNotificationsEnabled = pushNotificationsEnabled,
    pullsPerMonth = pullsPerMonth
  )
}

fun Repository.toRetention(): Retention {
  return Retention(
    maxCapacity = retentionMaxCapacity,
    maxAgeDays = retentionMaxAgeDays
  )
}

fun PluginExecution.toDto(): PluginExecutionDto {
  return PluginExecutionDto(
    pluginId = id,
    params = when (id) {
      FeedlessPlugins.org_feedless_fulltext.name -> PluginExecutionParamsDto(
        org_feedless_fulltext = Gson().fromJson(
          params.paramsJsonString,
          FulltextPluginParams::class.java
        ).toDto(),
      )

      FeedlessPlugins.org_feedless_diff_records.name -> PluginExecutionParamsDto(
        org_feedless_diff_records = Gson().fromJson(
          params.paramsJsonString,
          DiffRecordsParams::class.java
        ).toDto()
      )

//            FeedlessPlugins.org_feedless_conditional_tag.name -> PluginExecutionParamsDto(
//                org_feedless_conditional_tag = Gson().fromJson(
//                    params.paramsJsonString,
//                    ConditionalTagPluginParams::class.java
//                ).toDto()
//            )

      else -> throw IllegalArgumentException("Undefined dto mapping for '${id}'")
    },
  )
}

//fun ConditionalTagPluginParams.toDto(): ConditionalTagPluginParamsDto {
//    return ConditionalTagPluginParamsDto(
//        tag = tag,
//        filter = filter?.toDto()
//    )
//}

//fun NumericalFilterParams.toDto(): NumericalFilterParamsDto {
//    return NumericalFilterParamsDto(
//        value = value,
//        operator = operator.toDto(),
//    )
//}

fun Harvest.toDto(): HarvestDto {
  return HarvestDto(
    ok = !errornous,
    itemsAdded = itemsAdded,
    itemsIgnored = itemsIgnored,
    logs = logs,
    startedAt = startedAt.toMillis(),
    finishedAt = finishedAt?.toMillis()
  )
}

fun FeedParamsInput.toDto(): FeedParams {
  return FeedParams(
    generic = generic?.toDto(),
  )
}

fun org.migor.feedless.generated.types.SelectorsInput.toDto(): org.migor.feedless.generated.types.Selectors {
  return org.migor.feedless.generated.types.Selectors(
    contextXPath = contextXPath,
    linkXPath = linkXPath,
    dateXPath = dateXPath,
    extendContext = extendContext,
    dateIsStartOfEvent = org.apache.commons.lang3.BooleanUtils.isTrue(dateIsStartOfEvent),
    paginationXPath = paginationXPath,
  )
}

