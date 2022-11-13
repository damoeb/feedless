package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*


@Service
class ContextService {

  @Autowired
  lateinit var articleDAO: ArticleDAO

  fun byArticleId(articleId: UUID): List<ArticleEntity> {
    val article = articleDAO.findById(articleId).orElseThrow()
    val type = ArticleType.feed
    val status = ReleaseStatus.released
    val pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"))

    val successors = articleDAO.findAllAfter(article.releasedAt!!, article.streamId, type, status, pageable)
    val headOfStream = articleDAO.findAllByStreamId(article.streamId, arrayOf(type), arrayOf(status), pageable)

    return successors.plus(headOfStream).distinct()
  }
}
