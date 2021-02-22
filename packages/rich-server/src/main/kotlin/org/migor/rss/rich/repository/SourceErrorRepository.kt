package org.migor.rss.rich.repository

import org.migor.rss.rich.model.SourceError
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SourceErrorRepository : CrudRepository<SourceError, String> {

  fun countBySourceIdAndCreatedAtAfterOrderByCreatedAtDesc(sourceId: String, minCreatedAt: Date): Int
  fun deleteAllBySourceIdAndCreatedAtBefore(sourceId: String, maxCreatedAt: Date)
}
