package org.migor.feedless.api

import com.google.gson.Gson
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.generated.types.FeedParams
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.harvest.Harvest
import org.migor.feedless.pipeline.plugins.CompareBy
import org.migor.feedless.pipeline.plugins.CompositeFieldFilterParams
import org.migor.feedless.pipeline.plugins.CompositeFilterParams
import org.migor.feedless.pipeline.plugins.ConditionalTagPluginParams
import org.migor.feedless.pipeline.plugins.DiffRecordsParams
import org.migor.feedless.pipeline.plugins.FulltextPluginParams
import org.migor.feedless.pipeline.plugins.ItemFilterParams
import org.migor.feedless.pipeline.plugins.NumericalFilterParams
import org.migor.feedless.pipeline.plugins.RecordField
import org.migor.feedless.pipeline.plugins.StringFilterOperator
import org.migor.feedless.pipeline.plugins.StringFilterParams
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.util.toMillis
import org.migor.feedless.generated.types.CompareBy as CompareByDto
import org.migor.feedless.generated.types.CompositeFieldFilterParams as CompositeFieldFilterParamsDto
import org.migor.feedless.generated.types.CompositeFilterParams as CompositeFilterParamsDto
import org.migor.feedless.generated.types.DiffRecordsParams as DiffRecordsParamsDto
import org.migor.feedless.generated.types.FulltextPluginParams as FulltextPluginParamsDto
import org.migor.feedless.generated.types.Harvest as HarvestDto
import org.migor.feedless.generated.types.ItemFilterParams as ItemFilterParamsDto
import org.migor.feedless.generated.types.NumericalFilterParams as NumericalFilterParamsDto
import org.migor.feedless.generated.types.PluginExecution as PluginExecutionDto
import org.migor.feedless.generated.types.PluginExecutionParams as PluginExecutionParamsDto
import org.migor.feedless.generated.types.RecordField as RecordFieldDto
import org.migor.feedless.generated.types.StringFilterOperator as StringFilterOperatorDto
import org.migor.feedless.generated.types.StringFilterParams as StringFilterParamsDto

fun RepositoryEntity.toDto(currentUserIsOwner: Boolean): Repository {
  return Repository(
    id = id.toString(),
    ownerId = ownerId.toString(),
    product = product.toDto(),
    disabledFrom = disabledFrom?.toMillis(),
    plugins = plugins.map { it.toDto() },
    archived = archived,
    retention =
      Retention(
        maxCapacity = retentionMaxCapacity,
        maxAgeDays = retentionMaxAgeDays
      ),
    shareKey = if (currentUserIsOwner) {
      shareKey
    } else {
      ""
    },
    visibility = visibility.toDto(),
    createdAt = createdAt.toMillis(),
    lastUpdatedAt = lastUpdatedAt.toMillis(),

    nextUpdateAt = triggerScheduledNextAt?.toMillis(),
    description = description,
    title = title,
    refreshCron = sourcesSyncCron,
    documentCount = 0,
    tags = tags.asList(),
    sourcesCount = 0,
    sourcesCountWithProblems = 0,
    currentUserIsOwner = currentUserIsOwner,
    pushNotificationsEnabled = pushNotificationsEnabled,
    pullsPerMonth = pullsPerMonth,
    annotations = null
  )
}

private fun PluginExecution.toDto(): PluginExecutionDto {
  return PluginExecutionDto(
    pluginId = id,
    params = when (id) {
      FeedlessPlugins.org_feedless_fulltext.name -> PluginExecutionParamsDto(
        org_feedless_fulltext = Gson().fromJson(params.paramsJsonString, FulltextPluginParams::class.java).toDto(),
      )

      FeedlessPlugins.org_feedless_diff_records.name -> PluginExecutionParamsDto(
        org_feedless_diff_records = Gson().fromJson(
          params.paramsJsonString,
          DiffRecordsParams::class.java
        ).toDto()
      )

      FeedlessPlugins.org_feedless_conditional_tag.name -> PluginExecutionParamsDto(
        org_feedless_conditional_tag = Gson().fromJson(
          params.paramsJsonString,
          ConditionalTagPluginParams::class.java
        ).toDto()
      )
    },
  )
}

fun PluginExecutionJson.toDto(): PluginExecutionParamsDto {
  // todo fix
//  paramsJsonString
  return PluginExecutionParamsDto(
//    org_feedless_fulltext = org_feedless_fulltext?.toDto(),
//    org_feedless_feed = org_feedless_feed?.toDto(),
//    org_feedless_diff_records = org_feedless_diff_records?.toDto(),
//    jsonData = jsonData,
//    org_feedless_filter = org_feedless_filter?.map { it.toDto() },
  )
}

private fun ItemFilterParams.toDto(): ItemFilterParamsDto {
  return ItemFilterParamsDto(
    composite = composite?.toDto(),
    expression = expression
  )
}

fun FeedParamsInput.toDto(): FeedParams {
  return FeedParams(
    generic = generic?.fromDto(),
  )
}

fun CompositeFilterParams.toDto(): CompositeFilterParamsDto {
  return CompositeFilterParamsDto(
    include = include?.toDto(),
    exclude = exclude?.toDto(),
  )
}

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


fun CompositeFieldFilterParams.toDto(): CompositeFieldFilterParamsDto {
  return CompositeFieldFilterParamsDto(
    index = index?.toDto(),
    title = title?.toDto(),
    content = content?.toDto(),
    link = link?.toDto(),
  )
}

private fun NumericalFilterParams.toDto(): NumericalFilterParamsDto {
  return NumericalFilterParamsDto(
    value = value,
    operator = operator.toDto(),
  )
}

private fun StringFilterParams.toDto(): StringFilterParamsDto {
  return StringFilterParamsDto(
    value = value,
    operator = operator.toDto(),
  )
}

private fun DiffRecordsParams.toDto(): DiffRecordsParamsDto {
  return DiffRecordsParamsDto(
    compareBy = compareBy.toDto(),
    nextItemMinIncrement = nextItemMinIncrement,
    inlineDiffImage = inlineDiffImage,
    inlineLatestImage = inlineLatestImage,
    inlinePreviousImage = inlinePreviousImage,
  )
}

private fun CompareBy.toDto(): CompareByDto {
  return CompareByDto(
    field = field.toDto(),
    fragmentNameRef = fragmentNameRef,
  )
}

private fun FulltextPluginParams.toDto(): FulltextPluginParamsDto {
  return FulltextPluginParamsDto(
    readability = readability,
    summary = summary,
    inheritParams = false
  )
}

fun RecordDateField.fromDto(): MaxAgeDaysDateField {
  return when (this) {
    RecordDateField.createdAt -> MaxAgeDaysDateField.createdAt
    RecordDateField.startingAt -> MaxAgeDaysDateField.startingAt
    RecordDateField.publishedAt -> MaxAgeDaysDateField.publishedAt
  }
}

fun RecordField.toDto(): RecordFieldDto {
  return when (this) {
    RecordField.text -> RecordFieldDto.text
    RecordField.pixel -> RecordFieldDto.pixel
    RecordField.markup -> RecordFieldDto.markup
  }
}

fun StringFilterOperator.toDto(): StringFilterOperatorDto {
  return when (this) {
    StringFilterOperator.contains -> StringFilterOperatorDto.contains
    StringFilterOperator.matches -> StringFilterOperatorDto.matches
    StringFilterOperator.endsWith -> StringFilterOperatorDto.endsWith
    StringFilterOperator.startsWidth -> StringFilterOperatorDto.startsWidth
  }
}

