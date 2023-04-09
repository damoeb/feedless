package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.enums.EntityVisibility
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BucketDAO : JpaRepository<BucketEntity, UUID> {
  @Query(
    """
    select B from BucketEntity B where B.streamId = :streamId
  """
  )
  fun findByStreamId(@Param("streamId") streamId: UUID): Optional<BucketEntity>

  @Query(
    """
    select B from BucketEntity B
    where (cast(?1 as uuid) is null and B.visibility = ?2)
    or (B.ownerId = ?1 or B.visibility = ?2)
  """
  )
  fun findAllMatching(ownerId: UUID?, visibilityPublic: EntityVisibility, pageable: PageRequest): List<BucketEntity>
}
