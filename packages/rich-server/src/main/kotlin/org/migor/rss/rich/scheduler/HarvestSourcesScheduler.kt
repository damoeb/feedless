package org.migor.rss.rich.scheduler

import com.rometools.rome.feed.synd.SyndEntry
import org.apache.tika.langdetect.TextLangDetector
import org.migor.rss.rich.FeedUtil
import org.migor.rss.rich.HttpUtil
import org.migor.rss.rich.feed.*
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.*
import org.migor.rss.rich.repository.SourceEntryRepository
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.service.SourceService
import org.migor.rss.rich.transform.BaseTransform
import org.migor.rss.rich.transform.EntryTransform
import org.migor.rss.rich.transform.TwitterTransform
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.net.ConnectException
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.collections.HashMap


@Component
class HarvestSourcesScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(HarvestSourcesScheduler::class.simpleName)

//  private val modules: Map<String,String> = mapOf(
//    ITunes.URI to "itunes",
//    AtomLinkModuleImpl.URI to "atom",
//    ContentModuleImpl.URI to "content",
//    CreativeCommonsImpl.URI to "cc",
//    CustomTagsImpl.URI to "custom",
//    DCModuleImpl.URI to "dc",
//    FeedBurnerImpl.URI to "feedburner",
//    GeoRSSModule.GEORSS_GEORSS_URI to "geo",
//    GoogleBaseImpl.URI to "google",
//    MediaModuleImpl.URI to "media",
//    OpenSearchModuleImpl.URI to "os",
//    SlashImpl.URI to "slash",
//    SyModuleImpl.URI to "sy",
//  )

  @Autowired
  lateinit var sourceService: SourceService

  @Autowired
  lateinit var entryRepository: SourceEntryRepository

  @Autowired
  lateinit var sourceRepository: SourceRepository

  private val feedResolvers = arrayOf(
    TwitterFeedResolver(),
    NativeFeedResolver()
  )

  private val entryPostProcessors = arrayOf(
    TwitterTransform(),
    BaseTransform()
  )

  private val contentStrategies = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  @Scheduled(fixedDelay = 13456)
  fun harvestPending() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextHarvestAt")))
    sourceRepository.findAllByNextHarvestAtBeforeAndStatus(Date(), SourceStatus.ACTIVE, pageable)
      .forEach(Consumer { source: Source ->
        try {
          val responses = fetchSource(source)
          val feeds = convertToFeeds(responses)
          postProcessEntries(source, feeds.filterNotNull())

        } catch (ex: Exception) {
          log.error("Harvest failed source ${source.id} (${source.url}), ${ex.message}")
//          if (log.isDebugEnabled) {
//            e.printStackTrace()
//          }
          sourceService.updateNextHarvestDateAfterError(source, ex)
        }
      })
  }

  private fun postProcessEntries(source: Source, feeds: List<RichFeed>) {
    if (feeds.isNotEmpty()) {
      sourceService.enrichSourceWithFeedDetails(feeds.first(), source)
      val entryPostProcessor = resolveEntryPostProcessor(source.sourceType)
      val entries = feeds.first().feed.entries
        .map { syndEntry -> toEntry(syndEntry, source) }
        .map { entry -> entryPostProcessor.applyTransform(source, entry.first, entry.second, feeds) }
        .map { entry -> releaseEntry(entry) }
        .map { entry -> updateEntry(entry) }

      applyRetentionPolicy(source)

      val newEntriesCount = entries.stream().filter { pair: Pair<Boolean, SourceEntry>? -> pair!!.first }.count()
      if (newEntriesCount > 0) {
        log.info("Harvested $newEntriesCount entries for source ${source.id}")
        sourceService.updateUpdatedAt(source)
      }

      entryRepository.saveAll(entries.map { pair: Pair<Boolean, SourceEntry> -> pair.second })
      sourceService.updateNextHarvestDate(source, newEntriesCount > 0)
    }
  }

  private fun applyRetentionPolicy(source: Source) {
    if (source.retentionPolicy == EntryRetentionPolicy.MINIMAL) {
      // todo mag

    }
  }

  private fun releaseEntry(entry: SourceEntry): SourceEntry {
    entry.status = EntryStatus.RELEASED

    try {
      val detector = TextLangDetector()
      listOf("description", "title", "text").filter { s: String ->
        entry.content!![s] != null
      }.map { s: String ->
        entry.content!![s] as String
      }.forEach { text: String -> detector.addText(text) }

      val result = detector.detect()
      entry.lang = result.language
      entry.langScore = result.rawScore
    } catch (e: Exception) {
      entry.lang = "unknown"
      entry.langScore = 0f
    }

    return entry
  }


  private fun updateEntry(entry: SourceEntry): Pair<Boolean, SourceEntry> {
    val optionalEntry = entryRepository.findByLink(entry.link)
    return if (optionalEntry.isPresent) {
      Pair(false, mergeEntries(optionalEntry.get(), entry))
    } else {
      Pair(true, entry)
    }
  }

  private fun mergeEntries(existingEntry: SourceEntry, entry: SourceEntry): SourceEntry {
    val merged = HashMap<String, Any>(100)

    existingEntry.content?.let { existingContent ->
      existingContent.keys
        .forEach { key -> merged[key] = existingContent.getValue(key) }
    }
    entry.content?.let { content ->
      content.keys
        .filter { key -> content.getValue(key) is BigInteger }
        .forEach { numericKey -> merged[numericKey] = content.getValue(numericKey) }
    }

    existingEntry.content = merged
    return existingEntry
  }

  private fun toEntry(syndEntry: SyndEntry, source: Source): Pair<SourceEntry, SyndEntry> {
    val entry = SourceEntry()
    entry.link = syndEntry.link
    entry.source = source
    val properties: Map<String, Any> = mapOf(
      "link" to syndEntry.link,
      "author" to syndEntry.author,
      "title" to syndEntry.title,
      "comments" to syndEntry.comments,
      "authors" to emptyToNull(syndEntry.authors),
      "categories" to emptyToNull(syndEntry.categories),
      "contents" to emptyToNull(syndEntry.contents),
      "contributors" to emptyToNull(syndEntry.contributors),
      "description" to syndEntry.description,
      "updatedDate" to syndEntry.updatedDate,
      "enclosures" to emptyToNull(syndEntry.enclosures),
      "publishedDate" to syndEntry.publishedDate,
    ).filterValues { it != null }
      .mapValues { it.value as Any }

    if (syndEntry.publishedDate == null) {
      entry.pubDate = Date()
    } else {
      entry.pubDate = syndEntry.publishedDate
    }
    entry.content = properties
    entry.createdAt = Date()
    return Pair(entry, syndEntry)
  }

  private fun emptyToNull(list: List<Any>): Any? {
    return if (list.isEmpty()) {
      null
    } else {
      list
    }
  }

  private fun convertToFeeds(responses: List<HarvestResponse>): List<RichFeed?> {
    return responses.map { response ->
      try {
        contentStrategies.first { contentStrategy -> contentStrategy.canProcess(FeedUtil.detectFeedType(response.response)) }.process(response)
      } catch (e: Exception) {
        log.error("Failed to convert feed", e)
        null
      }
    }
  }

  private fun fetchSource(source: Source): List<HarvestResponse> {
    val feedResolver = resolveFeedResolverBySourceType(source.sourceType)
    return feedResolver.feedUrls(source).stream()
      .map { url -> fetchUrl(url) }
      .collect(Collectors.toList())

  }

  private fun fetchUrl(url: HarvestUrl): HarvestResponse {
    log.info("Fetching $url")
    val request = HttpUtil.client.prepareGet(url.url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to ${url.url} cause ${e.message}")
    }
    if(response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }
    return HarvestResponse(url, response)
  }

  private fun resolveEntryPostProcessor(sourceType: SourceType): EntryTransform {
    return entryPostProcessors.first { postProcessor -> postProcessor.canHandle(sourceType) }
  }

  private fun resolveFeedResolverBySourceType(sourceType: SourceType): FeedSourceResolver {
    return feedResolvers.first { feedResolver -> feedResolver.canHandle(sourceType) }
  }
}

