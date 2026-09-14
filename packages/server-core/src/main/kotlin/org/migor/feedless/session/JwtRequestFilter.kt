package org.migor.feedless.session

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.capability.withMdcCorrId
import org.migor.feedless.util.HttpUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Component
@Deprecated("use DataFetchingEnvironment directly instead")
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class JwtRequestFilter(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val tokenAuthenticator: TokenAuthenticator,
  @Value("\${spring.graphql.websocket.path:}") private val webSocketPath: String = "",
) : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request !is HttpServletRequest || response !is HttpServletResponse) {
      chain.doFilter(request, response)
      return
    }
    val corrId = HttpUtil.corrIdOf(request)
    response.setHeader(ApiParams.corrId, corrId)
    // Restored afterwards: a pooled thread must not carry this request's id into unrelated work.
    val previousAttributes = RequestContextHolder.getRequestAttributes()
    withMdcCorrId(corrId) {
      try {
        // Every WebSocket operation inherits the handshake's context, and the cross-site TOKEN cookie must not make it a user (DGS 9 parity).
        if (!isWebSocketHandshake(request)) {
          runBlocking {
            runCatching {
              SecurityContextHolder.getContext().authentication =
                tokenAuthenticator.authenticate(jwtTokenIssuer.decodeJwt(request), request)
            }.onFailure { log.debug(it.message) }
          }
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        chain.doFilter(request, response)
      } finally {
        RequestContextHolder.setRequestAttributes(previousAttributes)
      }
    }
  }

  private fun isWebSocketHandshake(request: HttpServletRequest): Boolean =
    webSocketPath.isNotEmpty() && request.requestURI.removePrefix(request.contextPath) == webSocketPath
}

/** Requests authenticate through [TokenAuthenticator], which decides which of the token's capabilities count. */
fun jwtToOAuth2AuthenticationToken(
  jwtToken: Jwt,
  authorities: List<GrantedAuthority> = jwtToken.capabilities(),
): OAuth2AuthenticationToken {
  val attributes = mapOf("dummy" to "wef")

  val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, "dummy")
  // same exception OAuth2AuthenticationToken threw for a missing id before Spring Security 7's nullability
  val authorizedClientRegistrationId =
    requireNotNull(jwtToken.getClaimAsString("id")) { "authorizedClientRegistrationId cannot be empty" }
  return OAuth2AuthenticationToken(
    principal,
    authorities,
    authorizedClientRegistrationId
  )
}
