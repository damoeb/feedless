package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.generated.ArticleGql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphqlQuery: GraphQLQueryResolver {

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  fun articles(bucketId: String): List<ArticleGql> {
    val pageable = PageRequest.of(0, 10)
    val bucket = bucketDAO.findById(UUID.fromString(bucketId)).orElseThrow()
//    articleDAO.findAllByStreamId(bucket.streamId!!, pageable).map { row: ArticleEntityDateTuple -> ArticleGql.builder()
//      .setId(row.first.id.toString())
////      .setPublishedAt("row.second.time")
//      .build() }
    return listOf(ArticleGql.builder().setId("1").build())
  }
}
