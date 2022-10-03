package org.migor.rich.rss.database

import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.Subscription

interface DeepArticleResult {
  var article: ArticleEntity
  var feed: NativeFeedEntity
  var subscription: Subscription
}

//@Converter(autoApply = true)
//class ArticleFeedAndSubscriptionResultConverter: AttributeConverter<TupleBackedMap, ArticleFeedAndSubscriptionResult> {
//
//}
