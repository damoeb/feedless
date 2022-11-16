package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.config.RabbitQueue
import org.migor.rich.rss.database.enums.ArticleSource
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.HarvestTaskEntity
import org.migor.rich.rss.database.models.WebDocumentEntity
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.database.repositories.WebDocumentDAO
import org.migor.rich.rss.generated.MqAskPrerenderingRequestDto
import org.migor.rich.rss.generated.MqPrerenderingResponseDto
import org.migor.rich.rss.harvest.BlacklistedForSiteHarvestException
import org.migor.rich.rss.harvest.HarvestException
import org.migor.rich.rss.harvest.PageInspection
import org.migor.rich.rss.harvest.SiteNotFoundException
import org.migor.rich.rss.transform.ExtractedArticle
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.HtmlUtil
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
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
@Profile("database")
class HarvestTaskService {
  private val log = LoggerFactory.getLogger(HarvestTaskService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var webGraphService: WebGraphService

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @RabbitListener(queues = [RabbitQueue.prerenderingResult])
  fun listenPrerenderResponse(prerenderResponseJson: String) {
    runCatching {
      val response = JsonUtil.gson.fromJson(prerenderResponseJson, MqPrerenderingResponseDto::class.java)
      val corrId = response.correlationId
      val content = Optional.ofNullable(contentDAO.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }
      val harvestTaskId = UUID.fromString(response.harvestTaskId)

      if (response.error) {
        log.error("[${corrId}] Failed to prerender ${response.url}")
        handleHarvestException(corrId, harvestTaskDAO.findById(harvestTaskId).orElseThrow(), IllegalArgumentException("Prerendering failed ${response.error}"))
      } else {
        saveExtractionForContent(corrId, harvestTaskId, content, fromMarkup(corrId, content.url!!, response.data))
      }
    }.onFailure {
      this.log.error("Cannot handle readability ${it.message}")
    }
  }

  @Transactional
  fun harvest(
    corrId: String,
    harvestTask: HarvestTaskEntity
  ) {
    runCatching {
      if (harvestTask.webDocumentId != null) {
        harvestForWebDocument(corrId, harvestTask, harvestTask.webDocument!!)
      } else {
        if (harvestTask.contentId != null) {
          harvestForContent(corrId, harvestTask, harvestTask.content!!)
        } else {
          throw RuntimeException("invalid harvestTask ${harvestTask.id}")
        }
      }
    }.onFailure {
      handleHarvestException(corrId, harvestTask, it)
    }
  }

  private fun handleHarvestException(corrId: String, harvestTask: HarvestTaskEntity, it: Throwable) {
    when (it) {
      is BlacklistedForSiteHarvestException -> {
        deleteHarvestTask(harvestTask)
      }
      is SiteNotFoundException -> {
        log.info("[$corrId] site not found, deleting article")
        deleteHarvestTask(harvestTask)
      }
      is HarvestException -> {
        log.info("[$corrId] harvest failed")
        deleteHarvestTask(harvestTask)
      }
      is IllegalArgumentException -> {
        log.info("[$corrId] argument is invalid")
        deleteHarvestTask(harvestTask)
      }
      is HostOverloadingException -> {
        log.info("[$corrId] postpone harvest")
        harvestTaskDAO.delayHarvest(harvestTask.id, Date(), datePlus(Duration.ofMinutes(3)))
      }
      else -> {
        it.printStackTrace()
        log.warn("[${corrId}] Failed to extract: ${it.message}")
        harvestTaskDAO.persistErrorByContentId(
          harvestTask.id,
          it.message,
          Date(),
          null
        )
      }
    }
  }

  private fun deleteHarvestTask(harvestTask: HarvestTaskEntity) {
    harvestTaskDAO.deleteById(harvestTask.id)
//    harvestTask.contentId?.let {
//      contentDAO.deleteById(it)
//    }
//    harvestTask.webDocumentId?.let {
//      webDocumentDAO.deleteById(it)
//    }
  }

  private fun harvestForContent(corrId: String, harvestTask: HarvestTaskEntity, content: ContentEntity) {
    val feed = harvestTask.feed!!
    val askPrerender = feed.harvestSiteWithPrerender
    val url = harvestTask.content!!.url!!

    harvest(corrId, url, harvestTask, askPrerender)?.let {
      saveExtractionForContent(corrId, harvestTask.id, content, extractFromAny(corrId, url, it))
    }
  }

  private fun harvestForWebDocument(
    corrId: String,
    harvestTask: HarvestTaskEntity,
    webDocument: WebDocumentEntity
  ) {
    val url = harvestTask.webDocument!!.url!!
    harvest(corrId, url, harvestTask)?.let {
      saveOgTagsForWebDocument(corrId, harvestTask, webDocument, it)
    }
  }

  private fun saveOgTagsForWebDocument(
    corrId: String,
    harvestTask: HarvestTaskEntity,
    webDocument: WebDocumentEntity,
    response: HttpResponse
  ) {
    val inspection = PageInspection.fromDocument(HtmlUtil.parse(String(response.responseBody)))
    webDocument.title = inspection.valueOf(PageInspection.title)
    val mimeType = MimeType.valueOf(response.contentType)
    webDocument.type = "${mimeType.type}/${mimeType.subtype}"
    webDocument.description = inspection.valueOf(PageInspection.description)
    webDocument.image = inspection.valueOf(PageInspection.imageUrl)
    webDocumentDAO.save(webDocument)
    harvestTaskDAO.deleteById(harvestTask.id)

    log.info("[${corrId}] saved OG tags $webDocument")
  }

  private fun harvest(
    corrId: String,
    url: String,
    harvestTask: HarvestTaskEntity,
    askPrerender: Boolean = false
  ): HttpResponse? {

    if (isBlacklistedForHarvest(url)) {
      log.warn("[$corrId] Blacklisted for harvesting $url")
      throw BlacklistedForSiteHarvestException(url)
    }

    val canPrerender = harvestTask.contentId != null && arrayOf("text/html", "text/plain").contains(httpService.getContentTypeForUrl(corrId, url))

    return if (canPrerender && askPrerender) {
      log.info("[$corrId] trigger prerendering for $harvestTask")
      val askPrerendering = MqAskPrerenderingRequestDto.Builder()
        .setUrl(url)
        .setCorrelationId(corrId)
        .setHarvestTaskId(harvestTask.id.toString())
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.askPrerendering, JsonUtil.gson.toJson(askPrerendering))
      return null
    } else {
      log.info("[$corrId] fetching static content for $url")
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
        throw IllegalArgumentException("Unsupported contentType ${contentType} for extraction")
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

  private fun saveExtractionForContent(corrId: String, harvestTaskId: UUID, content: ContentEntity, extractedArticle: ExtractedArticle) {
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
      content.contentRaw = extractedArticle.content
      content.contentRawMime = extractedArticle.contentMime!!
    }
    log.debug("[$corrId] mainImageUrl ${content.imageUrl} -> ${StringUtils.substring(extractedArticle.imageUrl, 0, 100)}")
    content.imageUrl = StringUtils.trimToNull(extractedArticle.imageUrl)

    content.contentSource = ArticleSource.WEBSITE
    content.contentText = StringUtils.trimToEmpty(extractedArticle.contentText)
    content.hasFulltext = StringUtils.isNoneBlank(content.contentRaw)
//      todo mag
//      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
//        .toMutableSet()
//      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
//      article.tags = tags.toList()
    contentDAO.saveFulltextContent(
      content.id,
      content.contentTitle,
      content.contentRaw,
      content.contentRawMime,
      content.contentSource,
      content.contentText,
      content.imageUrl,
      Date()
    )
    webGraphService.recordOutgoingLinks(corrId, listOf(content))
    harvestTaskDAO.deleteById(harvestTaskId)
  }

}
