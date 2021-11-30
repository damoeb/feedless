package org.migor.rss.rich.service

import org.jsoup.Jsoup
import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.generated.MqArticleChange
import org.migor.rss.rich.generated.MqAskReadability
import org.migor.rss.rich.generated.MqReadability
import org.migor.rss.rich.generated.MqReadabilityData
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.HtmlUtil.cleanHtml
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReadabilityService {
  private val log = LoggerFactory.getLogger(ReadabilityService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @RabbitListener(queues = [RabbitQueue.readability])
  fun listenReadabilityParsed(readabilityJson: String) {
    try {
      val response = JsonUtil.gson.fromJson(readabilityJson, MqReadability::class.java)
      val corrId = response.correlationId
      val article = Optional.ofNullable(articleRepository.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }

      if (response.harvestFailed) {
        log.error("[${corrId}] readability and harvest for ${response.url} failed")
        article.hasReadability = false
        article.hasHarvest = false
      } else {
        article.hasHarvest = true

        if (response.readabilityFailed) {
          log.error("[${corrId}] readability for ${response.url} failed")
          article.hasReadability = false
          article.contentRaw = cleanHtml(response.contentRaw)
          article.contentRawMime = response.contentRawMime

        } else {
          log.info("[$corrId] readability for ${response.url}")
          article.hasReadability = true
          val readability = withAbsUrls(response.url, response.readability!!)
          article.contentRaw = cleanHtml(readability.content)
          article.contentRawMime = "text/html"
          log.info("[$corrId] contentText ${article.contentText} -> ${readability.textContent}")
          article.contentText = readability.textContent
          log.info("[$corrId] title ${article.title} -> ${readability.title}")
          article.title = readability.title
          log.info("[$corrId] author ${article.author} -> ${readability.byline}")
          article.author = readability.byline

          val tags = Optional.ofNullable(article.tags).orElse(emptyList())
            .toMutableSet()
          tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
          article.tags = tags.toList()
        }
      }

      article.released = response.allowHarvestFailure || !response.harvestFailed

      articleRepository.save(article)

      val reportChange = MqArticleChange.builder()
        .setCorrelationId(corrId)
        .setUrl(article.url!!)
        .setReason("readability")
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.articleChanged, JsonUtil.gson.toJson(reportChange))
    } catch (e: Exception) {
      this.log.error("Cannot handle readability ${e.message}")
    }
  }

  private fun withAbsUrls(url: String, readability: MqReadabilityData): MqReadabilityData {
    val html = Jsoup.parse(readability.content)

    html
      .select("[href]")
      .forEach { element -> element.attr("href", absUrl(url, element.attr("href"))) }

    return MqReadabilityData(
      readability.title,
      readability.byline,
      html.html(),
      readability.textContent,
      readability.excerpt,
    )
  }

  fun askForReadability(corrId: String, article: Article, prerender: Boolean, allowHarvestFailure: Boolean) {
    val askReadability = MqAskReadability.Builder()
      .setUrl(article.url)
      .setCorrelationId(corrId)
      .setPrerender(prerender)
      .setAllowHarvestFailure(allowHarvestFailure)
      .build()
    rabbitTemplate.convertAndSend(RabbitQueue.askReadability, JsonUtil.gson.toJson(askReadability))
  }
}
