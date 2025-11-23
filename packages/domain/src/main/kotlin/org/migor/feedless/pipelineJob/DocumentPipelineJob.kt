package org.migor.feedless.pipelineJob

import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

data class DocumentPipelineJob(
    override val id: PipelineJobId = PipelineJobId(),
    override val sequenceId: Int,
    override val attempt: Int = 1,
    override val terminatedAt: LocalDateTime? = null,
    override val terminated: Boolean = false,
    override val coolDownUntil: LocalDateTime? = null,
    override val status: PipelineJobStatus = PipelineJobStatus.PENDING,
    override val logs: String? = null,
    val pluginId: String,
    val executorParams: PluginExecutionJson,
    val documentId: DocumentId,
) : PipelineJob(id, sequenceId, attempt, terminatedAt, terminated, coolDownUntil, status, logs) {
    fun updateStatus(status: PipelineJobStatus): DocumentPipelineJob {
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

