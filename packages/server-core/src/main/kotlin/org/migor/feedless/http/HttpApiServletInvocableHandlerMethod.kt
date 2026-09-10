package org.migor.feedless.http

import kotlinx.coroutines.Dispatchers
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
 *
 * It keeps Spring's [Dispatchers.Unconfined] start: the handler proxy — and its `@PreAuthorize` — runs
 * on the request thread while the security filter chain's context is still set. A context without a
 * dispatcher would start the coroutine on [Dispatchers.Default], where method security sees no
 * authentication at all.
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
