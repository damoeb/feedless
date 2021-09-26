package org.migor.rss.rich.harvest.feedparser

import com.rometools.rome.feed.synd.SyndEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestContext
import org.migor.rss.rich.service.PropertyService
import org.migor.rss.rich.transform.BaseTransform
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TwitterFeedSupport(val propertyService: PropertyService) : FeedContextResolver, BaseTransform() {

  override fun priority(): Int {
    return 1
  }

  override fun canHarvest(feed: Feed): Boolean {
    return feed.feedUrl!!.startsWith("https://twitter.com")
  }

  override fun getHarvestContexts(feed: Feed): List<HarvestContext> {
    val rssproxy = propertyService.rssProxyUrl()
    val url = feed.feedUrl!!.replace("https://twitter.com", propertyService.nitterUrl())
    val proxy = "${rssproxy}/api/feed?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&rule=DIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3ESPAN%3EA&output=ATOM"
    return listOf(
      HarvestContext("$url/rss"),
      HarvestContext(proxy)
    )
  }

  override fun applyTransform(feed: Feed, article: Article, syndEntry: SyndEntry, feedData: List<FeedData>): Article {
    val linkedSyndEntry = feedData.get(1).feed.entries.find { otherSyndEntry -> otherSyndEntry.link.equals(syndEntry.link) }

    val syndContent = linkedSyndEntry?.contents?.get(0)
    syndContent?.let {
      val document = Jsoup.parse(it.value)
      val stats = mapOf(
        "rr:comments" to selectStats(".icon-comment", document),
        "rr:retweets" to selectStats(".icon-retweet", document),
        "rr:quotes" to selectStats(".icon-quote", document),
        "rr:hearts" to selectStats(".icon-heart", document)
      )
      val content = HashMap<String, Any>(100)
      content.putAll(stats)
//      entry.properties?.let { it1 -> content.putAll(it1) }
//      entry.properties = content
    }
    return super.applyTransform(feed, article, syndEntry, feedData)
  }

  private fun selectStats(selector: String, document: Document): Number = document.select(selector).last().parent().text().replace(",", "").toBigInteger()

}
