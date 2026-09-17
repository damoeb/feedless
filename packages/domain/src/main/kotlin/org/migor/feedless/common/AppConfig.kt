package org.migor.feedless.common

import java.util.Locale

interface AppConfig {
  val apiGatewayUrl: String
  val appHost: String
  val locale: Locale

  companion object {
    const val maxPageSize: Int = 30
  }
}
