package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.report} & ${AppLayer.repository}")
interface ReportDAO : JpaRepository<ReportEntity, UUID> {
  @Query(
    value = """
      select distinct r from ReportEntity r
      inner join r.cronSchedule as c
      inner join fetch r.segment
      inner join fetch r.cronSchedule
      inner join fetch r.reporterPlugin
      where r.disabled = false
      and c.scheduledNextAt < :now
    """
  )
  fun findAllEnabledPendingBatched(@Param("now") now: LocalDateTime, pageable: PageRequest): List<ReportEntity>

}
