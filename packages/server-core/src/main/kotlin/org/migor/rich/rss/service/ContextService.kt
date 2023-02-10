package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.WebDocumentEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile(AppProfiles.database)
class ContextService {

  @Autowired
  lateinit var webGraphService: WebGraphService

  @Autowired
  lateinit var articleDAO: ArticleDAO

  fun getLinks(articleId: UUID, page: Int): List<WebDocumentEntity> {
    val article = articleDAO.findById(articleId).orElseThrow()

    val pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "score"))

    return webGraphService.findOutgoingLinks(article, pageable)
  }

  fun getArticles(articleId: UUID, page: Int): List<ArticleEntity> {
    val article = articleDAO.findById(articleId).orElseThrow()
    return findArticlesInContext(article, page)
  }

  fun findArticlesInContext(article: ArticleEntity, page: Int): List<ArticleEntity> {
    val type = ArticleType.feed
    val status = ReleaseStatus.released
    val pageable = PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "createdAt"))

    val successors = articleDAO.findAllAfter(article.releasedAt!!, article.streamId, type, status, pageable)
    val headOfStream = articleDAO.findAllByStreamId(article.streamId, arrayOf(type), arrayOf(status), pageable)

    return successors.plus(headOfStream).distinct()
  }
}
