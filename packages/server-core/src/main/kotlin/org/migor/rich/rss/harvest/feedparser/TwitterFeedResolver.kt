package org.migor.rich.rss.harvest.feedparser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichtFeed
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.harvest.HarvestContext
import org.migor.rich.rss.service.FeedService.Companion.absUrl
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.FeedUtil.cleanMetatags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class TwitterFeedResolver : FeedContextResolver {

  @Autowired
  lateinit var propertyService: PropertyService

//  @Autowired
//  lateinit var scoreService: ScoreService

  override fun priority(): Int {
    return 1
  }

  override fun canHarvest(feed: Feed): Boolean {
    return feed.feedUrl!!.startsWith("https://twitter.com")
  }

  override fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext> {
    val url = feed.feedUrl!!.replace("https://twitter.com", propertyService.nitterHost)
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    val proxy = "${propertyService.host}/api/web-to-feed/atom?url=${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }&version=${propertyService.webToFeedVersion}&linkXPath=.%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%2Fdiv%5B1%5D%2Fspan%5B1%5D%2Fa%5B1%5D&extendContext=n&contextXPath=%2F%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv&correlationId=$branchedCorrId"

    return listOf(
      HarvestContext("$url/rss", feed),
      HarvestContext(proxy, feed)
    )
  }

  override fun mergeFeeds(feeds: List<RichtFeed>): List<Pair<RichArticle, Article>> {
    val first = feeds[0].items
    val second = feeds[1].items
    return first.filterNotNull().map { article -> mergeArticles(article, second) }
  }

  private fun mergeArticles(entry: RichArticle, second: List<RichArticle>): Pair<RichArticle, Article> {
    val article = Article()
    runCatching {
      second.filter { otherEntry -> sameUrlIgnoringHost(entry.url, otherEntry.url) }
        .forEach { matchingEntry -> applyTransform(article, matchingEntry) }
    }

    return Pair(entry, article)
  }

  private fun sameUrlIgnoringHost(urlA: String, urlB: String): Boolean {
    return URL(urlA).path == URL(urlB).path
  }

  private fun applyTransform(article: Article, syndEntry: RichArticle): Article {

    syndEntry.contentRaw?.let {
      val contentRaw = cleanMetatags(it)
      val document = Jsoup.parse(contentRaw)
      document.body().select("a[href]")
        .map { link -> absUrl(article.url!!, link.attr("href")) }
      article.contentRaw = document.body().html()
      article.contentRawMime = "text/html"

      val commentCount = selectStats(".icon-comment", document)
      val quotesCount = selectStats(".icon-quote", document)
      val retweetCount = selectStats(".icon-retweet", document)
      val heartsCount = selectStats(".icon-heart", document)

      mapOf(
        "twitter:comments" to commentCount,
        "twitter:retweets" to retweetCount,
        "twitter:quotes" to quotesCount,
        "twitter:hearts" to heartsCount,
      ).forEach { (key, value) -> article.putDynamicField("stats", key, Optional.ofNullable(value).orElse(0)) }

//      article.likesCount = retweetCount + heartsCount
//      article.engagementsCount = commentCount + quotesCount
    }
    return article
  }

  private fun selectStats(selector: String, document: Document): Int? =
    document.select(selector).last()?.parent()?.text()?.replace(",", "")?.toInt()
}
