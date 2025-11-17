package org.migor.feedless.pipelineJob

import java.time.LocalDateTime

data class PipelineJob(
  val id: PipelineJobId,
  val sequenceId: Int,
  val attempt: Int,
  val terminatedAt: LocalDateTime?,
  val terminated: Boolean,
  val coolDownUntil: LocalDateTime?,
  val status: PipelineJobStatus,
  val logs: String?,
  val createdAt: LocalDateTime
)

