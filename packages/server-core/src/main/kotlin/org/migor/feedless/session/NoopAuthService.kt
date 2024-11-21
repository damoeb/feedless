package org.migor.feedless.session

import jakarta.servlet.http.HttpServletRequest
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("!${AppProfiles.session} & ${AppLayer.service}")
class NoopAuthService : IAuthService {
  override suspend fun decodeToken(token: String): OAuth2AuthenticationToken? = null
  override fun interceptJwt(request: HttpServletRequest): Jwt? = null
  override suspend fun assertToken(request: HttpServletRequest) {

  }
}
