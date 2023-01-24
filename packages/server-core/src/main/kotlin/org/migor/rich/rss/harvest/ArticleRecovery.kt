package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.DateClaimer
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleRecovery {

  private val log = LoggerFactory.getLogger(ArticleRecovery::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var dateClaimer: DateClaimer

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var pageInspectionService: PageInspectionService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Value("\${app.maxRecoveryPerFeed}")
  var maxRecoveryPerFeed: Int = 4

  fun recoverArticle(
    corrId: String,
    url: String,
    articleRecovery: ArticleRecoveryType
  ): RichArticle = resolveFromSite(corrId, url, articleRecovery)

  private fun shouldRecover(
    articleRecovery: ArticleRecoveryType
  ): Boolean = articleRecovery != ArticleRecoveryType.NONE

  fun resolveMetadata(
    corrId: String,
    url: String,
  ): Map<String, Any> {
    val response = httpService.httpGetCaching(corrId, url, 200)
    val document = Jsoup.parse(String(response.responseBody))
    document.select("script[type=\"text/javascript\"],.hidden,style").remove()

    val meta = pageInspectionService.fromDocument(document)
    return mapOf(
      "_" to mapOf(
        "schema" to pageInspectionService.jsonLdOf(document),
        "og" to pageInspectionService.openGraphTagsOf(document),
        "micro" to pageInspectionService.microDataTagsOf(document),
        "meta" to pageInspectionService.metaTagsOf(document)
      ),
      "aggregated" to meta
    )
  }

  private fun resolveFromSite(
    corrId: String,
    url: String,
    articleRecovery: ArticleRecoveryType
  ): RichArticle {
    this.log.info("[${corrId}] resolveFromSite url=${url} articleRecovery=${articleRecovery}")
    val response = httpService.httpGetCaching(corrId, url, 200)
    val document = Jsoup.parse(String(response.responseBody))
    document.select("script[type=\"text/javascript\"],.hidden,style").remove()

    val meta = pageInspectionService.fromDocument(document)

    val article = if (articleRecovery == ArticleRecoveryType.FULL) {
      webToArticleTransformer.fromDocument(document, url)
    } else {
      null
    }

    val richArticle = RichArticle()
    richArticle.id = url
    richArticle.title = Optional.ofNullable(meta.valueOf(pageInspectionService.title)).orElse("empty")
    richArticle.tags = Optional.ofNullable(meta.valueOf(pageInspectionService.keywords)).map {
      StringUtils.split(it, ",").asList().mapNotNull { kw -> kw.trim() }
    }.orElse(null)
    richArticle.contentText = listOfNotNull(
      article?.contentText,
      meta.valueOf(pageInspectionService.description),
      ""
    ).firstOrNull()!!
    richArticle.contentRaw = article?.content
    richArticle.contentRawMime = article?.contentMime
    richArticle.url = url
// todo   richArticle.author = PageInspection.author
    richArticle.attachments = mapOf(
      "audio" to meta.valueOf(pageInspectionService.audioUrl),
      "image" to meta.valueOf(pageInspectionService.imageUrl),
      "video" to meta.valueOf(pageInspectionService.videoUrl)
    ).filterValues { it != null }
      .map { RichEnclosure(url = it.value!!, type = it.key, length = 0) }
    richArticle.publishedAt = Optional.ofNullable(meta.valueOf(pageInspectionService.publishedAt))
      .map { dateClaimer.claimDatesFromString(corrId, it, null) }.orElse(Date())!!
    richArticle.imageUrl = meta.valueOf(pageInspectionService.imageUrl)

    return richArticle
  }

  fun shouldRecover(articleRecovery: ArticleRecoveryType, index: Int) =
    (articleRecovery != ArticleRecoveryType.NONE && index < maxRecoveryPerFeed) || articleRecovery == ArticleRecoveryType.NONE

  fun resolveArticleRecovery(articleResolution: String?): ArticleRecoveryType {
    val fallback = ArticleRecoveryType.NONE
    // todo check with featureflags
    return runCatching {
      Optional.ofNullable(articleResolution)
        .map { ArticleRecoveryType.valueOf(it.uppercase()) }
        .orElse(fallback)
    }.getOrElse { fallback }
  }

//  fun recoverAndMerge(corrId: String, syndEntry: SyndEntry, articleRecovery: ArticleRecoveryType): SyndEntry {
//    if (shouldRecover(articleRecovery)) {
//      val recovered = recoverArticle(corrId, syndEntry.link, articleRecovery)
//      syndEntry.title = recovered.title
//    }
//    return syndEntry
//  }

  fun recoverAndMerge(
    corrId: String,
    article: RichArticle,
    articleRecovery: ArticleRecoveryType
  ): RichArticle {
    return if (shouldRecover(articleRecovery)) {
      recoverArticle(corrId, article.url, articleRecovery)
    } else {
      article
    }
  }
}
