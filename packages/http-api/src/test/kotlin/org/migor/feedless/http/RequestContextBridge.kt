package org.migor.feedless.http

import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.user.UserId
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.CoroutinesUtils
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import java.lang.reflect.Method

/**
 * Test double for server-core's `HttpApiServletInvocableHandlerMethod`: carries the
 * [RequestContext] from the servlet request into the coroutine context of a suspend
 * controller method. Without it a WebMvcTest runs every call anonymously, and the
 * access guard would deny it.
 */
@TestConfiguration
class RequestContextBridge {

  @Bean
  fun requestContextBridgeRegistrations(): WebMvcRegistrations =
    object : WebMvcRegistrations {
      override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter =
        object : RequestMappingHandlerAdapter() {
          override fun createInvocableHandlerMethod(handlerMethod: HandlerMethod): ServletInvocableHandlerMethod =
            object : ServletInvocableHandlerMethod(handlerMethod) {
              override fun invokeSuspendingFunction(method: Method, target: Any, args: Array<Any>): Any {
                val requestContext = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
                  ?.request
                  ?.getAttribute(HTTP_API_REQUEST_CONTEXT_ATTR) as? RequestContext
                  ?: return super.invokeSuspendingFunction(method, target, args)
                return CoroutinesUtils.invokeSuspendingFunction(requestContext, method, target, *args)
              }
            }
        }
    }
}

// Each call is sent as `userId`, the way HttpApiJwtFilter does for a Bearer token, and
// returns the final response (suspend controllers answer through an async dispatch).

fun MockMvc.getAs(userId: UserId, url: String): MvcResult =
  complete(get(url) { requestAttr(HTTP_API_REQUEST_CONTEXT_ATTR, RequestContext(userId = userId)) }.andReturn())

/** No [RequestContext] attribute at all — an anonymous caller, the same as a missing Bearer token. */
fun MockMvc.getAnonymous(url: String): MvcResult =
  complete(get(url).andReturn())

fun MockMvc.postAs(userId: UserId, url: String, json: String): MvcResult =
  complete(
    post(url) {
      requestAttr(HTTP_API_REQUEST_CONTEXT_ATTR, RequestContext(userId = userId))
      contentType = MediaType.APPLICATION_JSON
      content = json
    }.andReturn(),
  )

fun MockMvc.patchAs(userId: UserId, url: String, json: String, headers: Map<String, String> = emptyMap()): MvcResult =
  complete(
    patch(url) {
      requestAttr(HTTP_API_REQUEST_CONTEXT_ATTR, RequestContext(userId = userId))
      contentType = MediaType.APPLICATION_JSON
      content = json
      headers.forEach { (name, value) -> header(name, value) }
    }.andReturn(),
  )

fun MockMvc.deleteAs(userId: UserId, url: String): MvcResult =
  complete(delete(url) { requestAttr(HTTP_API_REQUEST_CONTEXT_ATTR, RequestContext(userId = userId)) }.andReturn())

private fun MockMvc.complete(mvcResult: MvcResult): MvcResult =
  if (mvcResult.request.isAsyncStarted) perform(asyncDispatch(mvcResult)).andReturn() else mvcResult

fun assertStatus(result: MvcResult, expected: Int) {
  val actual = result.response.status
  assert(actual == expected) { "expected $expected, got $actual: ${result.response.contentAsString}" }
}

/** A denial must look exactly like a missing resource: 404, `NOT_FOUND`, the same message. */
fun assertNotFound(result: MvcResult, message: String) {
  assertStatus(result, 404)
  val body = result.response.contentAsString
  assert(body.contains("\"code\":\"NOT_FOUND\"")) { body }
  assert(body.contains("\"message\":\"$message\"")) { body }
}
