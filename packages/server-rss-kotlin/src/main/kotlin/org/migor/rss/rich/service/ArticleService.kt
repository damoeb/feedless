package org.migor.rss.rich.service

import org.jsoup.Jsoup
import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.generated.MqArticleChange
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

//  @Autowired
//  lateinit var streamService: StreamService

  @Autowired
  lateinit var readabilityService: ReadabilityService

  @Autowired
  lateinit var scoreService: ScoreService

  companion object {
    private fun getLinkCountFromHtml(article: Article, html: String): Int {
      return Jsoup.parse(html).body().select("a[href]")
        .map { a -> absUrl(article.url!!, a.attr("href")) }
        .toSet()
        // todo mag remove mailto:
        .count()
    }

    fun getLinkCount(article: Article): Int {
      val content = listOfNotNull(
        article.readability?.content,
        article.getHtmlContent()
      )
        .firstOrNull()
      return if (content != null) {
        getLinkCountFromHtml(article, content)
      } else 0
    }
  }

  fun triggerContentEnrichment(corrId: String, article: Article, feed: Feed) {
    if (feed.harvestSite) {
      log.info("[$corrId] trigger content enrichment for ${article.url}")
      readabilityService.askForReadability(corrId, article, feed.harvestPrerender, feed.allowHarvestFailure)
    }
    scoreService.askForScoring(corrId, article)
  }

  @RabbitListener(queues = [RabbitQueue.articleChanged])
  fun listenArticleChange(articleChangeJson: String) {
    try {
      val change = JsonUtil.gson.fromJson(articleChangeJson, MqArticleChange::class.java)
      val url = change.url
      val reason = change.reason

      // todo article may be released
      // todo mag fix subscription updated at, so bucket filling will be after articles are scored
      log.info("[${change.correlationId}] articleChange for $url $reason")
    } catch (e: Exception) {
      this.log.error("Cannot handle articleChange ${e.message}")
    }
  }

  fun tryCreateArticleFromContainedUrlForBucket(url: String, sourceUrl: String, bucket: Bucket): Boolean {
//    todo mag implement
//    try {
//      val article = articleRepository.findByUrl(url).orElseGet { createArticle(url, sourceUrl) }
//
//      log.info("${url} (${sourceUrl}) -> ${bucket.id}")
//      streamService.addArticleToStream(article, bucket.streamId!!, bucket.ownerId!!, emptyList())
//
//      return true
//    } catch (e: Exception) {
//      log.error("Failed tryCreateArticleFromUrlForBucket url=$url bucket=${bucket.id}: ${e.message}")
//      return false
//    }
    return false
  }
}
