package org.migor.feedless.attachment

import org.migor.feedless.document.DocumentId
import java.time.LocalDateTime

data class Attachment(
  val id: AttachmentId,
  val hasData: Boolean,
  val remoteDataUrl: String?,
  val mimeType: String,
  val originalUrl: String?,
  val name: String?,
  val size: Long?,
  val duration: Long?,
  val documentId: DocumentId,
  val createdAt: LocalDateTime
)
