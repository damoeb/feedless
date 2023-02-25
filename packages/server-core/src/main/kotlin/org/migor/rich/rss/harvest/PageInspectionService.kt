package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.JsonUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

class PageInspection(
  val sources: Array<Map<String, String>>,
) {

  fun valueOf(field: String): String? = sources.mapNotNull { source -> source[field] }.firstOrNull()
}

@Service
class PageInspectionService {
  internal val title = "title"
  internal val type = "type"
  internal val url = "url"
  internal val author = "author"
  internal val description = "description"
  internal val locale = "locale"
  internal val language = "language"
  internal val updatedAt = "updatedAt"
  internal val publishedAt = "publishedAt"
  internal val publisher = "publisher"
  internal val keywords = "keywords"
  internal val site = "site"
  internal val imageUrl = "image"
  internal val audioUrl = "audio"
  internal val videoUrl = "videoUrl"
  internal val commentsUrl = "commentsUrl"
  internal val commentCount = "commentCount"
  internal val isAccessibleForFree = "isAccessibleForFree"

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  fun fromDocument(document: Document): PageInspection {
    val article = webToArticleTransformer.fromDocument(document, "")
    article.imageUrl
    val pageLocale: Locale? = document.select("html[lang]")
      .map {
        runCatching { Locale(it.attr("lang").split("_", "-")[0]) }
          .getOrNull()
      }
      .firstOrNull()
    return PageInspection(
      arrayOf(
        jsonLdOf(document),
        openGraphTagsOf(document),
        twitterTagsOf(document),
        microDataTagsOf(document),
        metaTagsOf(document),
        mapOf(
          title to document.title(),
          language to Optional.ofNullable(pageLocale).map { it.language }.orElse(""),
          imageUrl to Optional.ofNullable(article.imageUrl).orElse("")
        )
      )
    )
  }

  fun jsonLdOf(document: Document): Map<String, String> {
    return try {
      val script = document.select("script[type=\"application/ld+json\"]")
      val json = JsonUtil.gson.fromJson<Map<String, Any>>(script.html(), Map::class.java)

      return mapOf(
        title to json["headline"] as String,
        updatedAt to json["dateModified"] as String,
        publishedAt to json["datePublished"] as String,
        description to json["description"] as String,
        commentsUrl to json["discussionUrl"] as String,
        imageUrl to json["image"] as String,
        keywords to json["keywords"] as String,
        commentCount to json["commentCount"] as String,
        isAccessibleForFree to json["isAccessibleForFree"] as String,
        imageUrl to json["thumbnailUrl"] as String,
      )
    } catch (e: Exception) {
      emptyMap()
    }
  }

  fun microDataTagsOf(document: Document): Map<String, String> {
    val fields = listOf(
      imageUrl to "image",
      title to "headline",
      publisher to "publisher",
      keywords to "keywords",
      description to "description",
      publishedAt to "datePublished",
      updatedAt to "dateModified",
    )
    val select = { name: String -> document.select("meta[itemprop=\"${name}\"]") }
    return asMap(
      fields, select
    )
  }

  fun openGraphTagsOf(document: Document): Map<String, String> {
    val fields = listOf(
      title to "og:title",
      type to "og:type",
      url to "og:url",
      imageUrl to "og:image",
      audioUrl to "og:audio",
      description to "og:description",
      locale to "og:locale",
      videoUrl to "og:video",
      updatedAt to "og:updated_time"
    )
    val select = { name: String -> document.select("meta[property=\"${name}\"]") }
    return asMap(
      fields, select
    )
  }

  fun metaTagsOf(document: Document): Map<String, String> {
    val fields = listOf(
      publishedAt to "date",
      author to "author",
      title to "title",
      keywords to "keywords",
      description to "description",
      publishedAt to "datePublished",
      updatedAt to "dateModified",
    )
    val select = { name: String -> document.select("meta[name=\"${name}\"]") }
    return asMap(
      fields, select
    )

  }

  fun twitterTagsOf(document: Document): Map<String, String> {
    val fields = listOf(
      description to "twitter:description",
      imageUrl to "twitter:image",
      site to "twitter:site",
      title to "twitter:title",
    )

    val select = { name: String -> document.select("meta[property=\"${name}\"]") }
    return asMap(
      fields, select
    )
  }

  private fun asMap(fields: List<Pair<String, String>>, valueForSelector: (String) -> Elements): Map<String, String> {
    val pairs = fields.map { pair -> Pair(pair.first, valueForSelector(pair.second).attr("content")) }
      .filter { pair -> StringUtils.isNotBlank(pair.second) }
    return mapOf(*pairs.toTypedArray())
  }

}
