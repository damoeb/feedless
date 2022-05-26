package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.Subscription
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile("stateful")
interface SubscriptionRepository : CrudRepository<Subscription, String> {

  @Query(
    """select distinct s from Subscription s
    inner join Feed f on s.feedId = f.id
    inner join Bucket b on b.id = s.bucketId
    inner join Exporter e on e.bucketId = b.id
    where e.id = :exporterId
    order by s.lastUpdatedAt asc """
  )
  fun findAllByExporterId(@Param("exporterId") exporterId: String): List<Subscription>

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
