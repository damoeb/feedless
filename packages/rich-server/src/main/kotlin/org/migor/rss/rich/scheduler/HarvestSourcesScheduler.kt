package org.migor.rss.rich.scheduler

import com.rometools.rome.feed.synd.SyndEntry
import org.migor.rss.rich.HttpUtil
import org.migor.rss.rich.feed.*
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceType
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SourceRepository
import org.migor.rss.rich.service.SourceService
import org.migor.rss.rich.service.SubscriptionService
import org.migor.rss.rich.transform.BaseTransform
import org.migor.rss.rich.transform.EntryTransform
import org.migor.rss.rich.transform.TwitterTransform
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
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
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var sourceService: SourceService

  @Autowired
  lateinit var entryRepository: EntryRepository

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
    XmlContent(),
    NullFeedParser(),
//    JsonContent()
  )

  @Scheduled(fixedDelay = 23456, initialDelay = 10000)
  fun harvestPending() {
    /*
    todo mag better but more complicated flow

    Harvester
    - find feed urls that have subscriptions
    - fetch feeds
    - correct feed errors
    - convert to internal format
    - archive raw feed items

    Feed Generator
    - find subscriptions that have to be updated
    - apply transforms
    - generate new feed

     */
    sourceRepository.findAllByNextHarvestAtBeforeOrNextHarvestAtIsNull(Date(), PageRequest.of(0, 10))
      .forEach(Consumer { source: Source ->
        try {
          log.info("Preparing harvest ${source.id} using ${source.sourceType}")
          val responses = fetchUrls(source)
          val feeds = convertToFeeds(responses)
          postProcessEntries(source, feeds)

          subscriptionService.updateHarvestDate(source, responses)
        } catch (e: Exception) {
          log.error("Cannot harvest subscription ${source.id}")
          e.printStackTrace()
          // todo mag save error in subscription
        }
      })
  }

  private fun postProcessEntries(source: Source, feeds: List<RichFeed>) {
    sourceService.enrichSourceWithFeedDetails(feeds.first(), source)
    val entryPostProcessor = resolveEntryPostProcessor(source.sourceType!!)
    val entries = feeds.first().feed.entries
      // todo mag explode entries
//      .filter { syndEntry: SyndEntry -> !entryRepository.existsBySubscriptionIdAndLink(subscription.id!!, syndEntry.link) }
      .map { syndEntry -> toEntry(syndEntry, source) }
      .map { entry -> entryPostProcessor.applyTransform(source, entry.first, entry.second, feeds) }
      .map { entry -> releaseEntry(entry) }
      .map { entry -> updateEntry(entry) }

    applyRetentionPolicy(source)

//    log.info("Adding ${entries.size} entries to subscription ${subscription.id} (${subscription.name})")
    entryRepository.saveAll(entries)
  }

  private fun applyRetentionPolicy(source: Source) {
    // todo mag
  }

  private fun releaseEntry(entry: Entry): Entry {
    entry.status = EntryStatus.RELEASED
    return entry
  }


  private fun updateEntry(entry: Entry): Entry {
    val optionalEntry = entryRepository.findByLink(entry.link)
    return if (optionalEntry.isPresent) {
      mergeEntries(optionalEntry.get(), entry)
    } else {
      log.info("adds entry ${entry.status} ${entry.link}")
      entry
    }
  }

  private fun mergeEntries(existingEntry: Entry, entry: Entry): Entry {
    val merged = HashMap<String, Any>(100)

    existingEntry.content?.let { existingContent -> existingContent.keys
      .forEach { key -> merged.put(key, existingContent.getValue(key))}}
    entry.content?.let { content -> content.keys
      .filter { key -> content.getValue(key) is BigInteger }
      .forEach {numericKey -> merged.put(numericKey, content.getValue(numericKey)) }}

    existingEntry.content = merged
    return existingEntry
  }

  private fun toEntry(syndEntry: SyndEntry, source: Source): Pair<Entry, SyndEntry> {
    val entry = Entry()
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

//    syndEntry.modules.forEach { module -> assignProperties(properties, module) }

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

  private fun convertToFeeds(responses: List<HarvestResponse>): List<RichFeed> {
    return responses.map { response ->
      contentStrategies.first { contentStrategy -> contentStrategy.canProcess(response) }.process(response)
    }
  }

  private fun fetchUrls(source: Source): List<HarvestResponse> {
    val feedResolver = resolveFeedResolverBySourceType(source.sourceType!!)
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
      throw HarvestException("When fetching ${url.url} expected 200 received ${response.statusCode}")
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

