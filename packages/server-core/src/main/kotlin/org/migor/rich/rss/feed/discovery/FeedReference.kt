package org.migor.rich.rss.feed.discovery

import org.migor.rich.rss.feed.parser.FeedType


data class FeedReference(val url: String, val type: FeedType, val title: String, val description: String? = null)
