package org.migor.rich.rss.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.service.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtRequestFilter : OncePerRequestFilter() {

  @Autowired
  lateinit var authService: AuthService

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val headerValue = request.getHeader("authorization")
    if (StringUtils.startsWith(headerValue, "Bearer ")) {
      val jwtToken = authService.decodeJwt(headerValue.substring(7))
      val attributes = mapOf("name" to "anonymous")
      val authorities: List<OAuth2UserAuthority> =
        authService.getAuthorities(jwtToken).map { OAuth2UserAuthority(it, attributes) }
      val nameAttributeKey = "name"
      val principal: OAuth2User = DefaultOAuth2User(authorities, attributes, nameAttributeKey)
      val authorizedClientRegistrationId = jwtToken.getClaimAsString("id")
      val authenticationToken = OAuth2AuthenticationToken(
        principal,
        authorities,
        authorizedClientRegistrationId
      )

      SecurityContextHolder.getContext().authentication = authenticationToken
    }
    filterChain.doFilter(request, response)
  }
}
