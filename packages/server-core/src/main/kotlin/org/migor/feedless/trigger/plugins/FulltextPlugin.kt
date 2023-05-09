package org.migor.feedless.trigger.plugins

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.jsoup.Jsoup
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.harvest.HarvestAbortedException
import org.migor.feedless.service.PuppeteerService
import org.migor.feedless.service.HttpResponse
import org.migor.feedless.service.HttpService
import org.migor.feedless.web.ExtractedArticle
import org.migor.feedless.web.FetchOptions
import org.migor.feedless.web.WebToArticleTransformer
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.ResourceUtils.isUrl
import java.io.ByteArrayInputStream
import java.util.*

@Service
class FulltextPlugin: WebDocumentPlugin {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  override fun id(): String = "fulltext"

  override fun executionPriority(): Int = 1

  override fun processWebDocument(corrId: String, webDocument: WebDocumentEntity) {
    val url = webDocument.url

    if (!isUrl(url)) {
      throw HarvestAbortedException("illegal url $url")
    }

    if (isBlacklistedForHarvest(url)) {
      throw HarvestAbortedException("blacklisted $url")
    }

    val canPrerender = arrayOf("text/html", "text/plain").contains(
      httpService.getContentTypeForUrl(
        corrId,
        url
      )
    )

    harvest(corrId, webDocument, false).let {
      if (canPrerender && Jsoup.parse(String(it.responseBody)).select("noscript").isNotEmpty()) {
        log.info("[$corrId] -> prerender (found noscript tag)")
        saveExtractionForContent(corrId, webDocument, it.url, harvest(corrId, webDocument, true))
      } else {
        saveExtractionForContent(corrId, webDocument, it.url, it)
      }
    }
  }

  private fun harvest(
    corrId: String,
    webDocument: WebDocumentEntity,
    shouldPrerender: Boolean = false
  ): HttpResponse {

    val url = webDocument.url

    return if (shouldPrerender) {
      log.debug("[$corrId] fetching prerendered content")
      val options = FetchOptions(
        websiteUrl = url,
        prerender = true,
      )
      val puppeteerResponse = puppeteerService.prerender(corrId, options).blockFirst()!!
      HttpResponse(
        contentType = "text/html",
        url = url,
        responseBody = puppeteerResponse.dataAscii?.toByteArray() ?: puppeteerResponse.dataBase64!!.encodeToByteArray(),
        statusCode = 200
      )
    } else {
      log.debug("[$corrId] fetching static content")
      httpService.guardedHttpResource(corrId, url, 200, listOf("text/"))
      httpService.httpGet(corrId, url, 200)
    }
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
    return when (val contentType = "${mime.type}/${mime.subtype}") {
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

  private fun fromPdf(corrId: String, url: String, response: HttpResponse): ExtractedArticle {
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
    webDocument: WebDocumentEntity,
    url: String,
    httpResponse: HttpResponse
  ) {
    val extractedArticle = extractFromAny(corrId, url, httpResponse)
    extractedArticle.title?.let {
      log.debug("[$corrId] title ${webDocument.contentTitle} -> $it")
      webDocument.contentTitle = it
    }
    StringUtils.trimToNull(extractedArticle.content)?.let {
      log.debug(
        "[$corrId] contentRawMime ${webDocument.contentRawMime} -> ${
          StringUtils.substring(
            extractedArticle.contentMime,
            0,
            100
          )
        }"
      )

      if (extractedArticle.contentMime!!.startsWith("text/html")) {
        val document = HtmlUtil.parseHtml(it, webDocument.url)
        webDocument.contentRaw = StringUtils.trimToNull(document.body().html())
      } else {
        webDocument.contentRaw = it
      }
      webDocument.contentRawMime = extractedArticle.contentMime!!
    }
    log.debug(
      "[$corrId] mainImageUrl ${webDocument.imageUrl} -> ${
        StringUtils.substring(
          extractedArticle.imageUrl,
          0,
          100
        )
      }"
    )
    webDocument.imageUrl = StringUtils.trimToNull(extractedArticle.imageUrl) ?: webDocument.imageUrl
    webDocument.contentText = StringUtils.trimToNull(extractedArticle.contentText)
    webDocument.hasFulltext = StringUtils.isNoneBlank(webDocument.contentRaw)
    log.info("[$corrId] found fulltext ${webDocument.hasFulltext}")
    if (url != webDocument.url) {
      webDocument.aliasUrl = url
    }

    webDocumentDAO.saveFulltextContent(
      webDocument.id,
      webDocument.aliasUrl,
      webDocument.contentTitle,
      webDocument.contentRaw,
      webDocument.contentRawMime,
      webDocument.contentText,
      webDocument.imageUrl,
      Date()
    )

  }
}
