package org.migor.rich.rss.api.dto

data class AppSettingsJsonDto(
  val canPrerender: Boolean,
  val stateless: Boolean,
  val webToFeedVersion: String,
  val willExtractFulltext: Boolean,
  val canMail: Boolean,
  val canPush: Boolean,
  val hasPuppeteerHost: Boolean,
)
