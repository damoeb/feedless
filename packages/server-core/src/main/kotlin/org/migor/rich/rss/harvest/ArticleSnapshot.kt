package org.migor.rich.rss.harvest

import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.SubscriptionEntity

data class ArticleSnapshot(val article: ArticleEntity, val feed: NativeFeedEntity, val subscription: SubscriptionEntity)
