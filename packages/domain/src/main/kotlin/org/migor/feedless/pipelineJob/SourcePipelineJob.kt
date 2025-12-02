package org.migor.feedless.pipelineJob

import org.migor.feedless.source.SourceId
import java.time.LocalDateTime

data class SourcePipelineJob(
  override val id: PipelineJobId = PipelineJobId(),
  override val sequenceId: Int,
  override val attempt: Int = 0,
  override val terminatedAt: LocalDateTime? = null,
  override val terminated: Boolean = false,
  override val coolDownUntil: LocalDateTime? = null,
  override val status: PipelineJobStatus = PipelineJobStatus.PENDING,
  override val logs: String? = null,
  val url: String,
  val sourceId: SourceId,
) : PipelineJob(id, sequenceId, attempt, terminatedAt, terminated, coolDownUntil, status, logs) {
  fun updateStatus(status: PipelineJobStatus): SourcePipelineJob {
    return if (status == PipelineJobStatus.PENDING) {
      copy(
        terminatedAt = null,
        terminated = false,
        status = status,
      )
    } else {
      copy(
        terminatedAt = LocalDateTime.now(),
        terminated = true,
        status = status
      )
    }
  }
}

