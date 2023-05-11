package org.migor.feedless.api.dto

import org.migor.feedless.feed.discovery.TransientOrExistingNativeFeed
import org.migor.feedless.web.GenericFeedRule

data class FeedDiscoveryDocument(
  val mimeType: String? = null,
  val language: String? = null,
  val imageUrl: String? = null,
  val url: String? = null,
  val body: String? = null,
  val title: String? = null,
  val description: String? = null,
  val statusCode: Int
)

data class FeedDiscoveryResults(
  val genericFeedRules: List<GenericFeedRule>,
  val nativeFeeds: List<TransientOrExistingNativeFeed>,
  val failed: Boolean,
  val errorMessage: String? = null,
  val document: FeedDiscoveryDocument
)
