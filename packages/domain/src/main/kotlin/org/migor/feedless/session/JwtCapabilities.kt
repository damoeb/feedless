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
   * Trägt die Report-Id in Bestätigungs- und Abmeldelinks. Der Besitz des
   * signierten Tokens ist dort der Nachweis - die Empfänger sind
   * typischerweise nicht angemeldet.
   */
  const val REPORT_ID = "report_id"

  /** Names the address in the abuse link of report mails; the link must outlive the report it came with. */
  const val RECIPIENT_ID = "recipient_id"
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
