package org.migor.rich.rss.api.auth

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

class AuthenticationHttpSessionHandshakeInterceptor : HttpSessionHandshakeInterceptor() {
  override fun beforeHandshake(
    request: ServerHttpRequest,
    response: ServerHttpResponse,
    wsHandler: WebSocketHandler,
    attributes: MutableMap<String, Any>
  ): Boolean {
//    authService.interceptTokenCookie((request as ServletServerHttpRequest).servletRequest)
    return super.beforeHandshake(request, response, wsHandler, attributes)
  }

}
