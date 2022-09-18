package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.generated.ArticleGql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.sql.Date

@Component
class GraphqlQuery: GraphQLQueryResolver {

  @Autowired
  lateinit var articleDAO: ArticleDAO

  fun articles(bucketId: String): List<ArticleGql> {
    val pageable = PageRequest.of(0, 10)
    articleDAO.findAllByBucketId(bucketId, pageable).map { row: Pair<ArticleEntity, Date> -> ArticleGql.builder()
      .setId(row.first.id.toString())
//      .setPublishedAt("row.second.time")
      .build() }
    return listOf(ArticleGql.builder().setId("1").build())
  }
}
