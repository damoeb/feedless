package org.migor.rss.rich.harvest

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.Subscription

data class ArticleSnapshot(val article: Article, val feed: Feed, val subscription: Subscription)
