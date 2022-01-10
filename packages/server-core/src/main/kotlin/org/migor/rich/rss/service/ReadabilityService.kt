package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.asynchttpclient.Response
import org.migor.rich.mq.generated.MqAskPrerendering
import org.migor.rich.mq.generated.MqPrerenderingResponse
import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleSource
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.transform.ExtractedArticle
import org.migor.rss.rich.transform.WebToArticleTransformer
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*


@Service
class ReadabilityService {
  private val log = LoggerFactory.getLogger(ReadabilityService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @RabbitListener(queues = [RabbitQueue.prerenderingResult])
  fun listenPrerenderResponse(prerenderResponseJson: String) {
    try {
      val response = JsonUtil.gson.fromJson(prerenderResponseJson, MqPrerenderingResponse::class.java)
      val corrId = response.correlationId
      val article = Optional.ofNullable(articleRepository.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }

      if (response.error) {
        log.error("[${corrId}] Failed to prerender ${response.url}")
      } else {
        articleRepository.save(amendReadability(corrId, article, fromMarkup(corrId, article, response.data)))
      }
    } catch (e: Exception) {
      this.log.error("Cannot handle readability ${e.message}")
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun appendReadability(
    corrId: String,
    article: Article,
    askPrerender: Boolean,
    allowHarvestFailure: Boolean
  ): Article {
    // todo don't do this for twitter

    val contentType = httpService.getContentTypeForUrl(corrId, article.url!!)
    val canPrerender = contentType == "text/html"
    if (!canPrerender && askPrerender) {
      log.warn("[$corrId] Overriding prerender-request, cause contentType=$contentType")
    }

    return if (canPrerender && askPrerender) {
      log.info("[$corrId] trigger prerendering for ${article.url}")
      val askPrerendering = MqAskPrerendering.Builder()
        .setUrl(article.url)
        .setCorrelationId(corrId)
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.askPrerendering, JsonUtil.gson.toJson(askPrerendering))
      article
    } else {
      log.info("[$corrId] extracting from static content for ${article.url}")
      runCatching {
        val response = httpService.httpGet(corrId, article.url!!, 200)
        amendReadability(corrId, article, extractFromAny(corrId, article, contentType, response))
      }.onFailure { log.error("[${corrId}] Failed to extract: ${it.message}") }
        .getOrDefault(article)
    }
  }

  private fun extractFromAny(
    corrId: String,
    article: Article,
    contentType: String?,
    response: Response
  ): ExtractedArticle? {
    return when (contentType) {
      "text/html" -> fromMarkup(corrId, article, response.responseBody)
      "text/plain" -> fromText(corrId, article, response)
      "application/pdf" -> fromPdf(corrId, article, response)
      else -> {
        log.warn("[${corrId}] Cannot extract article from mime $contentType")
        null
      }
    }
  }

  private fun fromText(corrId: String, article: Article, response: Response): ExtractedArticle {
    log.info("[${corrId}] from text")
    val extractedArticle = ExtractedArticle(article.url!!)
    extractedArticle.contentText = StringUtils.trimToNull(response.responseBody)
    return extractedArticle
  }

  private fun fromPdf(corrId: String, article: Article, response: Response): ExtractedArticle {
    log.info("[${corrId}] from pdf")
    response.responseBodyAsStream.use {
      val handler = BodyContentHandler()
      val metadata = Metadata()
      val parser = AutoDetectParser()
      val parseContext = ParseContext()
      parser.parse(response.responseBodyAsStream, handler, metadata, parseContext)

      val extractedArticle = ExtractedArticle(article.url!!)
      extractedArticle.title = metadata.get(TikaCoreProperties.TITLE)
      extractedArticle.contentText = handler.toString().replace("\n|\r|\t", " ")
      return extractedArticle
    }
  }

  private fun fromMarkup(corrId: String, article: Article, markup: String): ExtractedArticle? {
    log.info("[${corrId}] from markup")
    return webToArticleTransformer.extractArticle(markup, article.url!!)
  }

  private fun amendReadability(corrId: String, article: Article, extractedArticle: ExtractedArticle?): Article {
    if (Optional.ofNullable(extractedArticle).isPresent) {
      val readability = extractedArticle!!
      log.info("[$corrId] readability for ${article.url}")
      article.hasReadability = true
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

      article.sourceUsed = ArticleSource.WEBSITE
      log.info("[$corrId] contentText ${article.contentText} -> ${readability.contentText}")
      article.contentText = readability.contentText!!

      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
        .toMutableSet()
      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
      article.tags = tags.toList()

    } else {
      article.hasReadability = false
      log.error("[$corrId] failed readability for ${article.url}")
    }
    article.released = true
    return article
  }
}
