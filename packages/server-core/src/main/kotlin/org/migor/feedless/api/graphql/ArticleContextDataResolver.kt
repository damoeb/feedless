package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.graphql.DtoResolver.toDTO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Article
import org.migor.feedless.generated.types.ArticleContext
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.service.ContextService
import org.migor.feedless.service.FeedService
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
