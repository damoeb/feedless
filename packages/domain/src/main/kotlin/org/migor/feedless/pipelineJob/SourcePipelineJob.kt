package org.migor.feedless.pipelineJob

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class SourcePipelineJob(
  val id: PipelineJobId,
  val sequenceId: Int,
  val attempt: Int,
  val terminatedAt: LocalDateTime?,
  val terminated: Boolean,
  val coolDownUntil: LocalDateTime?,
  val status: PipelineJobStatus,
  val logs: String?,
  val url: String,
  val sourceId: SourceId,
  val createdAt: LocalDateTime
)

