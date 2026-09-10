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

  /**
   * Every `/api/v1` handler is a `suspend fun`, so Spring MVC completes it on an ASYNC dispatch that
   * runs the security filter chain again. This filter does not run on that dispatch (a
   * [OncePerRequestFilter] skips async dispatches); the chain's `SecurityContextHolderFilter` loads
   * the context from this repository instead. A request attribute lives exactly as long as this one
   * request, and it is the repository the stateless chain reads — the same default
   * `BasicAuthenticationFilter` saves to.
   */
  private val securityContextRepository: SecurityContextRepository = RequestAttributeSecurityContextRepository()

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    // No public auth issuance path on the HTTP API: every /api/v1/** request requires a UserSecret Bearer JWT.
    return !request.requestURI.startsWith("/api/v1/")
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
        val context = securityContextHolderStrategy.createEmptyContext()
        context.authentication = tokenAuthenticator.authenticate(jwt, request)
        securityContextHolderStrategy.context = context
        securityContextRepository.saveContext(context, request, response)
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
