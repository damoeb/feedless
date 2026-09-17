package org.migor.feedless.session

import java.time.Duration

fun testSessionProperties(
  jwtSecret: String = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm",
  whitelistedHosts: List<String> = emptyList(),
) = SessionProperties(
  jwtSecret = jwtSecret,
  whitelistedHosts = whitelistedHosts,
  auth = AnonymousTokenProperties(anonymousTokenValidFor = Duration.ofDays(28)),
)

fun testRootUserProperties(
  rootEmail: String = "admin@localhost",
  rootSecretKey: String = "QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK",
) = RootUserProperties(rootEmail = rootEmail, rootSecretKey = rootSecretKey)
