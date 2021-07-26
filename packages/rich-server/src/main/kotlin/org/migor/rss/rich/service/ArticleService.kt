package org.migor.rss.rich.service

import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.util.HttpUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.URLEncoder
import java.util.*

@Service
class ArticleService {
  private val log = LoggerFactory.getLogger(ArticleService::class.simpleName)

  fun getReadability(url: String): Readability? {
    log.debug("Fetching readability for ${url}")
    val request = HttpUtil.client.prepareGet("http://localhost:3000/articles/readability?url=${URLEncoder.encode(url, "utf-8")}").execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to $url cause ${e.message}")
    }
    if (response.statusCode == 200) {
      return JsonUtil.gson.fromJson(response.responseBody, Readability::class.java)
    }
    throw RuntimeException("Unable to extract readability")
  }
}

data class Readability(
  val title: String?, val byline: String?, val content: String?,val textContent: String?,val exerpt: String?,
)
