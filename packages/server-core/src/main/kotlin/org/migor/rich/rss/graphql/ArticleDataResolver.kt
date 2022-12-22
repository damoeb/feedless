package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.ArticleContextDto
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class ArticleDataResolver {

  @Autowired
  lateinit var contentService: ContentService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun content(dfe: DgsDataFetchingEnvironment): ContentDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
    contentService.findById(UUID.fromString(article.contentId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = "Article")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun context(dfe: DgsDataFetchingEnvironment): ArticleContextDto? = coroutineScope {
    val article: ArticleDto = dfe.getSource()
//    val context = contextService.byArticleId(UUID.fromString(article.id))
    ArticleContextDto.builder()
      .setArticleId(article.id)
//      .setArticles(context.articles.map { toDTO(it) })
//      .setLinks(context.links.map { toDTO(it) })
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
