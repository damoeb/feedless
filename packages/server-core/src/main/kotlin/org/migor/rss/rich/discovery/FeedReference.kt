package org.migor.rss.rich.discovery

import org.migor.rss.rich.harvest.feedparser.FeedType

data class FeedReference(val url: String?, val type: FeedType?, val title: String?)
