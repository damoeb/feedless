package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.BucketEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BucketDAO : CrudRepository<BucketEntity, UUID> {
  @Query("""
    select B from StreamEntity S
    inner join BucketEntity B on S.id = B.streamId
  """)
  fun findByStreamId(@Param("streamId") streamId: UUID): BucketEntity

  @Query("""
    select B from BucketEntity B
  """)
  fun findAllMatching(query: String, pageable: PageRequest): Page<BucketEntity>
}
