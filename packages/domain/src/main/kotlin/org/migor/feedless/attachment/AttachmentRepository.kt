package org.migor.feedless.attachment

interface AttachmentRepository {
  fun findById(attachmentId: AttachmentId): Attachment?
  fun save(attachment: Attachment): Attachment
  fun deleteById(attachmentId: AttachmentId)
  fun saveAll(attachments: List<Attachment>): List<Attachment>
}
