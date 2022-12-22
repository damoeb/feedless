package org.migor.rich.rss.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.rich.rss.generated.ArticleContextDto
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.WebDocumentDto
import org.migor.rich.rss.graphql.DtoResolver.toDTO
import org.migor.rich.rss.service.ContextService
import org.migor.rich.rss.service.FeedService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
class ArticleContextDataResolver {

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var contextService: ContextService

  @DgsData(parentType = "ArticleContext")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun articles(@InputArgument("page") page: Int,
                       dfe: DgsDataFetchingEnvironment): List<ArticleDto> = coroutineScope {
    val context: ArticleContextDto = dfe.getSource()
    contextService.getArticles(UUID.fromString(context.articleId), page).map { toDTO(it)}
  }

  @DgsData(parentType = "ArticleContext")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun links(@InputArgument("page") page: Int,
                    dfe: DgsDataFetchingEnvironment): List<WebDocumentDto> = coroutineScope {
    val context: ArticleContextDto = dfe.getSource()
    contextService.getLinks(UUID.fromString(context.articleId), page).map { toDTO(it) }
  }
}
