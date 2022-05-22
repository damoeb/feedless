package org.migor.rich.rss.api.dto

data class AppSettingsJsonDto(
  val jsSupport: Boolean,
  val stateless: Boolean,
  val webToFeedVersion: String,
)
