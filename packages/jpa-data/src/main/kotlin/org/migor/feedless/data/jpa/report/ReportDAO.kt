package org.migor.feedless.data.jpa.report

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
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
    nativeQuery = true,
    value = """
      select p.* from t_report p
      inner join t_cron_schedule c
      on c.id = p.cron_schedule_id
      where p.is_disabled = false
      and trigger_scheduled_next_at < :now
      limit 100
    """
  )
  fun findAllPendingBatched(@Param("now") now: LocalDateTime): List<ReportEntity>

}
