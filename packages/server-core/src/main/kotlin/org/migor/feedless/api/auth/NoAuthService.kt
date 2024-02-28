package org.migor.feedless.api.auth

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service


@Service
@Profile("!${AppProfiles.database}")
class NoAuthService : IAuthService {
  override fun decodeToken(token: String): OAuth2AuthenticationToken? = null
  override fun interceptJwt(request: HttpServletRequest): Jwt? = null
  override fun assertToken(request: HttpServletRequest) {

  }
}
