package org.migor.feedless.pipelineJob

import java.time.LocalDateTime
import java.util.*

sealed class PipelineJob(
    open val id: PipelineJobId = PipelineJobId(UUID.randomUUID()),
    open val sequenceId: Int,
    open val attempt: Int,
    open val terminatedAt: LocalDateTime? = null,
    open val terminated: Boolean = false,
    open val coolDownUntil: LocalDateTime? = null,
    open val status: PipelineJobStatus,
    open val logs: String? = null,
    open val createdAt: LocalDateTime = LocalDateTime.now(),
)
