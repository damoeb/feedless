package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface WebDocumentDAO : JpaRepository<WebDocumentEntity, UUID>, PagingAndSortingRepository<WebDocumentEntity, UUID> {

  fun findAllBySubscriptionIdAndStatusAndReleasedAtBefore(
    subscriptionId: UUID,
    status: ReleaseStatus,
    now: Date,
    pageable: PageRequest
  ): List<WebDocumentEntity>


  @Modifying
  @Query(
    """
    DELETE FROM WebDocumentEntity d
    WHERE d.id in (
        select d1.id from WebDocumentEntity d1
        where d1.subscriptionId = ?1
        and d1.status = ?2
        order by d1.releasedAt desc
        offset ?3 ROWS
    )
    """
  )
  fun deleteAllBySubscriptionIdAndStatusWithSkip(subscriptionId: UUID, status: ReleaseStatus, skip: Int)

  fun deleteAllBySubscriptionIdAndCreatedAtBeforeAndStatus(subscriptionId: UUID, date: Date, status: ReleaseStatus)

  @Modifying
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  override fun deleteById(id: UUID)

  @Query(
    """
    SELECT D FROM WebDocumentEntity D
    WHERE D.url = :url and D.subscriptionId = :subscriptionId
    """
  )
  fun findByUrlAndSubscriptionId(
    @Param("url") url: String,
    @Param("subscriptionId") subscriptionId: UUID
  ): WebDocumentEntity?


  fun existsByContentTitleAndSubscriptionId(title: String, subscriptionId: UUID): Boolean

  @Modifying
  @Query(
    """
    DELETE FROM WebDocumentEntity d
    WHERE d.id = ?1 and d.id in (
        select d1.id from WebDocumentEntity d1
        inner join SourceSubscriptionEntity s
        where d1.id = ?1
        and s.ownerId = ?2
    )
    """
  )
  fun deleteByIdAndOwnerId(id: UUID, ownerId: UUID)

  fun countBySubscriptionId(id: UUID): Long

  @Query(
    """
    SELECT date_part('year', releasedat\:\:date) as year,
           date_part('month', releasedat\:\:date) AS month,
           date_part('day', releasedat\:\:date) AS day,
           COUNT(id)
    FROM t_web_document
    WHERE releasedat >= date_trunc('month', current_date - interval '1' month)
       and subscriptionid = ?1
    GROUP BY year, month, day
    ORDER BY year, month, day
    """,
    nativeQuery = true
  )
  fun histogramPerDayByStreamIdOrImporterId(streamId: UUID): List<Array<Any>>

}
