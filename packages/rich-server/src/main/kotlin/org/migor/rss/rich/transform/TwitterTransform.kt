package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.model.SourceType

class TwitterTransform : BaseTransform() {
  override fun canHandle(sourceType: SourceType): Boolean = sourceType == SourceType.TWITTER

  override fun applyTransform(source: Source, entry: SourceEntry, syndEntry: SyndEntry, feeds: List<RichFeed>): SourceEntry {
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
      entry.properties?.let { it1 -> content.putAll(it1) }
      entry.properties = content
    }
    return super.applyTransform(source, entry, syndEntry, feeds)
  }

  private fun selectStats(selector: String, document: Document): Number = document.select(selector).last().parent().text().replace(",","").toBigInteger()

}
