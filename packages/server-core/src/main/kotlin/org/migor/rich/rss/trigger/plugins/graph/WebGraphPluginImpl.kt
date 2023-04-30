package org.migor.rich.rss.trigger.plugins.graph

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.HyperLinkEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.HyperLinkDAO
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.PluginsService
import org.migor.rich.rss.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

data class LinkTarget(val url: URL, val text: String)

@Service
@Profile(AppProfiles.webGraph)
class WebGraphPluginImpl: WebGraphPlugin() {

  private val log = LoggerFactory.getLogger(WebGraphPlugin::class.simpleName)

  @Autowired
  lateinit var hyperLinkDAO: HyperLinkDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  @Lazy
  lateinit var pluginsService: PluginsService

  override fun findOutgoingLinks(article: ArticleEntity, pageable: PageRequest): List<WebDocumentEntity> {
//    return webDocumentDAO.findAllOutgoingHyperLinksByContentId(article.contentId, pageable)
    return emptyList()
  }

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
    log.info("[${corrId}] recordOutgoingLink ${webDocument.id}")

    hyperLinkDAO.deleteAllByFromId(webDocument.id)

    val webDocuments = mutableSetOf<WebDocumentEntity>()
    val linkTargets = extractLinkTargets(corrId, webDocument)
    val linkScore = 1.0 / linkTargets.size

    val hyperlinks = linkTargets.map {
      run {
        val url = it.url.toString()
        val destination = Optional.ofNullable(webDocuments.find { it.url == url })
          .orElseGet {
            webDocumentDAO.findByUrl(url) ?: run {
              val to = WebDocumentEntity()
              to.url = url
              to.plugins = pluginsService.resolvePlugins().map { it.id() }
              to.finalized = true
              to.releasedAt = Date()
              to.updatedAt = Date()
              webDocuments.add(to)
              to
            }
          }

        val link = HyperLinkEntity()
        link.from = webDocument
        link.to = destination
        link.hyperText = it.text
        link.relevance = linkScore
        link
      }
    }
    webDocumentDAO.saveAll(webDocuments)
    hyperLinkDAO.saveAll(hyperlinks)
  }

  private   fun extractLinkTargets(corrId: String, webDocument: WebDocumentEntity): List<LinkTarget> {
    val document = if (webDocument.hasFulltext) {
      webDocument.getContentOfMime("text/html")
    } else {
      webDocument.getContentOfMime("text/html")
    }

    document?.let {
      val doc = HtmlUtil.parseHtml(it, webDocument.url)
      val fromUrl = webDocument.url
      return doc.body().select("a[href]").mapNotNull { link ->
        try {
          LinkTarget(URL(FeedService.absUrl(fromUrl, link.attr("href"))), link.text())
        } catch (e: Exception) {
          log.warn("[${corrId}] ${e.message}")
          null
        }
      }
        .distinct()
        .filter { isNotBlacklisted(it) }
    }
    return emptyList()
  }

  private fun isNotBlacklisted(linkTarget: LinkTarget): Boolean {
    return arrayOf(
      "facebook.com",
      "twitter.com",
      "amazon.com",
      "patreon.com"
    ).none { blackListedUrl -> linkTarget.url.host.contains(blackListedUrl) }
  }

}
