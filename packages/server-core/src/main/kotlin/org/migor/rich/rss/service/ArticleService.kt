package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleContentEntity
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.SiteHarvestEntity
import org.migor.rich.rss.database.models.Stream2ArticleEntity
import org.migor.rich.rss.database.repositories.ArticleContentDAO
import org.migor.rich.rss.database.repositories.AttachmentDAO
import org.migor.rich.rss.database.repositories.SiteHarvestDAO
import org.migor.rich.rss.database.repositories.Stream2ArticleDAO
import org.migor.rich.rss.service.SiteHarvestService.Companion.isBlacklistedForHarvest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.util.*

@Service
@Profile("database")
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

//  @Autowired
//  lateinit var streamService: StreamService

  @Autowired
  lateinit var articleContentDAO: ArticleContentDAO

  @Autowired
  lateinit var stream2ArticleDAO: Stream2ArticleDAO

  @Autowired
  lateinit var attachmentDAO: AttachmentDAO

  @Autowired
  lateinit var siteHarvestDAO: SiteHarvestDAO

//  companion object {
//    private fun getLinkCountFromHtml(article: ArticleEntity, html: String): Int {
//      return Jsoup.parse(html).body().select("a[href]")
//        .map { a -> absUrl(article.url!!, a.attr("href")) }
//        .toSet()
//        // todo mag remove mailto:
//        .count()
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
  fun create(corrId: String, article: ArticleContentEntity, feed: NativeFeedEntity? = null): ArticleContentEntity {
    val attachments = article.attachments
    article.attachments = emptyList()
    val savedArticle = articleContentDAO.save(article)

    attachments?.let {
      attachmentDAO.saveAll(attachments.map { attachment ->
        run {
          attachment.article = savedArticle
          attachment
        }
      })
    }

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
  fun update(corrId: String, article: ArticleContentEntity): ArticleContentEntity {
    return articleContentDAO.save(article)
  }

  @Transactional(readOnly = true)
  fun findAllByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): Page<ArticleContentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))
    return articleContentDAO.findAllByStreamId(streamId, type, status, pageable)
  }

  @Transactional(readOnly = true)
  fun findAllByStreamId2(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): Page<Stream2ArticleEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "releasedAt"))
    return stream2ArticleDAO.findAllByStreamId(streamId, type, status, pageable)
  }

  @Transactional(readOnly = true)
  fun findByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): Page<RichArticle> {
    return findAllByStreamId(streamId, page, type, status)
      .map { article ->
        RichArticle(
          id = article.id.toString(),
          title = article.title!!,
          url = article.url!!,
          tags = getTags(article),
          enclosures = emptyToNull(article.attachments)?.map { a -> RichEnclosure(a.length, a.mimeType!!, a.url!!) },
          contentText = article.contentText!!,
          contentRaw = contentToString(article),
          contentRawMime = article.contentRawMime,
          publishedAt = article.publishedAt!!,
          imageUrl = article.imageUrl
        )
      }
  }

  private fun getTags(article: ArticleContentEntity): List<String>? {
    val tags = mutableListOf<String>()
    if (article.hasFulltext) {
      tags.add("fulltext")
    }
    article.contentText?.let {
      if (it.length <= 140) {
        tags.add("short")
      }
    }
    article.attachments?.let {
      val mainTypes = it.map {
        it.mimeType
      }.plus(article.contentRawMime)
        .filterNotNull()
        .map { MimeType.valueOf(it).type }
        .distinct()

      if (mainTypes.contains("video")) {
        tags.add("video")
      }
      if (mainTypes.contains("audio")) {
        tags.add("audio")
      }
    }
    // todo mag add from url
    return emptyToNull(tags.distinct())
  }

  private fun <T> emptyToNull(list: List<T>?): List<T>? {
    return if (list.isNullOrEmpty()) {
      null
    } else {
      list
    }
  }

  private fun contentToString(article: ArticleContentEntity): String? {
    return if (StringUtils.startsWith(article.contentRawMime, "text")) {
      article.contentRaw!!
    } else {
      null
    }
  }

  fun findById(id: UUID): Optional<ArticleContentEntity> {
    return articleContentDAO.findById(id)
  }
}
