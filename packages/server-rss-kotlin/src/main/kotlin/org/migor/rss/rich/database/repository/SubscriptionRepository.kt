package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Subscription
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

@Repository
interface SubscriptionRepository : CrudRepository<Subscription, String> {

  @Query(
    """select distinct s from Subscription s
    inner join Feed f on s.feedId = f.id
    inner join Bucket b on b.id = s.bucketId
    inner join Exporter e on e.bucketId = b.id
    where (s.lastUpdatedAt is null or e.lastUpdatedAt is null or f.lastUpdatedAt > e.lastUpdatedAt)
        and e.id = :exporterId
    order by s.lastUpdatedAt asc """
  )
  fun findAllChangedSince(@Param("exporterId") exporterId: String): Stream<Subscription>

  @Query(
    """select distinct s from Subscription s
    where s.bucketId=:bucketId"""
  )
  fun findAllByBucketId(@Param("bucketId") bucketId: String): List<Subscription>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Modifying
  @Query(
    "update Subscription s " +
      "set s.lastUpdatedAt = :lastUpdatedAt " +
      "where s.bucketId = :bucketId"
  )
  fun setLastUpdatedAtByBucketId(@Param("bucketId") bucketId: String, @Param("lastUpdatedAt") lastUpdatedAt: Date)
}
