package org.migor.feedless.data.jpa.document

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.ReleaseStatus
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.document} & ${AppLayer.repository}")
interface DocumentDAO : JpaRepository<DocumentEntity, UUID>, KotlinJdslJpqlExecutor {

  @Modifying
  @Query(
    """
    DELETE FROM DocumentEntity d
    WHERE d.id in (
        select d1.id from DocumentEntity d1
        where d1.repositoryId = ?1
        and d1.status = ?2
        order by d1.startingAt desc nulls last, d1.publishedAt desc nulls last
        offset ?3 ROWS
    )
    """
  )
  fun deleteAllByRepositoryIdAndStatusWithSkip(repositoryId: UUID, status: ReleaseStatus, skip: Int)

  fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(
    repositoryId: UUID,
    date: LocalDateTime,
    status: ReleaseStatus
  )

  fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(id: UUID, maxDate: LocalDateTime, released: ReleaseStatus)
  fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(id: UUID, maxDate: LocalDateTime, released: ReleaseStatus)

  fun findByTitleInAndRepositoryId(titles: List<String>, repositoryId: UUID): DocumentEntity?

  fun countByRepositoryId(id: UUID): Long
  fun findAllByRepositoryId(id: UUID): List<DocumentEntity>

  fun findAllByRepositoryIdAndIdIn(repositoryId: UUID, ids: List<UUID>): List<DocumentEntity>
  fun findAllBySourceId(sourceId: UUID, pageable: PageRequest): List<DocumentEntity>


  @Query(
    """SELECT DISTINCT s FROM DocumentEntity s
    LEFT JOIN FETCH s.source
    LEFT JOIN FETCH s.source.actions
    WHERE s.id = :id"""
  )
  fun findByIdWithSource(@Param("id") documentId: UUID): DocumentEntity?
  fun countBySourceId(sourceId: UUID): Int


  @Query(
    """SELECT d FROM DocumentEntity d
    WHERE d.repositoryId = :repositoryId
    AND (
        (d.contentHash != '' AND d.contentHash = :contentHash)
        OR
        (d.contentHash = '' AND d.url = :url)
    )"""
  )
  fun findFirstByContentHashOrUrlAndRepositoryId(
    @Param("contentHash") contentHash: String,
    @Param("url") url: String,
    @Param("repositoryId") repositoryId: UUID
  ): DocumentEntity?


  @Query(
    """SELECT d FROM DocumentEntity d
       LEFT JOIN FETCH d.attachments
    WHERE d.id in (:ids)"""
  )
  fun findAllWithAttachmentsByIdIn(ids: List<UUID>): List<DocumentEntity>

}
