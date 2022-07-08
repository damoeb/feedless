package org.migor.rich.rss.api.dto

import org.migor.rich.rss.service.AuthTokenType

data class PermanentFeedUrl(
  val feedUrl: String,
  val type: AuthTokenType
)
