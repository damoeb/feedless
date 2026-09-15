package org.migor.feedless.common

interface AppConfig {
  val apiGatewayUrl: String
  val appHost: String

  companion object {
    const val maxPageSize: Int = 30
  }
}
