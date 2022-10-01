package org.migor.rich.rss.harvest

import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.SubscriptionEntity

data class ArticleSnapshot(val article: ArticleEntity, val feed: NativeFeedEntity, val subscription: SubscriptionEntity)
