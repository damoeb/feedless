package org.migor.rss.rich.plugins

import org.migor.rss.rich.database.enums.PostProcessorType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket

interface PostProcessorPlugin<O> {
  fun getType(): PostProcessorType
  fun getDefaultOptions(): O
  fun process(article: Article, bucket: Bucket, options: Map<String,String>?)
}
