package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.migor.rich.rss.util.JsonUtil

class MetatagExtraction(
  val sources: Array<Map<String, String>>,
) {

  fun valueOf(field: String): String? = sources.mapNotNull { source -> source[field] }.firstOrNull()
}

object MarkupInspector {
  const val title = "title"
  const val type = "type"
  const val url = "url"
  const val author = "author"
  const val description = "description"
  const val locale = "locale"
  const val updatedAt = "updatedAt"
  const val publishedAt = "publishedAt"
  const val publisher = "publisher"
  const val keywords = "keywords"
  const val site = "site"
  const val imageUrl = "image"
  const val audioUrl = "audio"
  const val videoUrl = "videoUrl"
  const val commentsUrl = "commentsUrl"
  const val commentCount = "commentCount"
  const val isAccessibleForFree = "isAccessibleForFree"

  fun fromDocument(document: Document): MetatagExtraction {
    return MetatagExtraction(
      arrayOf(
        jsonLdOf(document),
        openGraphTagsOf(document),
        twitterTagsOf(document),
        microDataTagsOf(document),
        metaTagsOf(document),
      )
    )
  }

  fun jsonLdOf(document: Document): Map<String, String> {
    return try {
      val script = document.select("script[type=\"application/ld+json\"]")
      val json = JsonUtil.gson.fromJson<Map<String, Any>>(script.html(), Map::class.java)

      return mapOf(
        title to json["headline"].toString(),
        updatedAt to json["dateModified"].toString(),
        publishedAt to json["datePublished"].toString(),
        description to json["description"].toString(),
        commentsUrl to json["discussionUrl"].toString(),
        imageUrl to json["image"].toString(),
        keywords to json["keywords"].toString(),
        commentCount to json["commentCount"].toString(),
        isAccessibleForFree to json["isAccessibleForFree"].toString(),
        imageUrl to json["thumbnailUrl"].toString(),
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
