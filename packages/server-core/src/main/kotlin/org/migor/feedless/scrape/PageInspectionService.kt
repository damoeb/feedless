package org.migor.feedless.scrape

import com.google.gson.Gson
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.stereotype.Service
import java.util.*

class PageInspection(
  val sources: Array<Map<String, String>>,
) {

  fun valueOf(field: String): String? = sources.firstNotNullOfOrNull { source -> source[field] }

  companion object {
    const val TITLE = "title"
    const val TYPE = "type"
    const val DESCRIPTION = "description"
    const val LOCALE = "locale"
    const val LANG = "language"
    const val IMAGE_URL = "image"
  }
}

@Service
class PageInspectionService {
  internal val title = PageInspection.TITLE
  internal val type = PageInspection.TYPE
  internal val url = "url"
  internal val author = "author"
  internal val description = PageInspection.DESCRIPTION
  internal val locale = PageInspection.LOCALE
  internal val language = PageInspection.LANG
  internal val updatedAt = "updatedAt"
  internal val publishedAt = "publishedAt"
  internal val publisher = "publisher"
  internal val keywords = "keywords"
  internal val site = "site"
  internal val imageUrl = PageInspection.IMAGE_URL
  internal val audioUrl = "audio"
  internal val videoUrl = "videoUrl"
  internal val commentsUrl = "commentsUrl"
  internal val commentCount = "commentCount"
  internal val isAccessibleForFree = "isAccessibleForFree"

  fun fromDocument(document: Document): PageInspection {
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
          language to (pageLocale?.language ?: ""),
        )
      )
    )
  }

  fun jsonLdOf(document: Document): Map<String, String> {
    return try {
      val script = document.select("script[type=\"application/ld+json\"]")
      val json = Gson().fromJson<Map<String, Any>>(script.html(), Map::class.java)

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
