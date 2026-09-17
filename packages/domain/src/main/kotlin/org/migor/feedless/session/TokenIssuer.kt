package org.migor.feedless.session

import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.Capability
import org.migor.feedless.group.GroupAndRole
import org.migor.feedless.repository.RepositoryClaimId
import org.migor.feedless.user.User
import org.migor.feedless.userSecret.UserSecret
import org.migor.feedless.userSecret.UserSecretId
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.time.Duration

interface TokenIssuer {
  /** API token acting as [user] in [actingGroup], valid only while the secret [secretId] exists; callers resolve the group with actingGroupOf. */
  fun issueApiToken(user: User, actingGroup: GroupAndRole, secretId: UserSecretId): AuthToken
  fun getExpiration(authority: AuthTokenType): Duration
  fun issueAnonymousToken(): AuthToken
  fun issueTokenForCapabilities(capabilities: List<Capability<out Any>>): AuthToken
  suspend fun decodeJwt(token: String): Jwt
  fun createJwtForAnonymousFeed(host: String, id: RepositoryClaimId): Jwt
  fun createJwtForReport(reportId: String, validForDays: Long): Jwt
  fun createJwtForRecipient(recipientId: String, validForDays: Long): Jwt

  /** Token a connected agent authenticates with, acting as [securityKey]'s owner. */
  fun createJwtForService(securityKey: UserSecret): Jwt
}
