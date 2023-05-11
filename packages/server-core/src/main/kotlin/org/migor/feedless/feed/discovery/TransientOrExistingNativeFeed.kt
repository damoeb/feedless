package org.migor.feedless.feed.discovery

import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.feed.parser.FeedType


data class TransientOrExistingNativeFeed(val transient: TransientNativeFeed? = null, val existing: NativeFeedEntity? = null)
data class TransientNativeFeed(val url: String, val type: FeedType, val title: String, val description: String? = null)
