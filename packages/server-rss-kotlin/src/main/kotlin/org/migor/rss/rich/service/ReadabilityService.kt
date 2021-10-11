package org.migor.rss.rich.service

import org.jsoup.Jsoup
import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.generated.MqAskReadability
import org.migor.rss.rich.generated.MqReadability
import org.migor.rss.rich.generated.MqReadabilityData
import org.migor.rss.rich.service.FeedService.Companion.absUrl
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

  @RabbitListener(queues = arrayOf(RabbitQueue.readability))
  fun listenReadabilityParsed(readabilityJson: String) {
    try {
      val readability = JsonUtil.gson.fromJson(readabilityJson, MqReadability::class.java)
      log.info("+ readability for ${readability.url} error=${readability.error}")
      val article = articleRepository.findByUrl(readability.url!!).orElseThrow { throw IllegalArgumentException("Article ${readability?.url} not found") }

      if (readability.error) {
        article.hasReadability = false
      } else {
        article.readability = withAbsUrls(readability.url, readability.readability!!)
        article.hasReadability = true
        val tags = Optional.ofNullable(article.tags).orElse(emptyList())
          .toMutableSet()
        tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
        article.tags = tags.toList()
      }

      articleRepository.save(article)

      rabbitTemplate.convertAndSend(RabbitQueue.articleChanged, JsonUtil.gson.toJson(arrayOf(article.url!!, "readability")))

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
      readability.exerpt
    )
  }

  fun askForReadability(article: Article) {
    rabbitTemplate.convertAndSend(RabbitQueue.askReadability, JsonUtil.gson.toJson(MqAskReadability.Builder().setUrl(article.url).build()))
  }
}
