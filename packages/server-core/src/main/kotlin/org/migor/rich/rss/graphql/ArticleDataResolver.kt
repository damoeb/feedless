package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Article
import org.migor.rich.rss.generated.types.ArticleContext
import org.migor.rich.rss.generated.types.Bucket
import org.migor.rich.rss.generated.types.Content
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class ArticleDataResolver {

  @Autowired
  lateinit var contentService: ContentService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var bucketService: BucketService

  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun content(dfe: DgsDataFetchingEnvironment): Content? = coroutineScope {
    val article: Article = dfe.getSource()
    contentService.findById(UUID.fromString(article.contentId)).map { toDTO(it) }.orElseThrow()
  }

  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun context(dfe: DgsDataFetchingEnvironment): ArticleContext? = coroutineScope {
    val article: Article = dfe.getSource()
//    val context = contextService.byArticleId(UUID.fromString(article.id))
    ArticleContext.newBuilder()
      .articleId(article.id)
//      .setArticles(context.articles.map { toDTO(it) })
//      .setLinks(context.links.map { toDTO(it) })
      .build()
  }

//  @DgsData(parentType = DgsConstants.ARTICLE.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeed? = coroutineScope {
//    val article: Article = dfe.getSource()
//    feedService.findNativeById(UUID.fromString(article.nativeFeedId)).map { toDTO(it) }.orElseThrow()
//  }

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
