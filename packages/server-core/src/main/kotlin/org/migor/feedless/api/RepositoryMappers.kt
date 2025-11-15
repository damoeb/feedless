package org.migor.feedless.api

import org.migor.feedless.data.jpa.harvest.HarvestEntity
import org.migor.feedless.data.jpa.repository.MaxAgeDaysDateField
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.data.jpa.source.actions.PluginExecutionJsonEntity
import org.migor.feedless.generated.types.CompareBy
import org.migor.feedless.generated.types.CompareByInput
import org.migor.feedless.generated.types.CompositeFieldFilterParams
import org.migor.feedless.generated.types.CompositeFieldFilterParamsInput
import org.migor.feedless.generated.types.CompositeFilterParams
import org.migor.feedless.generated.types.CompositeFilterParamsInput
import org.migor.feedless.generated.types.DiffRecordsParams
import org.migor.feedless.generated.types.DiffRecordsParamsInput
import org.migor.feedless.generated.types.FeedParams
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FulltextPluginParams
import org.migor.feedless.generated.types.FulltextPluginParamsInput
import org.migor.feedless.generated.types.Harvest
import org.migor.feedless.generated.types.ItemFilterParams
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.NumericalFilterParams
import org.migor.feedless.generated.types.NumericalFilterParamsInput
import org.migor.feedless.generated.types.PluginExecution
import org.migor.feedless.generated.types.PluginExecutionParams
import org.migor.feedless.generated.types.RecordDateField
import org.migor.feedless.generated.types.Repository
import org.migor.feedless.generated.types.Retention
import org.migor.feedless.generated.types.StringFilterParams
import org.migor.feedless.generated.types.StringFilterParamsInput
import org.migor.feedless.util.toMillis

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

private fun org.migor.feedless.data.jpa.repository.PluginExecution.toDto(): PluginExecution {
  return PluginExecution(
    pluginId = id,
    params = params.toDto(),
  )
}

fun PluginExecutionJsonEntity.toDto(): PluginExecutionParams {
  // todo fix
  return PluginExecutionParams(
//    org_feedless_fulltext = org_feedless_fulltext?.toDto(),
//    org_feedless_feed = org_feedless_feed?.toDto(),
//    org_feedless_diff_records = org_feedless_diff_records?.toDto(),
//    jsonData = jsonData,
//    org_feedless_filter = org_feedless_filter?.map { it.toDto() },
  )
}

private fun ItemFilterParamsInput.toDto(): ItemFilterParams {
  return ItemFilterParams(
    composite = composite?.toDto(),
    expression = expression
  )
}

fun FeedParamsInput.toDto(): FeedParams {
  return FeedParams(
    generic = generic?.fromDto(),
  )
}

fun CompositeFilterParamsInput.toDto(): CompositeFilterParams {
  return CompositeFilterParams(
    include = include?.toDto(),
    exclude = exclude?.toDto(),
  )
}

fun HarvestEntity.toDto(): Harvest {
  return Harvest(
    ok = !errornous,
    itemsAdded = itemsAdded,
    itemsIgnored = itemsIgnored,
    logs = logs,
    startedAt = startedAt.toMillis(),
    finishedAt = finishedAt?.toMillis()
  )
}


fun CompositeFieldFilterParamsInput.toDto(): CompositeFieldFilterParams {
  return CompositeFieldFilterParams(
    index = index?.toDto(),
    title = title?.toDto(),
    content = content?.toDto(),
    link = link?.toDto(),
  )
}

private fun NumericalFilterParamsInput.toDto(): NumericalFilterParams {
  return NumericalFilterParams(
    value = value,
    operator = operator,
  )
}

private fun StringFilterParamsInput.toDto(): StringFilterParams {
  return StringFilterParams(
    value = value,
    operator = operator,
  )
}

private fun DiffRecordsParamsInput.toDto(): DiffRecordsParams {
  return DiffRecordsParams(
    compareBy = compareBy.toDto(),
    nextItemMinIncrement = nextItemMinIncrement,
    inlineDiffImage = inlineDiffImage,
    inlineLatestImage = inlineLatestImage,
    inlinePreviousImage = inlinePreviousImage,
  )
}

private fun CompareByInput.toDto(): CompareBy {
  return CompareBy(
    field = field,
    fragmentNameRef = fragmentNameRef,
  )
}

private fun FulltextPluginParamsInput.toDto(): FulltextPluginParams {
  return FulltextPluginParams(
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

