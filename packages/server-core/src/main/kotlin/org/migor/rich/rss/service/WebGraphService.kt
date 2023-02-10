package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.HarvestTaskEntity
import org.migor.rich.rss.database.models.HyperLinkEntity
import org.migor.rich.rss.database.models.WebDocumentEntity
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.database.repositories.HyperLinkDAO
import org.migor.rich.rss.database.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
class WebGraphService {

  private val log = LoggerFactory.getLogger(WebGraphService::class.simpleName)

  @Autowired
  lateinit var linkService: LinkService

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var hyperLinkDAO: HyperLinkDAO

  @Autowired
  lateinit var contentDAO: ContentDAO

  fun recordOutgoingLinks(corrId: String, contents: List<ContentEntity>) {
    log.debug("[${corrId}] recordOutgoingLinks ${contents.size}")

    hyperLinkDAO.deleteAllByFromIdIn(contents.map { it.id })

    val webDocuments = mutableSetOf<WebDocumentEntity>()
    val harvestTasks = mutableSetOf<HarvestTaskEntity>()

    val hyperlinks = contents.flatMap { content ->
      run {

        val linkTargets = linkService.extractLinkTargets(corrId, content)
        val linkScore = 1.0 / linkTargets.size

        linkTargets.map {
          run {
            val url = it.url.toString()
            val webDocument = Optional.ofNullable(webDocuments.find { it.url == url })
              .orElseGet {
                run {
                  if (webDocumentDAO.existsByUrl(url)) {
                    webDocumentDAO.findByUrlEquals(url).get()
                  } else {
                    val webDocument = WebDocumentEntity()
                    webDocument.url = url
                    webDocuments.add(webDocument)

                    val harvestTask = HarvestTaskEntity()
                    harvestTask.webDocument = webDocument
                    harvestTasks.add(harvestTask)

                    webDocument
                  }
                }
              }

            val link = HyperLinkEntity()
            link.from = content
            link.to = webDocument
            link.hyperText = it.text
            link.relevance = linkScore
            link
          }
        }
      }
    }
    webDocumentDAO.saveAll(webDocuments)
    harvestTaskDAO.saveAll(harvestTasks)
    hyperLinkDAO.saveAll(hyperlinks)
  }

  fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity> {
    return webDocumentDAO.findAllOutgoingHyperLinksByContentId(article.contentId, pageable).content
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun finalizeWebDocumentHarvest(webDocument: WebDocumentEntity) {
    webDocument.finished = true
    webDocumentDAO.save(webDocument)
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun finalizeContentHarvest(corrId: String, content: ContentEntity) {
    contentDAO.saveFulltextContent(
      content.id,
      content.url!!,
      content.contentTitle,
      content.contentRaw,
      content.contentRawMime,
      content.contentSource,
      content.contentText,
      content.imageUrl,
      Date()
    )
  }

}
