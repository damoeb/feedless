package org.migor.feedless.session

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Profile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Component
@Deprecated("use DataFetchingEnvironment directly instead")
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class JwtRequestFilter(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val tokenAuthenticator: TokenAuthenticator,
) : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request is HttpServletRequest && response is HttpServletResponse) {
      runBlocking {
        runCatching {
          SecurityContextHolder.getContext().authentication =
            tokenAuthenticator.authenticate(jwtTokenIssuer.decodeJwt(request), request)
        }.onFailure { log.debug(it.message) }
      }
      val attributes = ServletRequestAttributes(request)
      val corrId = StringUtils.trimToNull(request.getHeader(ApiParams.corrId)) ?: newCorrId()
      attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)

      MDC.put(MdcKeys.CORR_ID, corrId)

//      val product = StringUtils.trimToNull(request.getHeader(ApiParams.product)) ?: Vertical.feedless.name
//      attributes.setAttribute("product", product, RequestAttributes.SCOPE_REQUEST)

      RequestContextHolder.setRequestAttributes(attributes)
    }
    chain.doFilter(request, response)
  }


}

/** Requests authenticate through [TokenAuthenticator], which decides which of the token's capabilities count. */
fun jwtToOAuth2AuthenticationToken(
  jwtToken: Jwt,
  authorities: List<GrantedAuthority> = jwtToken.capabilities(),
): OAuth2AuthenticationToken {
  val attributes = mapOf("dummy" to "wef")

  val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, "dummy")
  val authorizedClientRegistrationId = jwtToken.getClaimAsString("id")
  return OAuth2AuthenticationToken(
    principal,
    authorities,
    authorizedClientRegistrationId
  )
}
