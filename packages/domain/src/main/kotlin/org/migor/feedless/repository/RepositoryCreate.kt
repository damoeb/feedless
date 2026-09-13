package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.source.Source

data class RepositoryCreate(
  val product: Vertical,
  val title: String,
  val description: String = "",
  val sources: List<Source> = emptyList(),
  val refreshCron: String? = null,
  val visibility: EntityVisibility? = null,
  val pushNotificationsEnabled: Boolean = false,
  val retention: RepositoryRetention? = null,
  val plugins: List<PluginExecution>? = null,
)

data class RepositoryRetention(
  val maxCapacity: Int? = null,
  val maxAgeDays: Int? = null,
)
