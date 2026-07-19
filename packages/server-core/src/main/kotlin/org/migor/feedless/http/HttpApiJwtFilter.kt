package org.migor.feedless.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.migor.feedless.session.jwtToOAuth2AuthenticationToken
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class HttpApiJwtFilter(
  private val jwtTokenIssuer: JwtTokenIssuer,
) : OncePerRequestFilter() {

  private val log = LoggerFactory.getLogger(HttpApiJwtFilter::class.simpleName)

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    if (!request.requestURI.startsWith("/api/v1/")) {
      return true
    }
    return request.requestURI == "/api/v1/auth/login"
  }

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain,
  ) {
    val authenticated = runBlocking {
      try {
        val jwt = jwtTokenIssuer.decodeJwt(request)
        if (jwt.getClaimAsString(JwtParameterNames.TYPE) == AuthTokenType.ANONYMOUS.value) {
          response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication required")
          return@runBlocking false
        }
        SecurityContextHolder.getContext().authentication = jwtToOAuth2AuthenticationToken(jwt)
        request.setAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, injectCapabilitiesFromSecurityContext())
        true
      } catch (e: Exception) {
        log.debug("HTTP API auth failed: ${e.message}")
        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Authentication required")
        false
      }
    }
    if (authenticated) {
      filterChain.doFilter(request, response)
    }
  }
}
