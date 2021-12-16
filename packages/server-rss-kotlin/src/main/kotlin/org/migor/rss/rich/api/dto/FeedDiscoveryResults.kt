package org.migor.rss.rich.api.dto

import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.transform.GenericFeedRule

data class FeedDiscoveryResults(
  val genericFeedRules: List<GenericFeedRule>,
  val relatedFeeds: List<Feed>,
  val mimeType: String?,
  val nativeFeeds: List<FeedReference>?,
  val body: String?,
  val failed: Boolean,
  val errorMessage: String? = null
)
