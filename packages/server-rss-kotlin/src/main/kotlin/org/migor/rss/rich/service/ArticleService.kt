package org.migor.rss.rich.service

import org.jsoup.Jsoup
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.util.HtmlUtil
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

  companion object {
    private fun getLinkCountFromHtml(article: Article, html: String): Int {
      return Jsoup.parse(html).body().select("a[href]")
        .map { a -> absUrl(article.url!!, a.attr("href")) }
        .toSet()
        // todo mag remove mailto:
        .count()
    }

    fun getLinkCount(article: Article): Int {
      val content = listOf(
        article.readability?.content,
        article.contentHtml
      )
        .filterNotNull()
        .firstOrNull()
      return if (content != null) {
        getLinkCountFromHtml(article, content)
      } else 0
    }
  }

  fun getReadability(url: String): Readability {
    if (url.startsWith("https://www.youtube.com")) {
      throw RuntimeException("Unable to extract readability")
    }

    val existing = articleRepository.findByUrl(url)

    if (existing.isPresent) {
      if (existing.get().hasReadability == true) {
        return withAbsUrls(url, existing.get().readability!!)
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

  private fun withAbsUrls(url: String, readability: Readability): Readability {
    val html = Jsoup.parse(readability.content)

    html
      .select("[href]")
      .forEach { element -> element.attr("href", absUrl(url, element.attr("href"))) }

    return Readability(
      title = readability.title,
      content = html.html(),
      textContent = readability.textContent,
      byline = readability.byline,
      exerpt = readability.exerpt
    )
  }

  fun tryCreateArticleFromContainedUrlForBucket(url: String, sourceUrl: String, bucket: Bucket): Boolean {
    try {
      val article = articleRepository.findByUrl(url).orElseGet { createArticle(url, sourceUrl) }

      log.info("${url} (${sourceUrl}) -> ${bucket.id}")
      streamService.addArticleToStream(article, bucket.streamId!!, bucket.ownerId!!, emptyList())

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
    article.contentHtml = HtmlUtil.cleanHtml(readability.content)
    article.content = HtmlUtil.html2text(Optional.ofNullable(readability.textContent).orElse(""))!!
    article.readability = readability
    article.author = readability.byline
    article.tags = emptyList()
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
