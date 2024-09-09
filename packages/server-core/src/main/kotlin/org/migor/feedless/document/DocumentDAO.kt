package org.migor.feedless.document

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
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
@Profile(AppProfiles.database)
interface DocumentDAO : JpaRepository<DocumentEntity, UUID>, KotlinJdslJpqlExecutor {

  @Modifying(clearAutomatically = true)
  @Query(
    """
    DELETE FROM DocumentEntity d
    WHERE d.id in (
        select d1.id from DocumentEntity d1
        where d1.repositoryId = ?1
        and d1.status = ?2
        order by d1.startingAt asc, d1.publishedAt desc
        offset ?3 ROWS
    )
    """
  )
  fun deleteAllByRepositoryIdAndStatusWithSkip(repositoryId: UUID, status: ReleaseStatus, skip: Int)

  fun deleteAllByRepositoryIdAndPublishedAtBeforeAndStatus(repositoryId: UUID, date: LocalDateTime, status: ReleaseStatus)

  fun deleteAllByRepositoryIdAndStartingAtBeforeAndStatus(id: UUID, maxDate: LocalDateTime, released: ReleaseStatus)

  fun findFirstByUrlAndRepositoryId(url: String, repositoryId: UUID): DocumentEntity?

  fun findByContentTitleAndRepositoryId(title: String, repositoryId: UUID): DocumentEntity?

  fun countByRepositoryId(id: UUID): Long

  fun deleteAllByRepositoryIdAndIdIn(repositoryId: UUID, ids: List<UUID>)
  fun deleteAllByRepositoryIdAndId(repositoryId: UUID, fromString: UUID?)
  fun findAllBySourceId(sourceId: UUID, pageable: PageRequest): List<DocumentEntity>


  @Query(
    """SELECT DISTINCT s FROM DocumentEntity s
    LEFT JOIN FETCH s.source
    LEFT JOIN FETCH s.source.actions
    WHERE s.id = :id"""
  )
  fun findByIdWithSource(@Param("id") documentId: UUID): DocumentEntity?

}
