package org.migor.rich.rss.service

import org.jsoup.Jsoup
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.service.FeedService.Companion.absUrl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database")
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

//  @Autowired
//  lateinit var streamService: StreamService

  @Autowired
  lateinit var readabilityService: ReadabilityService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  companion object {
    private fun getLinkCountFromHtml(article: Article, html: String): Int {
      return Jsoup.parse(html).body().select("a[href]")
        .map { a -> absUrl(article.url!!, a.attr("href")) }
        .toSet()
        // todo mag remove mailto:
        .count()
    }

    fun getLinkCount(article: Article): Int {
      return Optional.ofNullable(article.getContentOfMime("text/html"))
        .map { contentHtml -> getLinkCountFromHtml(article, contentHtml) }
        .orElse(0)
    }
  }

  fun triggerContentEnrichment(corrId: String, article: Article, feed: Feed): Article {
    return if (feed.harvestSite) {
      readabilityService.appendReadability(corrId, article, feed.harvestPrerender, feed.allowHarvestFailure)
    } else {
      article
    }
  }

//  @RabbitListener(queues = [RabbitQueue.articleChanged])
//  fun listenArticleChange(articleChangeJson: String) {
//    try {
//      val change = JsonUtil.gson.fromJson(articleChangeJson, MqArticleChange::class.java)
//      val url = change.url
//      val reason = change.reason
//
//      // todo article may be released
//      // todo mag fix subscription updated at, so bucket filling will be after articles are scored
//      log.info("[${change.correlationId}] articleChange for $url $reason")
//    } catch (e: Exception) {
//      this.log.error("Cannot handle articleChange ${e.message}")
//    }
//  }

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

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  fun save(article: Article): Article {
    return articleRepository.save(article)
  }
}
