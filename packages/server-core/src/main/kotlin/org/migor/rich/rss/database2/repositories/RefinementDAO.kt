package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.RefinementEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RefinementDAO : CrudRepository<RefinementEntity, UUID> {
  @Query(
    """select pp from RefinementEntity pp
    inner join Bucket2RefinementEntity pp2b on pp2b.refinementId = pp.id
    where pp2b.bucketId = :bucketId"""
  )
  fun findAllByBucketId(@Param("bucketId") bucketId: UUID): List<RefinementEntity>
}
