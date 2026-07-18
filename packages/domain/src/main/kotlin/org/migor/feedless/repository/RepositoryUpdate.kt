package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class RepositoryUpdate(
  val title: String? = null,
  val description: String? = null,
  val refreshCron: String? = null,
  val pushNotificationsEnabled: Boolean? = null,
  val visibility: EntityVisibility? = null,
  val plugins: List<PluginExecution>? = null,
  val nextUpdateAt: LocalDateTime? = null,
  val scheduleNextUpdateNow: Boolean = false,
  val retentionMaxCapacity: Int? = null,
  val clearRetentionMaxCapacity: Boolean = false,
  val retentionMaxAgeDays: Int? = null,
  val clearRetentionMaxAgeDays: Boolean = false,
  val retentionMaxAgeDaysReferenceField: MaxAgeDaysDateField? = null,
  val sources: RepositorySourcesUpdate? = null,
)

data class RepositorySourcesUpdate(
  val add: List<Source>? = null,
  val update: List<RepositorySourceUpdate>? = null,
  val remove: List<SourceId>? = null,
)

data class RepositorySourceUpdate(
  val sourceId: SourceId,
  val title: String? = null,
  val tags: List<String>? = null,
  val disabled: Boolean? = null,
  val latLng: LatLonPoint? = null,
  val clearLatLng: Boolean = false,
  val actions: List<ScrapeAction>? = null,
  val clearActions: Boolean = false,
)
