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

    val meta = MarkupInspector.fromDocument(document)
    return mapOf(
      "_" to mapOf(
        "schema" to MarkupInspector.jsonLdOf(document),
        "og" to MarkupInspector.openGraphTagsOf(document),
        "micro" to MarkupInspector.microDataTagsOf(document),
        "meta" to MarkupInspector.metaTagsOf(document)
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

    val meta = MarkupInspector.fromDocument(document)

    val article = if (articleRecovery == ArticleRecovery.FULL) {
      webToArticleTransformer.fromDocument(document, unresolved.url)
    } else {
      null
    }

    return ArticleJsonDto(
      id = unresolved.id,
      title = or(meta.valueOf(MarkupInspector.title), unresolved.title)!!,
      tags = Optional.ofNullable(meta.valueOf(MarkupInspector.keywords)).map {
        StringUtils.split(it, ",").asList().mapNotNull { kw -> kw.trim() }
      }.orElse(emptyList()),
      content_text = listOfNotNull(
        article?.contentText,
        meta.valueOf(MarkupInspector.description),
        unresolved.content_text,
        ""
      ).firstOrNull()!!,
      content_raw = listOfNotNull(article?.content, unresolved.content_raw).firstOrNull(),
      content_raw_mime = listOfNotNull(article?.contentMime, unresolved.content_raw_mime).firstOrNull(),
      url = unresolved.url,
      author = or(meta.valueOf(MarkupInspector.author), unresolved.author),
      enclosures = mapOf(
        "audio" to meta.valueOf(MarkupInspector.audioUrl),
        "image" to meta.valueOf(MarkupInspector.imageUrl),
        "video" to meta.valueOf(MarkupInspector.videoUrl)
      ).filterValues { it != null }
        .map { EnclosureDto(url = it.value!!, type = it.key, length = 0) },
      date_published = Optional.ofNullable(meta.valueOf(MarkupInspector.publishedAt))
        .map { dateClaimer.claimDateFromString(corrId, it, null) }.orElse(unresolved.date_published)!!,
      commentsFeedUrl = null,
      main_image_url = meta.valueOf(MarkupInspector.imageUrl),
    )
  }

  private fun or(a: String?, b: String?): String? = Optional.ofNullable(a).orElse(b)
  fun shouldRecover(articleRecovery: ArticleRecovery, index: Int) =
    (articleRecovery != ArticleRecovery.NONE && index < propertyService.maxResolutionPerFeed) || articleRecovery == ArticleRecovery.NONE

}
