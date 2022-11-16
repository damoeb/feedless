package org.migor.rich.rss.service

import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.HarvestTaskEntity
import org.migor.rich.rss.database.models.HyperLinkEntity
import org.migor.rich.rss.database.models.WebDocumentEntity
import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.database.repositories.HyperLinkDAO
import org.migor.rich.rss.database.repositories.WebDocumentDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
@Profile("database")
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

  fun recordOutgoingLinks(corrId: String, contents: List<ContentEntity>) {
    log.info("[${corrId}] recordOutgoingLinks ${contents.size}")

    hyperLinkDAO.deleteAllByFromIdIn(contents.map { it.id })

    val webDocuments = mutableListOf<WebDocumentEntity>()
    val harvestTasks = mutableListOf<HarvestTaskEntity>()

    val hyperlinks = contents.flatMap { content ->
      run {

        val linkTargets = linkService.extractLinkTargets(content)
        val linkScore = 1.0 / linkTargets.size

        linkTargets.map {
          run {
            val url = it.url.toString()
            val webDocument = if (webDocumentDAO.existsByUrl(url)) {
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
    log.info("[${corrId}] save webDocuments")
    webDocumentDAO.saveAll(webDocuments)
    log.info("[${corrId}] save harvestTasks")
    harvestTaskDAO.saveAll(harvestTasks)
    log.info("[${corrId}] save hyperlinks")
    hyperLinkDAO.saveAll(hyperlinks)
  }

  private fun toHyperLink(
    from: ContentEntity,
    to: LinkTarget,
    linkScore: Double
  ): HyperLinkEntity? {
    return runCatching {
      val link = HyperLinkEntity()
      link.from = from
      link.to = getOrCreateWebDocument(to.url.toString())
      link.hyperText = to.text
      link.relevance = linkScore
      link
    }.getOrNull()
  }

  private fun getOrCreateWebDocument(url: String): WebDocumentEntity {
    return webDocumentDAO.findByUrlEquals(url).orElseGet {
      run {
        val webDocument = WebDocumentEntity()
        webDocument.url = url
        val savedWebDocument = webDocumentDAO.save(webDocument)

        val task = HarvestTaskEntity()
        task.webDocument = webDocument
        harvestTaskDAO.save(task)

        savedWebDocument
      }
    }
  }

  fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity> {
    return webDocumentDAO.findAllOutgoingHyperLinksByContentId(article.contentId, pageable).content
  }
}
