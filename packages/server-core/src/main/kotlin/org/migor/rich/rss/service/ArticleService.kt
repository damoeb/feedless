package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.models.ArticleType
import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.SiteHarvestEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.database2.repositories.SiteHarvestDAO
import org.migor.rich.rss.service.FeedService.Companion.absUrl
import org.migor.rich.rss.service.SiteHarvestService.Companion.isBlacklistedForHarvest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database2")
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

//  @Autowired
//  lateinit var streamService: StreamService

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var siteHarvestDAO: SiteHarvestDAO

  companion object {
    private fun getLinkCountFromHtml(article: ArticleEntity, html: String): Int {
      return Jsoup.parse(html).body().select("a[href]")
        .map { a -> absUrl(article.url!!, a.attr("href")) }
        .toSet()
        // todo mag remove mailto:
        .count()
    }

//    fun getLinkCount(article: ArticleEntity): Int {
//      return Optional.ofNullable(article.getContentOfMime("text/html"))
//        .map { contentHtml -> getLinkCountFromHtml(article, contentHtml) }
//        .orElse(0)
//    }
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

  fun tryCreateArticleFromContainedUrlForBucket(url: String, sourceUrl: String, bucket: BucketEntity): Boolean {
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

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  fun create(corrId: String, article: ArticleEntity, feed: NativeFeedEntity? = null): ArticleEntity {
    val savedArticle = articleDAO.save(article)

    feed?.let {
      if (!isBlacklistedForHarvest(article.url!!) && feed.harvestSite) {
        val siteHarvest = SiteHarvestEntity()
        siteHarvest.article = savedArticle
        siteHarvest.feed = feed
        siteHarvestDAO.save(siteHarvest)
      }
    }

    return savedArticle
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  fun update(corrId: String, article: ArticleEntity): ArticleEntity {
    return articleDAO.save(article)
  }

  fun findByStreamId(streamId: UUID, page: Int, type: ArticleType): Page<RichArticle> {
    val pageable = PageRequest.of(0, 10)
    val pagedResult = articleDAO.findAllByStreamId(streamId, type, pageable)
    val items = pagedResult
      .map { result: Array<Any> -> replacePublishedAt(result[0] as ArticleEntity, result[1] as Date) }
      .map { article -> RichArticle(
        id = article.id.toString(),
        title = article.title!!,
        url = article.url!!,
        author = null, // article.author,
        tags = null, // article.tags?.map { tag -> "${tag.ns}:${tag.tag}" },
        commentsFeedUrl = null,
        contentText = article.contentText!!,
        contentRaw = contentToString(article),
        contentRawMime = article.contentRawMime,
        publishedAt = article.publishedAt!!,
        imageUrl = article.mainImageUrl
      )
      }

    return items
  }

  private fun contentToString(article: ArticleEntity): String? {
    return if (StringUtils.startsWith(article.contentRawMime, "text")) {
      article.contentRaw!!
    } else {
      null
    }
  }

  private fun replacePublishedAt(article: ArticleEntity, publishedAt: Date): ArticleEntity {
    article.publishedAt = publishedAt
    return article
  }

}
