package org.migor.feedless.source

import org.locationtech.jts.geom.Point
import org.migor.feedless.actions.ScrapeAction
import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

data class Source(
  val id: SourceId,
  val language: String?,
  val title: String,
  val latLon: Point?,
  val tags: Array<String>?,
  val repositoryId: RepositoryId,
  val disabled: Boolean,
  val lastRecordsRetrieved: Int,
  val lastRefreshedAt: LocalDateTime?,
  val errorsInSuccession: Int,
  val lastErrorMessage: String?,
  val createdAt: LocalDateTime,
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

