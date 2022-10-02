package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.generated.ArticleGql
import org.migor.rich.rss.generated.EnclosureGql
import org.migor.rich.rss.service.BucketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphqlQuery : GraphQLQueryResolver {

  @Autowired
  lateinit var bucketService: BucketService

  fun articles(bucketId: String): List<ArticleGql> {
    val bucket = bucketService.findByBucketId(bucketId, 0, "")
    return bucket.items.map { article -> ArticleGql.builder()
      .setId(article.id)
      .setTitle(article.title)
      .setImageUrl(article.imageUrl)
      .setUrl(article.url)
      .setContentText(article.contentText)
      .setContentRaw(article.contentRaw)
      .setContentRawMime(article.contentRawMime)
      .setTags(article.tags)
      .setPublishedAt(article.publishedAt.time.toDouble())
      .setEnclosures(article.enclosures?.map { enclosure -> EnclosureGql.builder().setUrl(enclosure.url).setType(enclosure.type).build() })
      .build()}
  }
}
