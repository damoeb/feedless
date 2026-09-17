package org.migor.feedless.api

import org.migor.feedless.common.PublicUrls
import org.migor.feedless.document.DocumentId

fun createDocumentUrl(publicUrls: PublicUrls, id: DocumentId): String =
  "${publicUrls.apiGatewayUrl}/article/${id}"
