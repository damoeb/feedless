package org.migor.feedless.document

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface DocumentDAO : JpaRepository<DocumentEntity, UUID>, KotlinJdslJpqlExecutor {

  @Modifying
  @Query(
    """
    DELETE FROM DocumentEntity d
    WHERE d.id in (
        select d1.id from DocumentEntity d1
        where d1.repositoryId = ?1
        and d1.status = ?2
        order by d1.publishedAt desc
        offset ?3 ROWS
    )
    """
  )
  fun deleteAllByRepositoryIdAndStatusWithSkip(repositoryId: UUID, status: ReleaseStatus, skip: Int)

  fun deleteAllByRepositoryIdAndCreatedAtBeforeAndStatus(repositoryId: UUID, date: Date, status: ReleaseStatus)

  @Modifying
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  override fun deleteById(id: UUID)

  @Query(
    """
    SELECT D FROM DocumentEntity D
    WHERE D.url = :url and D.repositoryId = :repositoryId
    """
  )
  fun findByUrlAndRepositoryId(
    @Param("url") url: String,
    @Param("repositoryId") repositoryId: UUID
  ): DocumentEntity?


  fun existsByContentTitleAndRepositoryId(title: String, repositoryId: UUID): Boolean
  fun findByContentTitleAndRepositoryId(title: String, repositoryId: UUID): DocumentEntity?

  fun countByRepositoryId(id: UUID): Long

  @Query(
    """
    SELECT date_part('year', released_at\:\:date) as year,
           date_part('month', released_at\:\:date) AS month,
           date_part('day', released_at\:\:date) AS day,
           COUNT(id)
    FROM t_document
    WHERE released_at >= date_trunc('month', current_date - interval '1' month)
       and repository_id = ?1
    GROUP BY year, month, day
    ORDER BY year, month, day
    """,
    nativeQuery = true
  )
  fun histogramPerDayByStreamIdOrImporterId(streamId: UUID): List<Array<Any>>
  fun deleteAllByRepositoryIdAndIdIn(repositoryId: UUID, ids: List<UUID>)
  fun deleteAllByRepositoryIdAndIdNotIn(repositoryId: UUID, ids: List<UUID>)
  fun deleteAllByRepositoryIdAndId(repositoryId: UUID, fromString: UUID?)

}
