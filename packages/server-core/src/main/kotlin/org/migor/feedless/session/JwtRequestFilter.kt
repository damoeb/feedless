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
import org.migor.feedless.data.jpa.enums.Vertical
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


@Component
@Profile("${AppProfiles.session} & ${AppLayer.service}")
class JwtRequestFilter : Filter {
  private val log = LoggerFactory.getLogger(JwtRequestFilter::class.simpleName)

  @Autowired
  private lateinit var authService: AuthService

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    if (request is HttpServletRequest && response is HttpServletResponse) {
      runBlocking {
        runCatching {
          SecurityContextHolder.getContext().authentication = toAuthenticationToken(authService.interceptToken(request))
        }
      }
      val attributes = ServletRequestAttributes(request)
      val corrId = StringUtils.trimToNull(request.getHeader(ApiParams.corrId)) ?: newCorrId()
      attributes.setAttribute("corrId", corrId, RequestAttributes.SCOPE_REQUEST)

      val product = StringUtils.trimToNull(request.getHeader(ApiParams.product)) ?: Vertical.feedless.name
      attributes.setAttribute("product", product, RequestAttributes.SCOPE_REQUEST)

      RequestContextHolder.setRequestAttributes(attributes)
    }
    chain.doFilter(request, response)
  }


  private fun toAuthenticationToken(jwtToken: Jwt): OAuth2AuthenticationToken {
    val userId = jwtToken.claims[JwtParameterNames.USER_ID] as String
    val attributes = mapOf(
      JwtParameterNames.USER_ID to userId
    )
    val authorities: List<OAuth2UserAuthority> = getAuthorities(jwtToken).map { OAuth2UserAuthority(it, attributes) }
    val nameAttributeKey = JwtParameterNames.USER_ID
    val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, nameAttributeKey)
    val authorizedClientRegistrationId = jwtToken.getClaimAsString("id")
    return OAuth2AuthenticationToken(
      principal,
      authorities,
      authorizedClientRegistrationId
    )
  }
  private fun getAuthorities(jwt: Jwt): List<String> {
    return jwt.getClaim("authorities") as List<String>
  }
}
