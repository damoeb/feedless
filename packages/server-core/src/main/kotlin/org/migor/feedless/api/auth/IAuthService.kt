package org.migor.feedless.api.auth

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service


enum class Authority {
  ANONYMOUS,
  PROVIDE_HTTP_RESPONSE,
  USER
}

object JwtParameterNames {
  const val EXP = "exp"
  const val ID = "id"
  const val IAT = "iat"
  const val USER_ID = "user_id"
  const val TYPE = "token_type"
}

enum class AuthTokenType(val value: String) {
  ANON("ANON"),
  USER("USER"),
  API("API"),
  AGENT("AGENT"),
}

@Service
interface IAuthService {
  fun decodeToken(token: String): OAuth2AuthenticationToken?
  fun interceptJwt(request: HttpServletRequest): Jwt?
  fun assertToken(request: HttpServletRequest)
}
