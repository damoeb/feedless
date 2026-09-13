package org.migor.feedless.http

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.capability.HTTP_API_REQUEST_CONTEXT_ATTR
import org.migor.feedless.session.AuthTokenType
import org.migor.feedless.session.JwtParameterNames
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.TokenAuthenticator
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.migor.feedless.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class HttpApiJwtFilter(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val tokenAuthenticator: TokenAuthenticator,
) : OncePerRequestFilter() {

  private val log = LoggerFactory.getLogger(HttpApiJwtFilter::class.simpleName)

  private val securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()

  /** The async re-dispatch of suspend handlers skips this filter, so the context is stored where SecurityContextHolderFilter reloads it. */
  private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    // Every /api/v1 request needs a Bearer JWT, except GET/HEAD /api/v1/status, which is public.
    return !request.requestURI.startsWith("/api/v1/") || isPublicStatusRequest(request)
  }

  private fun isPublicStatusRequest(request: HttpServletRequest): Boolean =
    (request.method == "GET" || request.method == "HEAD") &&
      request.requestURI == StatusHttpController.PUBLIC_STATUS_PATH

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
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = tokenAuthenticator.authenticate(jwt, request)
        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
        request.setAttribute(HTTP_API_REQUEST_CONTEXT_ATTR, injectCapabilitiesFromSecurityContext(HttpUtil.corrIdOf(request)))
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
