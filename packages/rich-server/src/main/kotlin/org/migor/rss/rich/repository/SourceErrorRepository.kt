package org.migor.rss.rich.repository

import org.migor.rss.rich.model.SourceError
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SourceErrorRepository : PagingAndSortingRepository<SourceError, String> {
  fun countBySourceIdAndCreatedAtAfterOrderByCreatedAtDesc(sourceId: String, minCreatedAt: Date): Int
  fun deleteAllBySourceIdAndCreatedAtBefore(sourceId: String, maxCreatedAt: Date)
  fun findAllBySourceId(sourceId: String, pageable: Pageable): List<SourceError>
}
