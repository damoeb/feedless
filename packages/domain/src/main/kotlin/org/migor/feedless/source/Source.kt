package org.migor.feedless.source

import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.geo.LatLonPoint
import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

data class Source(
    val id: SourceId = SourceId(),
    val language: String? = null,
    val title: String,
    val latLon: LatLonPoint? = null,
    val tags: Array<String>? = null,
    val repositoryId: RepositoryId? = null,
    val disabled: Boolean = false,
    val lastRecordsRetrieved: Int = 0,
    val lastRefreshedAt: LocalDateTime? = null,
    val errorsInSuccession: Int = 0,
    val lastErrorMessage: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val actions: List<ScrapeAction> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Source

        if (id != other.id) return false
        if (title != other.title) return false
        if (!tags.contentEquals(other.tags)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (tags?.contentHashCode() ?: 0)
        return result
    }
}

