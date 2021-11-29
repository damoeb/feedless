package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.feed.synd.SyndEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestContext
import org.migor.rss.rich.service.PropertyService
import org.migor.rss.rich.util.CryptUtil
import org.migor.rss.rich.util.FeedUtil.cleanMetatags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class TwitterFeedResolver : FeedContextResolver {

  @Autowired
  lateinit var propertyService: PropertyService

  override fun priority(): Int {
    return 1
  }

  override fun canHarvest(feed: Feed): Boolean {
    return feed.feedUrl!!.startsWith("https://twitter.com")
  }

  override fun getHarvestContexts(corrId: String, feed: Feed): List<HarvestContext> {
    val url = feed.feedUrl!!.replace("https://twitter.com", propertyService.nitterHost!!)
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    val proxy = "${propertyService.host}/api/rss-proxy/atom?url=${
      URLEncoder.encode(
        url,
        StandardCharsets.UTF_8
      )
    }&linkXPath=.%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%2Fdiv%5B1%5D%2Fspan%5B1%5D%2Fa%5B1%5D&extendContext=n&contextXPath=%2F%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv&correlationId=$branchedCorrId"

    return listOf(
      HarvestContext("$url/rss"),
      HarvestContext(proxy)
    )
  }

  override fun mergeFeeds(feedData: List<FeedData>): List<Pair<SyndEntry, Article>> {
    val first = feedData[0].feed.entries
    val second = feedData[1].feed.entries
    return first.filterNotNull().map { article -> mergeArticles(article, second) }
  }

  private fun mergeArticles(entry: SyndEntry, second: List<SyndEntry>): Pair<SyndEntry, Article> {
    val article = Article()
    runCatching {
      second.filter { otherEntry -> sameUrlIgnoringHost(entry.link, otherEntry.link) }
        .forEach { matchingEntry -> applyTransform(article, matchingEntry) }
    }

    return Pair(entry, article)
  }

  private fun sameUrlIgnoringHost(urlA: String, urlB: String): Boolean {
    return URL(urlA).path == URL(urlB).path
  }

  private fun applyTransform(article: Article, syndEntry: SyndEntry): Article {

    val syndContent = syndEntry.contents?.get(0)
    syndContent?.let {
      val document = Jsoup.parse(cleanMetatags(it.value))
      mapOf(
        "rr:comments" to selectStats(".icon-comment", document),
        "rr:retweets" to selectStats(".icon-retweet", document),
        "rr:quotes" to selectStats(".icon-quote", document),
        "rr:hearts" to selectStats(".icon-heart", document)
      ).forEach { (key, value) -> article.putDynamicField("stats", key, value) }
    }
    return article
  }

  private fun selectStats(selector: String, document: Document): Number =
    document.select(selector).last().parent().text().replace(",", "").toBigInteger()
}
