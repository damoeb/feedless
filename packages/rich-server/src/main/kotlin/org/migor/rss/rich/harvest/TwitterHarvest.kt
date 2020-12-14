package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class TwitterHarvest: HarvestStrategy {
  override fun canHarvest(subscription: Subscription): Boolean {
    return subscription.url?.contains("twitter.com")!!
  }

  override fun applyPostTransforms(subscription: Subscription, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
    val linkedSyndEntry = feeds.get(1).feed.entries.find { otherSyndEntry -> otherSyndEntry.link.equals(syndEntry.link) }

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
      entry.content?.let { it1 -> content.putAll(it1) }
      entry.content = content
    }
    return entry
  }

  private fun selectStats(selector: String, document: Document): Number = document.select(selector).last().parent().text().replace(",","").toBigInteger()


  override fun urls(subscription: Subscription): List<HarvestUrl> {
    val url = subscription.url!!.replace("twitter.com", "nitter.net")
    val proxy = "http://localhost:3000/api/feed?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}&rule=DIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3EDIV%3ESPAN%3EA&output=ATOM"
    return listOf(
      HarvestUrl("$url/rss"),
      HarvestUrl(proxy)
    )
  }

  override fun options(): List<HarvestStrategyOption<out Any>> {
    return listOf()
  }

  override fun namespace(): String {
    TODO("Not yet implemented")
  }
}
