package org.migor.feedless.attachment


data class Attachment(
  val id: AttachmentId,
  val name: String,
  val mimeType: String? = null,
  val data: ByteArray? = null,
)
