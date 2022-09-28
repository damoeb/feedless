package org.migor.rich.rss.harvest

import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.model.Subscription
import org.migor.rich.rss.database2.models.ArticleEntity

data class ArticleSnapshot(val article: ArticleEntity, val feed: Feed, val subscription: Subscription)
