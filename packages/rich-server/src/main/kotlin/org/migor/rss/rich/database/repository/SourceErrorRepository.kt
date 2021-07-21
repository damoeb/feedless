package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.SourceError
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SourceErrorRepository : PagingAndSortingRepository<SourceError, String> {
  fun countBySourceIdAndCreatedAtAfterOrderByCreatedAtDesc(sourceId: String, minCreatedAt: Date): Int
  fun deleteAllBySourceIdAndCreatedAtBefore(sourceId: String, maxCreatedAt: Date)
}
