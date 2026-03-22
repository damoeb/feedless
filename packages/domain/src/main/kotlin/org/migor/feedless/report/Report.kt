package org.migor.feedless.report

import org.migor.feedless.cronSchedule.CronSchedule
import org.migor.feedless.cronSchedule.CronScheduleId
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Report(
  val id: ReportId = ReportId(),
  val recipientEmail: String,
  val recipientName: String,
  val authorized: Boolean = false,
  val authorizationAttempt: Int = 0,
  val reporterPlugin: PluginExecution,
  val lastRequestedAuthorization: LocalDateTime? = null,
  val authorizedAt: LocalDateTime? = null,
  val disabled: Boolean = false,
  val disabledAt: LocalDateTime? = null,
  val segmentId: SegmentationId,
  val segment: Segmentation,
  val userId: UserId? = null,
  val cronScheduleId: CronScheduleId,
  val cronSchedule: CronSchedule,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

