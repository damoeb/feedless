package org.migor.feedless.http

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext

fun HttpServletRequest.httpApiRequestContext(): RequestContext? =
  getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
