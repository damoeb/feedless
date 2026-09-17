package org.migor.feedless.session

fun testSessionProperties(
  jwtSecret: String = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm",
  whitelistedHosts: List<String> = emptyList(),
) = SessionProperties(jwtSecret = jwtSecret, whitelistedHosts = whitelistedHosts)

fun testRootUserProperties(
  rootEmail: String = "admin@localhost",
  rootSecretKey: String = "QDWSM3OYBPGTEVSPB5FKVDM3CSNCWHVK",
) = RootUserProperties(rootEmail = rootEmail, rootSecretKey = rootSecretKey)
