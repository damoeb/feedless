package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.ContextDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContextService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class ArticleDataResolver {

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var contextService: ContextService

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun content(dfe: DgsDataFetchingEnvironment): ContentDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
    articleService.findContentById(UUID.fromString(article.contentId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun context(dfe: DgsDataFetchingEnvironment): ContextDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
    val articles = contextService.byArticleId(UUID.fromString(article.id))
    ContextDto.builder()
      .setArticles(articles.map { toDTO(it) })
      .build()
  }

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeedDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
    feedService.findNativeById(UUID.fromString(article.nativeFeedId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun bucket(dfe: DgsDataFetchingEnvironment): BucketDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
    bucketService.findByStreamId(UUID.fromString(article.streamId)).map { toDTO(it) }.orElseThrow()
  }

//  @DgsData(parentType = "Article")
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun context(dfe: DgsDataFetchingEnvironment): ContextDto? = coroutineScope {
//    val article: ArticleDto = dfe.getSource()
//    bucketService.findByStreamId(UUID.fromString(article.streamId)).map { toDTO(it) }.orElseThrow()
//  }
}
