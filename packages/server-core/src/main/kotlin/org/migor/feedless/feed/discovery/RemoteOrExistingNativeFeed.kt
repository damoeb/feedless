package org.migor.feedless.feed.discovery

import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.feed.parser.FeedType


data class RemoteOrExistingNativeFeed(val remote: RemoteNativeFeedRef? = null, val existing: NativeFeedEntity? = null)
data class RemoteNativeFeedRef(val url: String, val type: FeedType, val title: String, val description: String? = null)
