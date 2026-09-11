package org.migor.feedless.http

import kotlinx.coroutines.Dispatchers
import org.springframework.core.CoroutinesUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import java.lang.reflect.Method

/**
 * Puts the [RequestContext] set by [HttpApiJwtFilter] into the coroutine context of suspend controllers.
 * Keeps the Unconfined start, so @PreAuthorize runs on the request thread while the security context is set.
 */
class HttpApiServletInvocableHandlerMethod(handlerMethod: HandlerMethod) :
  ServletInvocableHandlerMethod(handlerMethod) {

  override fun invokeSuspendingFunction(method: Method, target: Any, args: Array<Any>): Any {
    val requestContext = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
      ?.request
      ?.httpApiRequestContext()
      ?: return super.invokeSuspendingFunction(method, target, args)

    return CoroutinesUtils.invokeSuspendingFunction(Dispatchers.Unconfined + requestContext, method, target, *args)
  }
}
