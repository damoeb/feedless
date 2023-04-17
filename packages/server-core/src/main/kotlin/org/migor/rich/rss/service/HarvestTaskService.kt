package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.data.jpa.enums.ArticleSource
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.HarvestTaskEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.HarvestTaskDAO
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.harvest.BlacklistedForSiteHarvestException
import org.migor.rich.rss.harvest.HarvestException
import org.migor.rich.rss.harvest.PageInspectionService
import org.migor.rich.rss.harvest.PuppeteerService
import org.migor.rich.rss.harvest.SiteNotFoundException
import org.migor.rich.rss.service.graph.WebGraphService
import org.migor.rich.rss.transform.ExtractedArticle
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.io.ByteArrayInputStream
import java.time.Duration
import java.util.*


@Service
@Profile(AppProfiles.database)
class HarvestTaskService {
  private val log = LoggerFactory.getLogger(HarvestTaskService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var webGraphService: WebGraphService

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Autowired
  lateinit var contentService: ContentService

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var pageInspectionService: PageInspectionService

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun harvest(
    corrId: String,
    harvestTask: HarvestTaskEntity
  ) {
    runCatching {
      if (harvestTask.webDocumentId != null) {
        log.info("[${corrId}] harvesting ${harvestTask.id} (web document)")
        harvestForWebDocument(corrId, harvestTask, harvestTask.webDocument!!)
      } else {
        if (harvestTask.contentId != null) {
          log.info("[${corrId}] harvesting ${harvestTask.id} (content)")
          harvestForContent(corrId, harvestTask, harvestTask.content!!)
        } else {
          throw RuntimeException("invalid harvestTask ${harvestTask.id}")
        }
      }
      harvestTaskDAO.deleteById(harvestTask.id)
    }.onFailure {
      handleHarvestException(corrId, harvestTask, it)
    }
  }

  private fun handleHarvestException(corrId: String, harvestTask: HarvestTaskEntity, it: Throwable) {
    when (it) {
      is BlacklistedForSiteHarvestException -> {
        deleteHarvestTask(corrId, harvestTask)
      }

      is SiteNotFoundException -> {
        log.info("[$corrId] site not found, deleting article")
        deleteHarvestTask(corrId, harvestTask)
      }

      is HarvestException -> {
        log.info("[$corrId] harvest failed")
        deleteHarvestTask(corrId, harvestTask)
      }

      is IllegalArgumentException -> {
        log.info("[$corrId] invalid: ${it.message}")
        deleteHarvestTask(corrId, harvestTask)
      }

      is HostOverloadingException -> {
        log.info("[$corrId] postponed by host")
        harvestTaskDAO.delayHarvest(harvestTask.id, Date(), datePlus(Duration.ofMinutes(3)))
      }

      else -> {
        it.printStackTrace()
        log.warn("[${corrId}] Failed to extract: ${it.message}")
        deleteHarvestTask(corrId, harvestTask)
      }
    }
  }

  private fun deleteHarvestTask(corrId: String, harvestTask: HarvestTaskEntity) {
    harvestTaskDAO.deleteById(harvestTask.id)
//    harvestTask.contentId?.let {
//      contentDAO.deleteById(harvestTask.contentId!!)
//    }
    harvestTask.webDocumentId?.let {
      webDocumentDAO.deleteById(harvestTask.webDocumentId!!)
    }
  }

  private fun harvestForContent(corrId: String, harvestTask: HarvestTaskEntity, content: ContentEntity) {
    val feed = harvestTask.feed!!
    val askPrerender = feed.harvestSiteWithPrerender
    val url = harvestTask.content!!.url

    harvest(corrId, url, harvestTask, askPrerender).let {
      saveExtractionForContent(corrId, content, it.url, extractFromAny(corrId, url, it))
    }
  }

  private fun harvestForWebDocument(
    corrId: String,
    harvestTask: HarvestTaskEntity,
    webDocument: WebDocumentEntity
  ) {
    val url = harvestTask.webDocument!!.url!!
    saveOgTagsForWebDocument(corrId, webDocument, harvest(corrId, url, harvestTask))
  }

  private fun saveOgTagsForWebDocument(
    corrId: String,
    webDocument: WebDocumentEntity,
    response: HttpResponse
  ) {
    val inspection = pageInspectionService.fromDocument(parseHtml(String(response.responseBody), webDocument.url!!))
    webDocument.title = inspection.valueOf(pageInspectionService.title)
    val mimeType = MimeType.valueOf(response.contentType)
    webDocument.type = "${mimeType.type}/${mimeType.subtype}"
    webDocument.description = inspection.valueOf(pageInspectionService.description)
    webDocument.imageUrl = inspection.valueOf(pageInspectionService.imageUrl)

    webGraphService.finalizeWebDocumentHarvest(webDocument)
    log.debug("[${corrId}] saved OG tags $webDocument")
  }

  private fun harvest(
    corrId: String,
    url: String,
    harvestTask: HarvestTaskEntity,
    askPrerender: Boolean = false
  ): HttpResponse {

    if (isBlacklistedForHarvest(url)) {
      log.warn("[$corrId] Blacklisted for harvesting $url")
      throw BlacklistedForSiteHarvestException(url)
    }

    val canPrerender = harvestTask.contentId != null && arrayOf("text/html", "text/plain").contains(
      httpService.getContentTypeForUrl(
        corrId,
        url
      )
    )

    return if (canPrerender && askPrerender) {
      log.info("[$corrId] trigger prerendering for $harvestTask")
      val options = GenericFeedFetchOptions(
        websiteUrl = url,
        prerender = true,
      )
      val puppeteerResponse = puppeteerService.prerender(corrId, options).blockFirst()!!
      HttpResponse(
        contentType = "text/html",
        url = url,
        responseBody = puppeteerResponse.html.encodeToByteArray(),
        statusCode = 200
      )
    } else {
      log.info("[$corrId] fetching static content for $url")
      httpService.guardedHttpResource(url, 200, listOf("text/"))
      httpService.httpGet(corrId, url, 200)
    }
  }

  private fun datePlus(duration: Duration): Date {
    return Date(System.currentTimeMillis() + duration.toMillis())
  }

  companion object {
    fun isBlacklistedForHarvest(url: String): Boolean {
      return listOf("https://twitter.com", "https://www.imdb.com", "https://www.google.").any { url.startsWith(it) }
    }
  }

  private fun extractFromAny(
    corrId: String,
    url: String,
    response: HttpResponse
  ): ExtractedArticle {
    val mime = MimeType.valueOf(response.contentType)
    val contentType = "${mime.type}/${mime.subtype}"
    log.info("[${corrId}] mime $contentType")
    return when (contentType) {
      "text/html" -> fromMarkup(corrId, url, String(response.responseBody))
      "text/plain" -> fromText(corrId, url, response)
      "application/pdf" -> fromPdf(corrId, url, response)
      else -> {
        log.warn("[${corrId}] Cannot extract article from mime $contentType")
        throw IllegalArgumentException("Unsupported contentType $contentType for extraction")
      }
    }
  }

  private fun fromText(corrId: String, url: String, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from text")
    val extractedArticle = ExtractedArticle(url)
    extractedArticle.contentText = StringUtils.trimToNull(String(response.responseBody))
    return extractedArticle
  }

  fun fromPdf(corrId: String, url: String, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from pdf")
    ByteArrayInputStream(response.responseBody).use {
      val handler = BodyContentHandler()
      val metadata = Metadata()
      val parser = AutoDetectParser()
      val parseContext = ParseContext()
      parser.parse(it, handler, metadata, parseContext)

      val extractedArticle = ExtractedArticle(url)
      extractedArticle.title = metadata.get(TikaCoreProperties.TITLE)
      extractedArticle.contentText = handler.toString().replace("\n|\r|\t", " ")
      log.info("[${corrId}] pdf-content ${extractedArticle.contentText}")
      return extractedArticle
    }
  }

  private fun fromMarkup(corrId: String, url: String, markup: String): ExtractedArticle {
    log.info("[${corrId}] from markup")
    return webToArticleTransformer.fromHtml(markup, url)
  }

  private fun saveExtractionForContent(
    corrId: String,
    content: ContentEntity,
    url: String,
    extractedArticle: ExtractedArticle
  ) {
    extractedArticle.title?.let {
      log.debug("[$corrId] title ${content.contentTitle} -> $it")
      content.contentTitle = it
    }
    extractedArticle.content?.let {
      log.debug(
        "[$corrId] contentRawMime ${content.contentRawMime} -> ${
          StringUtils.substring(
            extractedArticle.contentMime,
            0,
            100
          )
        }"
      )

      if (extractedArticle.contentMime!!.startsWith("text/html")) {
        val document = parseHtml(it, content.url)
//        content.contentRaw = contentService.inlineImages(corrId, document)
        content.contentRaw = document.body().html()
      } else {
        content.contentRaw = it
      }
      content.contentRawMime = extractedArticle.contentMime!!
    }
    log.debug(
      "[$corrId] mainImageUrl ${content.imageUrl} -> ${
        StringUtils.substring(
          extractedArticle.imageUrl,
          0,
          100
        )
      }"
    )
    content.imageUrl = StringUtils.trimToNull(extractedArticle.imageUrl) ?: content.imageUrl
    content.contentSource = ArticleSource.WEBSITE
    content.contentText = StringUtils.trimToEmpty(extractedArticle.contentText)
    content.hasFulltext = StringUtils.isNoneBlank(content.contentRaw)
    content.url = url
//      todo mag
//      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
//        .toMutableSet()
//      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
//      article.tags = tags.toList()

    webGraphService.finalizeContentHarvest(corrId, content)

    webGraphService.recordOutgoingLinks(corrId, listOf(content))
  }

}
