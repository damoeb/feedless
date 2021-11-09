package org.migor.rss.rich.api.dto

import org.migor.rss.rich.discovery.FeedReference
import org.migor.rss.rich.parser.GenericFeedRule

data class FeedDiscovery(val genericFeedRules: List<GenericFeedRule>, val harvestUrl: String, val originalUrl: String, val mimeType: String?, val withJavaScript: Boolean, val nativeFeeds: List<FeedReference>?, val body: String?, val failed: Boolean, val errorMessage: String? = null)
