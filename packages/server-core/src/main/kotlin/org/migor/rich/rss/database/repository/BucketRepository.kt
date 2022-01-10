package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.BucketType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BucketRepository : CrudRepository<Bucket, String> {
  fun findFirstByTypeAndOwnerId(type: BucketType, ownerId: String): Bucket?

//  @Transactional(propagation = Propagation.REQUIRES_NEW)
//  @Modifying
//  @Query("update Bucket b set b.lastUpdatedAt = :lastUpdatedAt where b.id = :id")
//  fun setLastUpdatedAt(@Param("id") bucketId: String, @Param("lastUpdatedAt") lastUpdatedAt: Date)
}
