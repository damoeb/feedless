package org.migor.rss.rich.mq

import org.jsoup.Jsoup
import org.migor.rss.rich.config.EventType
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

data class ReadabilityMessage(val url: String? = null, val readability: Readability? = null)

data class Readability(
  val title: String?, val byline: String?, val content: String?, val textContent: String?, val exerpt: String?,
)


@Service
class ReadabilityService {
  private val log = LoggerFactory.getLogger(ReadabilityService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @RabbitListener(queues = arrayOf(EventType.readabilityParsed))
  fun listenReadabilityParsed(readabilityJson: String) {
    val message = JsonUtil.gson.fromJson(readabilityJson, ReadabilityMessage::class.java)
    log.debug("Fetched readability for ${message.url}")
    val article = articleRepository.findByUrl(message.url!!).orElseThrow()

    article.readability = withAbsUrls(message.url, message.readability!!)
    article.hasReadability = true
    val tags = Optional.ofNullable(article.tags).orElse(emptyList())
      .toMutableSet()
    tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
    article.tags = tags.toList()
    articleRepository.save(article);
  }

  private fun withAbsUrls(url: String, readability: Readability): Readability {
    val html = Jsoup.parse(readability.content)

    html
      .select("[href]")
      .forEach { element -> element.attr("href", absUrl(url, element.attr("href"))) }

    return Readability(
      title = readability.title,
      content = html.html(),
      textContent = readability.textContent,
      byline = readability.byline,
      exerpt = readability.exerpt
    )
  }
}
