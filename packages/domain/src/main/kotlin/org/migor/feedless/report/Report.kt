package org.migor.feedless.report

import org.migor.feedless.user.UserId
import java.time.LocalDateTime

data class Report(
  val id: ReportId = ReportId(),
  val recipientEmail: String,
  val recipientName: String,
  val authorized: Boolean = false,
  val authorizationAttempt: Int = 0,
  val lastRequestedAuthorization: LocalDateTime? = null,
  val authorizedAt: LocalDateTime? = null,
  val lastReportedAt: LocalDateTime? = null,
  val disabled: Boolean = false,
  val disabledAt: LocalDateTime? = null,
  val nextReportedAt: LocalDateTime,
  val segmentId: SegmentationId,
  val userId: UserId? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

