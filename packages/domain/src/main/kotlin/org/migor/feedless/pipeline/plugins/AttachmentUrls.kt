package org.migor.feedless.pipeline.plugins

import org.migor.feedless.attachment.AttachmentId
import org.migor.feedless.common.AppConfig

fun createAttachmentUrl(appConfig: AppConfig, id: AttachmentId): String =
  "${appConfig.apiGatewayUrl}/attachment/${id.uuid}"
