package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.pipelineJob.MaxAgeDaysDateField
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.user.UserId
import java.time.LocalDateTime
import java.util.*

data class Repository(
    val id: RepositoryId = RepositoryId(),
    val title: String,
    val description: String = "",
    val tags: Array<String> = emptyArray(),
    val visibility: EntityVisibility = EntityVisibility.isPrivate,
    val sourcesSyncCron: String = "",
    val retentionMaxCapacity: Int? = null,
    val pushNotificationsEnabled: Boolean = false,
    val retentionMaxAgeDays: Int? = null,
    val retentionMaxAgeDaysReferenceField: MaxAgeDaysDateField,
    val lastUpdatedAt: LocalDateTime,
    val disabledFrom: LocalDateTime?? = null,
    val shareKey: String = UUID.randomUUID().toString(),
    val sunsetAfterTimestamp: LocalDateTime?? = null,
    val sunsetAfterTotalDocumentCount: Int?? = null,
    val documentCountSinceCreation: Int = 0,
    val archived: Boolean = false,
    val product: Vertical = Vertical.feedless,
    val triggerScheduledNextAt: LocalDateTime? = null,
    val schemaVersion: Int = 0,
    val pullsPerMonth: Int = 0,
    val lastPullSync: LocalDateTime? = null,
    val plugins: List<PluginExecution> = emptyList(),
    val ownerId: UserId,
    val groupId: GroupId? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
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

