package org.migor.rich.rss.auth

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

class AuthenticationHttpSessionHandshakeInterceptor(val authService: AuthService) : HttpSessionHandshakeInterceptor() {
  override fun beforeHandshake(
    request: ServerHttpRequest,
    response: ServerHttpResponse,
    wsHandler: WebSocketHandler,
    attributes: MutableMap<String, Any>
  ): Boolean {
    authService.interceptTokenCookie((request as ServletServerHttpRequest).servletRequest)
    return super.beforeHandshake(request, response, wsHandler, attributes)
  }
}
