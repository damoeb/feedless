package org.migor.feedless.report

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Report(
  val id: ReportId,
  val recipientEmail: String,
  val recipientName: String,
  val authorized: Boolean,
  val authorizationAttempt: Int,
  val lastRequestedAuthorization: LocalDateTime?,
  val authorizedAt: LocalDateTime?,
  val lastReportedAt: LocalDateTime?,
  val disabled: Boolean,
  val disabledAt: LocalDateTime?,
  val nextReportedAt: LocalDateTime,
  val segmentId: SegmentationId,
  val userId: UserId?,
  val createdAt: LocalDateTime
)

