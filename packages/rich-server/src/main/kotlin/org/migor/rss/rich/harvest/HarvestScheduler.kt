package org.migor.rss.rich.harvest

import com.rometools.rome.feed.synd.SyndEntry
import io.netty.handler.codec.http.HttpHeaders
import org.asynchttpclient.Dsl
import org.migor.rss.rich.model.Subscription
import org.migor.rss.rich.repository.SubscriptionRepository
import org.migor.rss.rich.service.FeedService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.lang.Exception
import java.time.Duration
import java.util.*
import java.util.function.Consumer


@Component
class HarvestScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(HarvestScheduler::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var repository: SubscriptionRepository

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

  @Scheduled(fixedDelay = 5000, initialDelay = 10000)
  fun harvestPending() {
    log.info("Starting harvester")
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
    repository.findNextHarvestEarlier(Date())
      .forEach(Consumer { subscription: Subscription ->
        try {
          log.info("Harvesting" + subscription.name)
//        1. find feed url
          val harvestStrategy = findStrategy(subscription)
          log.info("using " + harvestStrategy.javaClass.simpleName)
//        2. download feed
          val harvestResponse = fetchFeed(subscription, harvestStrategy)
//        3. to internal format
          val richFeed = getFeedContent(harvestResponse)
//        4. process feed
          processSubscription(subscription, richFeed, harvestStrategy)

        } catch (e: Exception) {
          log.error("Cannot harvest subscription", subscription.uuid)
          e.printStackTrace()
          // todo mag save error in subscription
        } finally {
          repository.save(subscription)
        }
      })

    log.info("Stopping harvester")
  }

  private fun processSubscription(subscription: Subscription, richFeed: RichFeed, harvestStrategy: HarvestStrategy) {
    feedService.saveOrUpdateFeedForSubscription(richFeed, subscription);

    val entries: List<SyndEntry> = dropKnownEntries(richFeed).map { syndEntry -> harvestStrategy.applyTransforms(syndEntry) }

  }

  private fun dropKnownEntries(richFeed: RichFeed): List<SyndEntry> {
    TODO()

  }

  private fun getFeedContent(response: HarvestResponse): RichFeed {
    val contentStrategy = contentStrategies.first { contentStrategy -> contentStrategy.canProcess(response) }
    return contentStrategy.process(response)
  }

  private fun fetchFeed(subscription: Subscription, strategy: HarvestStrategy): HarvestResponse {
    val url = strategy.url(subscription)
    log.info("Fetching ${url}")
    val request = client.prepareGet(url).execute()
    val response = request.get()

    if(response.statusCode != 200) {
     throw BadStatusCodeException("Expected 200 received ${response.statusCode}")
    }

    setNextHarvestAfter(subscription, response.headers)

    return HarvestResponse(response.responseBody, response.contentType)
  }

  private fun setNextHarvestAfter(subscription: Subscription, responseHeaders: HttpHeaders) {
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
    subscription.nextHarvestAt = Date.from(Date().toInstant().plus(Duration.ofSeconds(60)))
  }

  private fun findStrategy(subscription: Subscription): HarvestStrategy {
    return harvestStrategies.first { harvestStrategy -> harvestStrategy.canHarvest(subscription) }
  }
}

