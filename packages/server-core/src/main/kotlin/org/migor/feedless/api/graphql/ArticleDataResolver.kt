package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Article
import org.migor.feedless.generated.types.ArticleContext
import org.migor.feedless.generated.types.Bucket
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.BucketService
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.WebDocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class ArticleDataResolver {

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

//  @Autowired
//  lateinit var contextService: ContextService

  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun webDocument(dfe: DgsDataFetchingEnvironment): WebDocument? = coroutineScope {
    val article: Article = dfe.getSource()
    webDocumentService.findById(UUID.fromString(article.webDocumentId))
      .map { toDTO(it) }
      .orElseThrow { IllegalArgumentException("webDocument not found") }
  }

  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun context(dfe: DgsDataFetchingEnvironment): ArticleContext? = coroutineScope {
    val article: Article = dfe.getSource()
//    val articles = contextService.getArticles(UUID.fromString(article.id), 0)
//    val links = contextService.getLinks(UUID.fromString(article.id), 0)
    ArticleContext.newBuilder()
      .articleId(article.id)
//      .articles(articles.map { toDTO(it) })
      .articles(emptyList())
//      .links(links.map { toDTO(it) })
      .links(emptyList())
      .build()
  }

  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun bucket(dfe: DgsDataFetchingEnvironment): Bucket? = coroutineScope {
    val article: Article = dfe.getSource()
    bucketService.findByStreamId(UUID.fromString(article.streamId)).map { toDTO(it) }.orElse(null)
  }

//  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun context(dfe: DgsDataFetchingEnvironment): ContextDto? = coroutineScope {
//    val article: ArticleDto = dfe.getSource()
//    bucketService.findByStreamId(UUID.fromString(article.streamId)).map { toDTO(it) }.orElseThrow()
//  }
}
