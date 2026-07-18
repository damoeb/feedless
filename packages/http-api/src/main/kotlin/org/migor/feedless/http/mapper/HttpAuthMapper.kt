package org.migor.feedless.http.mapper

import org.migor.feedless.auth.AuthToken
import org.migor.feedless.http.api.model.AuthenticationResponse
import org.migor.feedless.http.api.model.SessionResponse
import org.migor.feedless.session.SessionInfo
import org.springframework.stereotype.Component

@Component
class HttpAuthMapper {

  fun toAuthenticationResponse(token: AuthToken, corrId: String): AuthenticationResponse =
    AuthenticationResponse(
      token = token.token,
      corrId = corrId,
    )

  fun toSessionResponse(session: SessionInfo): SessionResponse =
    SessionResponse(
      isAnonymous = session.isAnonymous,
      isLoggedIn = session.isLoggedIn,
      userId = session.userId?.uuid,
    )
}
