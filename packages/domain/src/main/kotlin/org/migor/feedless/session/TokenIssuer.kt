package org.migor.feedless.session

import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.Capability
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.user.User
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.time.Duration

interface TokenIssuer {
  /** API token acting as [user] in [actingGroup]; callers resolve the group with actingGroupOf. */
  fun issueApiToken(user: User, actingGroup: GroupAndRole): AuthToken
  fun getExpiration(authority: AuthTokenType): Duration
  fun issueAnonymousToken(): AuthToken
  fun issueTokenForCapabilities(capabilities: List<Capability<out Any>>): AuthToken
  suspend fun decodeJwt(token: String): Jwt
}
