package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.migor.rich.rss.config.RabbitQueue
import org.migor.rich.rss.database.model.ArticleSource
import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.generated.MqAskPrerendering
import org.migor.rich.rss.generated.MqPrerenderingResponse
import org.migor.rich.rss.transform.ExtractedArticle
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.util.*


@Service
@Profile("database2")
class ReadabilityService {
  private val log = LoggerFactory.getLogger(ReadabilityService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Autowired
  lateinit var articleDao: ArticleDAO

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @RabbitListener(queues = [RabbitQueue.prerenderingResult])
  fun listenPrerenderResponse(prerenderResponseJson: String) {
    try {
      val response = JsonUtil.gson.fromJson(prerenderResponseJson, MqPrerenderingResponse::class.java)
      val corrId = response.correlationId
      val article = Optional.ofNullable(articleDao.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }

      if (response.error) {
        log.error("[${corrId}] Failed to prerender ${response.url}")
        saveReadability(corrId, article, null)
      } else {
        saveReadability(corrId, article, fromMarkup(corrId, article, response.data))
      }
    } catch (e: Exception) {
      this.log.error("Cannot handle readability ${e.message}")
    }
  }

  fun triggerReadabilityExtraction(
    corrId: String,
    article: ArticleEntity,
    askPrerender: Boolean,
  ) {
    // todo don't do this for twitter

    val contentType = httpService.getContentTypeForUrl(corrId, article.url!!)
    val canPrerender = contentType == "text/html"
    if (!canPrerender && askPrerender) {
      log.warn("[$corrId] Overriding prerender-request, cause contentType=$contentType")
    }

    if (canPrerender && askPrerender) {
      log.info("[$corrId] trigger prerendering for ${article.url}")
      val askPrerendering = MqAskPrerendering.Builder()
        .setUrl(article.url)
        .setCorrelationId(corrId)
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.askPrerendering, JsonUtil.gson.toJson(askPrerendering))
    } else {
      log.info("[$corrId] extracting from static content for ${article.url}")
      runCatching {
        val response = httpService.httpGet(corrId, article.url!!, 200)
        saveReadability(corrId, article, extractFromAny(corrId, article, contentType, response))
      }.onFailure { log.error("[${corrId}] Failed to extract: ${it.message}") }
        .getOrDefault(article)
    }
  }

  private fun extractFromAny(
    corrId: String,
    article: ArticleEntity,
    contentType: String?,
    response: HttpResponse
  ): ExtractedArticle? {
    return when (contentType) {
      "text/html" -> fromMarkup(corrId, article, String(response.responseBody))
      "text/plain" -> fromText(corrId, article, response)
      "application/pdf" -> fromPdf(corrId, article, response)
      else -> {
        log.warn("[${corrId}] Cannot extract article from mime $contentType")
        null
      }
    }
  }

  private fun fromText(corrId: String, article: ArticleEntity, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from text")
    val extractedArticle = ExtractedArticle(article.url!!)
    extractedArticle.contentText = StringUtils.trimToNull(String(response.responseBody))
    return extractedArticle
  }

  private fun fromPdf(corrId: String, article: ArticleEntity, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from pdf")
    ByteArrayInputStream(response.responseBody).use {
      val handler = BodyContentHandler()
      val metadata = Metadata()
      val parser = AutoDetectParser()
      val parseContext = ParseContext()
      parser.parse(it, handler, metadata, parseContext)

      val extractedArticle = ExtractedArticle(article.url!!)
      extractedArticle.title = metadata.get(TikaCoreProperties.TITLE)
      extractedArticle.contentText = handler.toString().replace("\n|\r|\t", " ")
      return extractedArticle
    }
  }

  private fun fromMarkup(corrId: String, article: ArticleEntity, markup: String): ExtractedArticle? {
    log.info("[${corrId}] from markup")
    return webToArticleTransformer.fromHtml(markup, article.url!!)
  }

  private fun saveReadability(corrId: String, article: ArticleEntity, extractedArticle: ExtractedArticle?): ArticleEntity {
    if (Optional.ofNullable(extractedArticle).isPresent) {
      val readability = extractedArticle!!
      log.info("[$corrId] readability for ${article.url}")
      article.hasContent = true
      readability.title?.let {
        log.info("[$corrId] title ${article.title} -> $it")
        article.title = it
      }
      readability.content?.let {
        log.info("[$corrId] contentRawMime ${article.contentRawMime} -> ${readability.contentMime}")
        article.contentRaw = readability.content
        article.contentRawMime = readability.contentMime!!
      }
      log.info("[$corrId] mainImageUrl ${article.mainImageUrl} -> ${readability.imageUrl}")
      article.mainImageUrl = readability.imageUrl

      article.contentSource = ArticleSource.WEBSITE
      log.info("[$corrId] contentText ${article.contentText} -> ${readability.contentText}")
      article.contentText = readability.contentText!!

//      todo mag
//      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
//        .toMutableSet()
//      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
//      article.tags = tags.toList()
      articleDao.saveContent(article.id, article.title, article.contentRaw, article.contentRawMime, article.contentSource, article.contentText, article.mainImageUrl)
    } else {
      article.hasContent = false
      log.error("[$corrId] failed readability for ${article.url}")
    }
    article.released = true
    return article
  }
}
