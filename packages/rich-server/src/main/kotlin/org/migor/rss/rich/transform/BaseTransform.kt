package org.migor.rss.rich.transform

import com.rometools.rome.feed.synd.SyndEntry
import org.jsoup.Jsoup
import org.migor.rss.rich.HttpUtil
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceType
import java.net.ConnectException

open class BaseTransform : EntryTransform {
  override fun canHandle(sourceType: SourceType): Boolean = true

  override fun applyTransform(source: Source, entry: Entry, syndEntry: SyndEntry, feeds: List<RichFeed>): Entry {
    if (source.withFulltext && source.sourceType != SourceType.TWITTER) {
      val url = entry.link!!
      val analysis = analyze(url)

      val content = HashMap<String, Any>()
      content.putAll(analysis)
      entry.content?.let { content.putAll(it) }
      entry.content = content
    }
    entry.score = calculateScore(entry)
    entry.status = EntryStatus.TRANSFORMED
    return entry
  }

  private fun calculateScore(entry: Entry): Double {
    return 1.0
  }

  private fun analyze(url: String): Map<String, Any> {
    val request = HttpUtil.client.prepareGet(url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to $url cause ${e.message}")
    }

    val document = Jsoup.parse(response.responseBody)
//      document.select()
    // quality, quantity stats, language, pubDate, authors, readability
    return mapOf()
  }

}
