package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.models.BucketEntity
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

  // see https://www.postgresql.org/docs/current/arrays.html#ARRAYS-SEARCHING
  @Query(
    """
    select * from t_bucket
    where ownerId = :ownerId
    and cardinality(:every\:\:text[]) = all (select count(*) from unnest(:every\:\:text[]) as dt(tag) where tag = ANY (tags))
    offset :offset rows limit :limit
  """,
    nativeQuery = true
  )
  fun findAllByOwnerIdAndEveryTags(@Param("ownerId") ownerId: UUID,
                                   @Param("every") everyTags: Array<String>,
                                   @Param("offset") offset: Int,
                                   @Param("limit") pageSize: Int): List<BucketEntity>

  @Query(
    """
    select * from t_bucket
    where ownerId = :ownerId
    and 0 < all (select count(*) from unnest(:some\:\:text[]) as dt(tag) where tag = ANY (tags))
    offset :offset rows limit :limit
  """,
    nativeQuery = true
  )
  fun findAllByOwnerIdAndSomeTags(@Param("ownerId") ownerId: UUID,
                       @Param("some") someTags: Array<String>,
                       @Param("offset") offset: Int,
                       @Param("limit") pageSize: Int): List<BucketEntity>

  @Query(
    """
    select B from BucketEntity B
    where B.visibility = ?1
  """
  )
  fun findAllPublic(visibilityPublic: EntityVisibility, pageable: PageRequest): List<BucketEntity>

  @Query(
    """
      select id, isBucket from (
      (
    select B.id as id, B.lastupdatedat as lastupdatedat, TRUE as isBucket from t_bucket as B
    where B.ownerId = ?1
    union
    select F.id as id, F.lastupdatedat as lastupdatedat, FALSE as isBucket from t_feed_native as F
    where F.ownerId = ?1 and not exists(select TRUE from t_importer I where I.feedid = F.id and I.ownerid = ?1)
    ) order by lastupdatedat offset ?2 rows limit ?3
    ) as bucketOrFeed
  """,
    nativeQuery = true
  )
  fun findAllMixed(ownerId: UUID, offset: Int, pageSize: Int): List<Array<Any>>

}
