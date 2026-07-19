package org.migor.feedless.http

import org.springframework.core.CoroutinesUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import java.lang.reflect.Method

/**
 * HTTP-edge bridge: propagates [RequestContext] from the servlet request (set by [HttpApiJwtFilter])
 * into the coroutine context for suspend MVC controller methods.
 *
 * Spring's default [ServletInvocableHandlerMethod] invokes suspend functions with an empty context;
 * this subclass mirrors GraphQL's `injectCapabilitiesFromSecurityContext()` at the MVC adapter edge.
 */
class HttpApiServletInvocableHandlerMethod(handlerMethod: HandlerMethod) :
  ServletInvocableHandlerMethod(handlerMethod) {

  override fun invokeSuspendingFunction(method: Method, target: Any, args: Array<Any>): Any {
    val requestContext = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
      ?.request
      ?.httpApiRequestContext()
      ?: return super.invokeSuspendingFunction(method, target, args)

    val result = CoroutinesUtils.invokeSuspendingFunction(requestContext, method, target, *args)
    return result
  }
}
