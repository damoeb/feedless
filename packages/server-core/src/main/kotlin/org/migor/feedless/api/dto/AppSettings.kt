package org.migor.feedless.api.dto

data class AppSettings(
  val flags: AppFeatureFlags,
  val webToFeedVersion: String,
  val urls: Map<String, String>,
)
