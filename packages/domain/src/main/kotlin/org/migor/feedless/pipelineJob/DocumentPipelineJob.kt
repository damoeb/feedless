package org.migor.feedless.pipelineJob

import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

data class DocumentPipelineJob(
  val id: PipelineJobId,
  val sequenceId: Int,
  val attempt: Int,
  val terminatedAt: LocalDateTime?,
  val terminated: Boolean,
  val coolDownUntil: LocalDateTime?,
  val status: PipelineJobStatus,
  val logs: String?,
  val pluginId: String,
  val executorParams: PluginExecutionJson,
  val documentId: DocumentId,
  val createdAt: LocalDateTime
)

