package org.migor.feedless.api.mapper

import com.google.gson.Gson
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.api.fromDto
import org.migor.feedless.api.mapper.fromDto as scrapeFlowFromDto
import org.migor.feedless.generated.types.BoolUpdateOperationsInput
import org.migor.feedless.generated.types.NullableIntUpdateOperationsInput
import org.migor.feedless.generated.types.NullableLongUpdateOperationsInput
import org.migor.feedless.generated.types.NullableStringUpdateOperationsInput
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RecordDateFieldUpdateOperationsInput
import org.migor.feedless.generated.types.RepositoryCreateInput
import org.migor.feedless.generated.types.RepositoryUpdateDataInput
import org.migor.feedless.generated.types.RetentionInput
import org.migor.feedless.generated.types.RetentionUpdateInput
import org.migor.feedless.generated.types.SourcesUpdateInput
import org.migor.feedless.generated.types.StringUpdateOperationsInput
import org.migor.feedless.generated.types.VisibilityUpdateOperationsInput
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.repository.RepositoryCreate
import org.migor.feedless.repository.RepositoryRetention
import org.migor.feedless.repository.RepositorySourceUpdate
import org.migor.feedless.repository.RepositorySourcesUpdate
import org.migor.feedless.repository.RepositoryUpdate
import org.migor.feedless.source.SourceId
import org.migor.feedless.util.toLocalDateTime

fun RepositoryCreateInput.toDomain(): RepositoryCreate {
  return RepositoryCreate(
    product = product.fromDto(),
    title = title,
    description = description,
    sources = sources.map { it.toSource() },
    refreshCron = refreshCron,
    visibility = visibility?.fromDto(),
    pushNotificationsEnabled = pushNotificationsMuted == true,
    retention = retention?.toDomain(),
    plugins = plugins?.map { PluginExecution(id = it.pluginId, params = it.params.toParams()) },
  )
}

fun RetentionInput.toDomain(): RepositoryRetention {
  return RepositoryRetention(
    maxCapacity = maxCapacity,
    maxAgeDays = maxAgeDays,
  )
}

fun RepositoryUpdateDataInput.toDomain(): RepositoryUpdate {
  return RepositoryUpdate(
    title = title?.set,
    description = description?.set,
    refreshCron = refreshCron?.set,
    pushNotificationsEnabled = pushNotificationsMuted?.set,
    visibility = visibility?.set?.fromDto(),
    plugins = plugins?.map { PluginExecution(id = it.pluginId, params = it.params.toParams()) },
    nextUpdateAt = nextUpdateAt?.set?.toLocalDateTime(),
    retentionMaxCapacity = retention?.maxCapacity?.set,
    retentionMaxAgeDays = retention?.maxAgeDays?.set,
    retentionMaxAgeDaysReferenceField = retention?.ageReferenceField?.set?.fromDto(),
    sources = sources?.toDomain(),
  )
}

fun SourcesUpdateInput.toDomain(): RepositorySourcesUpdate {
  return RepositorySourcesUpdate(
    add = add?.map { it.toSource() },
    update = update?.map { sourceUpdate ->
      RepositorySourceUpdate(
        sourceId = SourceId(sourceUpdate.where.id),
        title = sourceUpdate.data.title?.set,
        tags = sourceUpdate.data.tags?.set,
        disabled = sourceUpdate.data.disabled?.set,
        latLng = sourceUpdate.data.latLng?.set?.let { LatLonPoint(it.lat, it.lng) },
        clearLatLng = sourceUpdate.data.latLng != null && sourceUpdate.data.latLng?.set == null,
        actions = sourceUpdate.data.flow?.set?.scrapeFlowFromDto(),
        clearActions = sourceUpdate.data.flow != null && sourceUpdate.data.flow?.set == null,
      )
    },
    remove = remove?.map { SourceId(it) },
  )
}

fun PluginExecutionInput.fromDto(): PluginExecution {
  return PluginExecution(id = pluginId, params = params.toParams())
}

fun PluginExecutionParamsInput.toParams(): PluginExecutionJson {
  val data = listOfNotNull(
    org_feedless_filter,
    org_feedless_feed,
    org_feedless_diff_records,
    jsonData,
    org_feedless_conditional_tag,
    org_feedless_fulltext,
  )
    .firstOrNull()

  return PluginExecutionJson(
    paramsJsonString = data?.let { Gson().toJson(it) },
  )
}
