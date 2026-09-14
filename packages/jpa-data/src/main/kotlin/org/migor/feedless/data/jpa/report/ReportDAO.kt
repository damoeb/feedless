package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
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
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
interface ReportDAO : JpaRepository<ReportEntity, UUID> {
  /** Reports start authorized; false marks one switched off through updateReportById. */
  @Query(
    value = """
      select distinct r from ReportEntity r
      inner join r.cronSchedule as c
      join fetch r.segment
      join fetch r.cronSchedule
      where r.disabled = false
      and  r.authorized = true
      and  c.scheduledNextAt < :now
    """
  )
  fun findAllEnabledPendingBatched(@Param("now") now: LocalDateTime, pageable: PageRequest): List<ReportEntity>

  @Modifying
  @Query(
    value = """
      update ReportEntity r set r.disabled = true, r.disabledAt = :now
      where lower(trim(r.recipientEmail)) = :email
      and r.disabled = false
    """
  )
  fun disableAllByRecipientEmail(@Param("email") email: String, @Param("now") now: LocalDateTime): Int

}
