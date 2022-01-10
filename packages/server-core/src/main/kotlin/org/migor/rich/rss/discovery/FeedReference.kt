package org.migor.rich.rss.discovery

import org.migor.rich.rss.harvest.feedparser.FeedType

data class FeedReference(val url: String?, val type: FeedType?, val title: String?)
