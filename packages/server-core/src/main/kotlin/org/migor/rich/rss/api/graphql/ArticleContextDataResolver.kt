package org.migor.rich.rss.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.generated.DgsConstants
import org.migor.rich.rss.generated.types.Article
import org.migor.rich.rss.generated.types.ArticleContext
import org.migor.rich.rss.api.graphql.DtoResolver.toDTO
import org.migor.rich.rss.generated.types.WebDocument
import org.migor.rich.rss.service.ContextService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class ArticleContextDataResolver {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var contextService: ContextService

  @DgsData(parentType = DgsConstants.ARTICLECONTEXT.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun articles(
    @InputArgument("page") page: Int,
    dfe: DgsDataFetchingEnvironment
  ): List<Article> = coroutineScope {
    val context: ArticleContext = dfe.getSource()
    contextService.getArticles(UUID.fromString(context.articleId), page).map { toDTO(it) }
  }

  @DgsData(parentType = DgsConstants.ARTICLECONTEXT.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun links(
    @InputArgument("page") page: Int,
    dfe: DgsDataFetchingEnvironment
  ): List<WebDocument> = coroutineScope {
    val context: ArticleContext = dfe.getSource()
    contextService.getLinks(UUID.fromString(context.articleId), page).map { toDTO(it) }
  }
}
