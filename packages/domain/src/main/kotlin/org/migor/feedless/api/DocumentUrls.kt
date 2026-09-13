package org.migor.feedless.api

import org.migor.feedless.common.AppConfig
import org.migor.feedless.document.DocumentId

fun createDocumentUrl(appConfig: AppConfig, id: DocumentId): String =
  "${appConfig.apiGatewayUrl}/article/${id}"
