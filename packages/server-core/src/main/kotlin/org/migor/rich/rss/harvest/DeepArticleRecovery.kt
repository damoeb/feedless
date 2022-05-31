package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.EnclosureDto
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.transform.DateClaimer
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeepArticleRecovery {

  private val log = LoggerFactory.getLogger(DeepArticleRecovery::class.simpleName)

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
    article: ArticleJsonDto,
    articleRecovery: ArticleRecovery
  ): ArticleJsonDto {
    return when (articleRecovery) {
      ArticleRecovery.NONE -> article
      else -> resolveFromSite(corrId, article, articleRecovery)
    }
  }

  fun resolveMetadata(
    corrId: String,
    url: String,
  ): Map<String, Any> {
    val response = httpService.httpGet(corrId, url, 200)
    val document = Jsoup.parse(response.responseBody)
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
    unresolved: ArticleJsonDto,
    articleRecovery: ArticleRecovery
  ): ArticleJsonDto {
    this.log.info("[${corrId}] resolveFromSite url=${unresolved.url} articleRecovery=${articleRecovery}")
    val response = httpService.httpGet(corrId, unresolved.url, 200)
    val document = Jsoup.parse(response.responseBody)
    document.select("script[type=\"text/javascript\"],.hidden,style").remove()

    val meta = PageInspection.fromDocument(document)

    val article = if (articleRecovery == ArticleRecovery.FULL) {
      webToArticleTransformer.fromDocument(document, unresolved.url)
    } else {
      null
    }

    return ArticleJsonDto(
      id = unresolved.id,
      title = or(meta.valueOf(PageInspection.title), unresolved.title)!!,
      tags = Optional.ofNullable(meta.valueOf(PageInspection.keywords)).map {
        StringUtils.split(it, ",").asList().mapNotNull { kw -> kw.trim() }
      }.orElse(null),
      content_text = listOfNotNull(
        article?.contentText,
        meta.valueOf(PageInspection.description),
        unresolved.content_text,
        ""
      ).firstOrNull()!!,
      content_raw = listOfNotNull(article?.content, unresolved.content_raw).firstOrNull(),
      content_raw_mime = listOfNotNull(article?.contentMime, unresolved.content_raw_mime).firstOrNull(),
      url = unresolved.url,
      author = or(meta.valueOf(PageInspection.author), unresolved.author),
      enclosures = mapOf(
        "audio" to meta.valueOf(PageInspection.audioUrl),
        "image" to meta.valueOf(PageInspection.imageUrl),
        "video" to meta.valueOf(PageInspection.videoUrl)
      ).filterValues { it != null }
        .map { EnclosureDto(url = it.value!!, type = it.key, length = 0) },
      date_published = Optional.ofNullable(meta.valueOf(PageInspection.publishedAt))
        .map { dateClaimer.claimDateFromString(corrId, it, null) }.orElse(unresolved.date_published)!!,
      commentsFeedUrl = null,
      main_image_url = meta.valueOf(PageInspection.imageUrl),
    )
  }

  private fun or(a: String?, b: String?): String? = Optional.ofNullable(a).orElse(b)
  fun shouldRecover(articleRecovery: ArticleRecovery, index: Int) =
    (articleRecovery != ArticleRecovery.NONE && index < propertyService.maxResolutionPerFeed) || articleRecovery == ArticleRecovery.NONE

}
