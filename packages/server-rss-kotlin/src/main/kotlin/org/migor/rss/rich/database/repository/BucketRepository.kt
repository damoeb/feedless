package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Bucket
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface BucketRepository : CrudRepository<Bucket, String> {
  @Query("""select distinct b from Bucket b
    inner join ArticlePostProcessorToBucket pp2b on pp2b.id.bucketId = b.id
    order by b.lastPostProcessedAt asc""")
  fun findDueToPostProcessors(pageable: PageRequest): Iterable<Bucket>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Bucket b set b.lastPostProcessedAt = :now where b.id = :bucketId")
  fun updateLastPostProcessedAt(@Param("bucketId") bucketId: String, @Param("now") date: Date)

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query("update Bucket b set b.lastUpdatedAt = :lastUpdatedAt where b.id = :id")
  fun setLastUpdatedAt(@Param("id") bucketId: String, @Param("lastUpdatedAt") lastUpdatedAt: Date)

}
