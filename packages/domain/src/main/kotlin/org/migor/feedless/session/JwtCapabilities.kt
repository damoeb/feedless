package org.migor.feedless.session

import com.google.gson.JsonSyntaxException
import org.springframework.security.oauth2.jwt.Jwt

object JwtParameterNames {
  const val EXP = "exp"
  const val ID = "id"
  const val IAT = "iat"

  @Deprecated("will be removed")
  const val USER_ID = "user_id"
  const val CAPABILITIES = "capabilities"
  const val TYPE = "token_type"
  const val HOST = "host"

  /**
   * Carries the report id in confirmation and unsubscribe links. Possession
   * of the signed token is the proof there - recipients are typically not
   * logged in.
   */
  const val REPORT_ID = "report_id"

  /** Names the address in the abuse link of report mails; the link must outlive the report it came with. */
  const val RECIPIENT_ID = "recipient_id"

  /** Names the user secret an API token belongs to, so deleting the secret revokes the token. */
  const val SECRET_ID = "secret_id"
}

enum class AuthTokenType(val value: String) {
  ANONYMOUS("ANON"),
  USER("USER"),
  API("API"),
  SERVICE("AGENT"),
}

fun Jwt.capabilities(): List<LazyGrantedAuthority> {
  val capabilitiesMaybe = claims[JwtParameterNames.CAPABILITIES]
  if (capabilitiesMaybe is Map<*, *>) {
    try {
      return capabilitiesMaybe.entries.map {
        LazyGrantedAuthority(it.key as String, it.value as String)
      }
    } catch (_: IllegalArgumentException) {
    } catch (_: JsonSyntaxException) {
    }
  }
  return emptyList()
}
