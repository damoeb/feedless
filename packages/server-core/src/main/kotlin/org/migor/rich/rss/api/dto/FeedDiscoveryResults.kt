package org.migor.rich.rss.api.dto

import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.discovery.FeedReference
import org.migor.rich.rss.transform.GenericFeedRule

data class FeedDiscoveryResults(
  val genericFeedRules: List<GenericFeedRule>,
  val relatedFeeds: List<NativeFeedEntity>,
  val mimeType: String?,
  val screenshot: String? = null,
  val nativeFeeds: List<FeedReference>,
  val body: String?,
  val failed: Boolean,
  val errorMessage: String? = null
)
