package org.migor.rss.rich.service

import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.generated.MqAskPrerendering
import org.migor.rss.rich.generated.MqPrerenderingResponse
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
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @RabbitListener(queues = [RabbitQueue.prerenderResult])
  fun listenPrerenderResponse(prerenderResponseJson: String) {
    try {
      val response = JsonUtil.gson.fromJson(prerenderResponseJson, MqPrerenderingResponse::class.java)
      val corrId = response.correlationId
      val article = Optional.ofNullable(articleRepository.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }

      if (response.error) {
        log.error("[${corrId}] Failed to prerender ${response.url}")
      } else {
        extractReadability(corrId, article, response.data)
      }
    } catch (e: Exception) {
      this.log.error("Cannot handle readability ${e.message}")
    }
  }

  fun appendReadability(corrId: String, article: Article, prerender: Boolean, allowHarvestFailure: Boolean) {
    // todo don't do this for twitter

    if (prerender) {
      log.info("[$corrId] trigger content enrichment for ${article.url}")
      val askPrerendering = MqAskPrerendering.Builder()
        .setUrl(article.url)
        .setCorrelationId(corrId)
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.askPrerender, JsonUtil.gson.toJson(askPrerendering))
    } else {
      val response = httpService.httpGet(corrId, article.url!!, 200)
      extractReadability(corrId, article, response.responseBody)
    }
  }

  private fun extractReadability(corrId: String, article: Article, markup: String) {
    val extractedArticle = webToArticleTransformer.extractArticle(markup, article.url!!)

    if (Optional.ofNullable(extractedArticle).isPresent) {
      val readability = extractedArticle!!
      log.info("[$corrId] readability for ${article.url}")
      article.hasReadability = true
      article.contentRaw = readability.content
      article.contentRawMime = "text/html"
      log.info("[$corrId] contentText ${article.contentText} -> ${readability.contentText}")
      article.contentText = readability.contentText!!
      log.info("[$corrId] title ${article.title} -> ${readability.title}")
      article.title = readability.title
//          log.info("[$corrId] author ${article.author} -> ${readability.byline}")
//          article.author = readability.byline

      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
        .toMutableSet()
      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
      article.tags = tags.toList()

    } else {
      article.hasReadability = false
      log.error("[$corrId] failed readability for ${article.url}")
    }
    article.released = true
    articleService.save(article)

//        val reportChange = MqArticleChange.builder()
//          .setCorrelationId(corrId)
//          .setUrl(article.url!!)
//          .setReason("readability")
//          .build()
//        rabbitTemplate.convertAndSend(RabbitQueue.articleChanged, JsonUtil.gson.toJson(reportChange))

  }
}
