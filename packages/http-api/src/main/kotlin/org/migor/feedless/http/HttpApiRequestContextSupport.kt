package org.migor.feedless.http

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

internal fun currentHttpApiRequestContext(): RequestContext? =
  (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
    ?.request
    ?.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext

internal fun <T> Flow<T>.withHttpApiRequestContext(): Flow<T> {
  val requestContext = currentHttpApiRequestContext() ?: return this
  return flowOn(requestContext)
}
