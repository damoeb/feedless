package org.migor.feedless.pipeline.plugins

import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.common.PublicUrls

fun createAttachmentUrl(publicUrls: PublicUrls, id: AttachmentId): String =
  "${publicUrls.apiGatewayUrl}/attachment/${id.uuid}"
