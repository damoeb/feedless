package org.migor.rss.rich.api.dto

import org.migor.rss.rich.discovery.FeedReference

data class FeedDiscovery(val harvestUrl: String, val originalUrl: String, val feeds: List<FeedReference>?, val body: String?)
