package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.util.*

@Service
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

  @Autowired
  lateinit var streamService: StreamService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  fun getReadability(url: String): Readability {
    if (url.startsWith("https://www.youtube.com")) {
      throw RuntimeException("Unable to extract readability")
    }

    val existing = articleRepository.findByUrl(url)

    if (existing.isPresent) {
      if (existing.get().hasReadability == true) {
        return existing.get().readability!!
      }
      throw RuntimeException("Readability not part of record $url")
    } else {
      log.debug("Fetching readability for $url")
      val response = httpService.httpGet("http://localhost:3000/articles/readability?url=${URLEncoder.encode(url, "utf-8")}")

      if (response.statusCode == 200) {
        return JsonUtil.gson.fromJson(response.responseBody, Readability::class.java)
      }
      throw RuntimeException("Unable to extract readability")
    }
  }

  fun tryCreateArticleFromUrlForBucket(url: String, sourceUrl: String, bucket: Bucket): Boolean {
    try {
      val article = articleRepository.findByUrl(url).orElseGet { createArticle(url, sourceUrl) }

      log.info("${url} (${sourceUrl}) -> ${bucket.id}")
      streamService.addArticleToStream(article, bucket.streamId!!, bucket.ownerId!!, emptyArray())

      return true
    } catch (e: Exception) {
      log.error("Failed tryCreateArticleFromUrlForBucket url=$url bucket=${bucket.id}: ${e.message}")
      return false
    }
  }

  private fun createArticle(url: String, sourceUrl: String): Article {
    val readability = getReadability(url)
    val article = Article()
    article.url = url
    article.title = readability.title
    article.contentHtml = readability.content
    article.content = Optional.ofNullable(readability.textContent).orElse("")
    article.readability = readability
    article.author = readability.byline
    article.tags = arrayOf("seed")
    article.released = true
    article.hasReadability = true
    article.sourceUrl = sourceUrl
    article.applyPostProcessors = false
    return article
  }
}

data class Readability(
  val title: String?, val byline: String?, val content: String?, val textContent: String?, val exerpt: String?,
)
