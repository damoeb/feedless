package org.migor.feedless.common

fun testPublicUrls(
  apiGatewayUrl: String = "http://localhost:8080",
  appHost: String = "http://localhost:4200",
) = PublicUrls(apiGatewayUrl = apiGatewayUrl, appHost = appHost)
