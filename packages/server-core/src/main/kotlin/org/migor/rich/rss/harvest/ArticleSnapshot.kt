package org.migor.rich.rss.harvest

import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.model.Subscription

data class ArticleSnapshot(val article: Article, val feed: Feed, val subscription: Subscription)
