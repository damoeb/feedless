package org.migor.rich.rss.service.graph

import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.springframework.data.domain.PageRequest

interface WebGraphService {

  fun recordOutgoingLinks(corrId: String, contents: List<ContentEntity>)

  fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity>

  fun finalizeWebDocumentHarvest(webDocument: WebDocumentEntity)

  fun finalizeContentHarvest(corrId: String, content: ContentEntity)
}
