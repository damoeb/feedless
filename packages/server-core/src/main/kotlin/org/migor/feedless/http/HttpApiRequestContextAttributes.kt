package org.migor.feedless.http

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.capability.RequestContext

/** Request attribute key for [RequestContext] established by [HttpApiJwtFilter]. */
const val HTTP_API_REQUEST_CONTEXT_ATTR = "org.migor.feedless.http.requestContext"

fun HttpServletRequest.httpApiRequestContext(): RequestContext? =
  getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
