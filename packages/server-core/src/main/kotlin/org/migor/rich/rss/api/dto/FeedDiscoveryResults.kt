package org.migor.rich.rss.api.dto

import org.migor.rich.rss.discovery.FeedReference
import org.migor.rich.rss.transform.GenericFeedRule

data class FeedDiscoveryDocument(
  val mimeType: String?,
  val language: String?,
  val imageUrl: String?,
  val screenshot: String? = null,
  val body: String?,
  val title: String?,
  val description: String?,
)

data class FeedDiscoveryResults(
  val genericFeedRules: List<GenericFeedRule>,
  val nativeFeeds: List<FeedReference>,
  val failed: Boolean,
  val errorMessage: String? = null,
  val document: FeedDiscoveryDocument?
)
