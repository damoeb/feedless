package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.EnclosureDto
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.DateClaimer
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.SafeGuards
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
  lateinit var webToArticleTransformer: WebToArticleTransformer

  fun recoverArticle(
    corrId: String,
    url: String,
    articleRecovery: ArticleRecoveryType
  ): ArticleJsonDto = resolveFromSite(corrId, url, articleRecovery)

  private fun shouldRecover(
    articleRecovery: ArticleRecoveryType
  ): Boolean = articleRecovery != ArticleRecoveryType.NONE

  fun resolveMetadata(
    corrId: String,
    url: String,
  ): Map<String, Any> {
    val response = httpService.httpGet(corrId, url, 200)
    val document = Jsoup.parse(SafeGuards.guardedToString(response.responseBodyAsStream))
    document.select("script[type=\"text/javascript\"],.hidden,style").remove()

    val meta = PageInspection.fromDocument(document)
    return mapOf(
      "_" to mapOf(
        "schema" to PageInspection.jsonLdOf(document),
        "og" to PageInspection.openGraphTagsOf(document),
        "micro" to PageInspection.microDataTagsOf(document),
        "meta" to PageInspection.metaTagsOf(document)
      ),
      "aggregated" to meta
    )
  }

  private fun resolveFromSite(
    corrId: String,
    url: String,
    articleRecovery: ArticleRecoveryType
  ): ArticleJsonDto {
    this.log.info("[${corrId}] resolveFromSite url=${url} articleRecovery=${articleRecovery}")
    val response = httpService.httpGet(corrId, url, 200)
    val document = Jsoup.parse(SafeGuards.guardedToString(response.responseBodyAsStream))
    document.select("script[type=\"text/javascript\"],.hidden,style").remove()

    val meta = PageInspection.fromDocument(document)

    val article = if (articleRecovery == ArticleRecoveryType.FULL) {
      webToArticleTransformer.fromDocument(document, url)
    } else {
      null
    }

    return ArticleJsonDto(
      id = url,
      title = Optional.ofNullable(meta.valueOf(PageInspection.title)).orElse("empty"),
      tags = Optional.ofNullable(meta.valueOf(PageInspection.keywords)).map {
        StringUtils.split(it, ",").asList().mapNotNull { kw -> kw.trim() }
      }.orElse(null),
      content_text = listOfNotNull(
        article?.contentText,
        meta.valueOf(PageInspection.description),
        ""
      ).firstOrNull()!!,
      content_raw = article?.content,
      content_raw_mime = article?.contentMime,
      url = url,
      author = PageInspection.author,
      enclosures = mapOf(
        "audio" to meta.valueOf(PageInspection.audioUrl),
        "image" to meta.valueOf(PageInspection.imageUrl),
        "video" to meta.valueOf(PageInspection.videoUrl)
      ).filterValues { it != null }
        .map { EnclosureDto(url = it.value!!, type = it.key, length = 0) },
      date_published = Optional.ofNullable(meta.valueOf(PageInspection.publishedAt))
        .map { dateClaimer.claimDateFromString(corrId, it, null) }.orElse(Date())!!,
      main_image_url = meta.valueOf(PageInspection.imageUrl),
    )
  }

  fun shouldRecover(articleRecovery: ArticleRecoveryType, index: Int) =
    (articleRecovery != ArticleRecoveryType.NONE && index < propertyService.maxResolutionPerFeed) || articleRecovery == ArticleRecoveryType.NONE

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
    article: ArticleJsonDto,
    articleRecovery: ArticleRecoveryType
  ): ArticleJsonDto {
    return if (shouldRecover(articleRecovery)) {
       recoverArticle(corrId, article.url, articleRecovery)
    } else {
      article
    }
  }
}
