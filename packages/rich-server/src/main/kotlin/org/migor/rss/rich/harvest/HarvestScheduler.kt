package org.migor.rss.rich.harvest

import com.google.gson.GsonBuilder
import com.rometools.rome.feed.synd.SyndEntry
import org.asynchttpclient.Dsl
import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.EntryRepository
import org.migor.rss.rich.repository.SubscriptionRepository
import org.migor.rss.rich.service.FeedService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors


@Component
class HarvestScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(HarvestScheduler::class.simpleName)
  private val gson = GsonBuilder().create()

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
  lateinit var feedService: FeedService

  @Autowired
  lateinit var entryRepository: EntryRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  private val builderConfig = Dsl.config()
    .setConnectTimeout(60000)
    .setReadTimeout(60000)
    .setFollowRedirect(true)
    .setMaxRedirects(3)
    .build()

  private val client = Dsl.asyncHttpClient(builderConfig)
  private val harvestStrategies = arrayOf(
//    SimpleHarvest(),
    TwitterHarvest()
  )

  private val contentStrategies = arrayOf(
    XmlContent(),
//    JsonContent()
  )

  @Scheduled(fixedDelay = 20000, initialDelay = 10000)
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
    subscriptionRepository.findNextHarvestEarlier(Date())
      .forEach(Consumer { subscription: Subscription ->
        try {
          log.info("Harvesting" + subscription.name)
//        1. find feed url
          val strategy = findStrategy(subscription)
          log.info("using " + strategy.javaClass.simpleName)
//        2. download feed
          val responses = fetchUrls(strategy.urls(subscription))
//        3. to internal format
          val feeds = convertToFeeds(responses)
//        4. process feed
          processSubscription(subscription, feeds, strategy)

          setNextHarvestAfter(subscription, responses)
        } catch (e: Exception) {
          log.error("Cannot harvest subscription", subscription.id)
          e.printStackTrace()
          // todo mag save error in subscription
        } finally {
          subscriptionRepository.save(subscription)
        }
      })
  }

  private fun processSubscription(subscription: Subscription, feeds: List<RichFeed>, harvestStrategy: HarvestStrategy) {
    feedService.createFeedForSubscription(feeds.first(), subscription)

    val entries = feeds.first().feed.entries
      .filter { syndEntry: SyndEntry -> !entryRepository.existsBySubscriptionIdAndLink(subscription.id!!, syndEntry.link) }
      .map { syndEntry -> toEntry(syndEntry, subscription) }
      .map { entry -> harvestStrategy.applyPostTransforms(entry.first, entry.second, feeds) }

    log.info("Adding ${entries.size} entries to subscription ${subscription.id} (${subscription.name})")
    entryRepository.saveAll(entries)
  }

  private fun toEntry(syndEntry: SyndEntry, subscription: Subscription): Pair<Entry, SyndEntry> {
    val entry = Entry()
    entry.link = syndEntry.link
    entry.subscription = subscription
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
    return responses.map {
      response -> contentStrategies.first { contentStrategy -> contentStrategy.canProcess(response) }.process(response)
    }
  }

  private fun fetchUrls(urls: List<HarvestUrl>): List<HarvestResponse> {
    return urls.stream()
      .map { url -> fetchUrl(url) }
      .collect(Collectors.toList())

  }

  private fun fetchUrl(url: HarvestUrl): HarvestResponse {
    log.info("Fetching ${url}")
    val request = client.prepareGet(url.url).execute()
    val response = request.get()

    if(response.statusCode != 200) {
      throw BadStatusCodeException("Expected 200 received ${response.statusCode}")
    }
    return HarvestResponse(url, response)
  }

  private fun setNextHarvestAfter(subscription: Subscription, responses: List<HarvestResponse>) {
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
    subscription.nextHarvestAt = Date.from(Date().toInstant().plus(Duration.ofSeconds(60)))
  }

  private fun findStrategy(subscription: Subscription): HarvestStrategy {
    return harvestStrategies.first { harvestStrategy -> harvestStrategy.canHarvest(subscription) }
  }
}

