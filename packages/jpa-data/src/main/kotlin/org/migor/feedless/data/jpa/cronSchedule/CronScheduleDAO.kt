package org.migor.feedless.data.jpa.cronSchedule

import org.migor.feedless.AppLayer
import org.migor.feedless.report.ReportId
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppLayer.repository)
interface CronScheduleDAO : JpaRepository<CronScheduleEntity, UUID> {

  @Query(
    """SELECT r.cronSchedule FROM ReportEntity r
    WHERE r.id = :id"""
  )
  fun findByReportId(@Param("id") id: ReportId): CronScheduleEntity?
}
