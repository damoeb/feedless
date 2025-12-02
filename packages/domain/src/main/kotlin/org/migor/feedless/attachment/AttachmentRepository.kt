package org.migor.feedless.attachment

interface AttachmentRepository {
  suspend fun findById(attachmentId: AttachmentId): Attachment?
  suspend fun save(attachment: Attachment): Attachment
  suspend fun deleteById(attachmentId: AttachmentId)
  suspend fun saveAll(attachments: List<Attachment>): List<Attachment>
}
