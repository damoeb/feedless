package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Repository(
  val id: RepositoryId,
  val title: String,
  val description: String,
  val tags: Array<String>,
  val visibility: EntityVisibility,
  val sourcesSyncCron: String,
  val retentionMaxCapacity: Int?,
  val pushNotificationsEnabled: Boolean,
  val retentionMaxAgeDays: Int?,
  val retentionMaxAgeDaysReferenceField: MaxAgeDaysDateField,
  val lastUpdatedAt: LocalDateTime,
  val disabledFrom: LocalDateTime?,
  val shareKey: String,
  val sunsetAfterTimestamp: LocalDateTime?,
  val sunsetAfterTotalDocumentCount: Int?,
  val documentCountSinceCreation: Int,
  val archived: Boolean,
  val product: Vertical,
  val triggerScheduledNextAt: LocalDateTime?,
  val schemaVersion: Int,
  val pullsPerMonth: Int,
  val lastPullSync: LocalDateTime?,
  val plugins: List<PluginExecution>,
  val ownerId: UserId,
  val groupId: GroupId?,
  val createdAt: LocalDateTime
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Repository

    if (id != other.id) return false
    if (title != other.title) return false
    if (description != other.description) return false
    if (!tags.contentEquals(other.tags)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = id.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + description.hashCode()
    result = 31 * result + tags.contentHashCode()
    return result
  }
}

