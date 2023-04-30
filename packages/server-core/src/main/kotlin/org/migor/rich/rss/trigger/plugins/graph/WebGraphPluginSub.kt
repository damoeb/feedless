package org.migor.rich.rss.trigger.plugins.graph

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@Profile("!${AppProfiles.webGraph}")
class WebGraphPluginSub: WebGraphPlugin() {
  private val log = LoggerFactory.getLogger(WebGraphPlugin::class.simpleName)

  init {
    log.info("Deactivated WebGraph")
  }
  override fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity> {
    return emptyList()
  }

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
  }

}
