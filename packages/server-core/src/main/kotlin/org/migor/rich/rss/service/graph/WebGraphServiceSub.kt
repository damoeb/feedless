package org.migor.rich.rss.service.graph

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@Profile("!${AppProfiles.webGraph}")
class WebGraphServiceSub: WebGraphService {
  private val log = LoggerFactory.getLogger(WebGraphService::class.simpleName)

  init {
    log.info("Deactivated WebGraph")
  }

  override fun recordOutgoingLinks(corrId: String, contents: List<ContentEntity>) {
  }

  override fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity> {
    return emptyList()
  }

  override fun finalizeWebDocumentHarvest(webDocument: WebDocumentEntity) {
  }

  override fun finalizeContentHarvest(corrId: String, content: ContentEntity) {
  }
}
