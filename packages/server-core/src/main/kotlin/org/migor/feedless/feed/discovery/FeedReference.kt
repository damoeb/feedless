package org.migor.feedless.feed.discovery

import org.migor.feedless.feed.parser.FeedType


data class FeedReference(val url: String, val type: FeedType, val title: String, val description: String? = null)
