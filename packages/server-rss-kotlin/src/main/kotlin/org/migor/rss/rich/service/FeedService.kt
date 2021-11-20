package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.FeedEvent
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedEventRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.feedparser.FeedBodyParser
import org.migor.rss.rich.harvest.feedparser.JsonFeedParser
import org.migor.rss.rich.harvest.feedparser.NullFeedParser
import org.migor.rss.rich.harvest.feedparser.XmlFeedParser
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedEventRepository: FeedEventRepository

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var httpService: HttpService

  private val feedBodyParsers: Array<FeedBodyParser> = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  init {
    feedBodyParsers.sortByDescending { feedBodyParser -> feedBodyParser.priority() }
    log.info("Using bodyParsers ${feedBodyParsers.map { contentStrategy -> "$contentStrategy priority: ${contentStrategy.priority()}" }.joinToString(", ")}")
  }

  fun parseFeedFromUrl(cid: String, url: String): FeedData {
    log.debug("[$cid] Fetching $url")
    val response = httpService.httpGet(url)

    if (response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }

    return this.parseFeed(cid, HarvestResponse(url, response))
  }

  fun parseFeed(cid: String, response: HarvestResponse): FeedData {
    return try {
      val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(
        response.response
      )
      log.debug("[$cid] Find bodyParser for feedType=$feedType mimeType=$mimeType")
      val bodyParser = feedBodyParsers.first { bodyParser ->
        bodyParser.canProcess(
          feedType,
          mimeType
        )
      }
      bodyParser
        .process(response)
    } catch (e: Exception) {
      throw HarvestException(e.message!!)
    }
  }

  fun updateUpdatedAt(cid: String, feed: Feed) {
    log.debug("[$cid] Updating updatedAt for feed=${feed.id}")
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun updateNextHarvestDateAfterError(cid: String, feed: Feed, e: Exception) {
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(2, ChronoUnit.MINUTES)))
    log.debug("[$cid] Rescheduling failed harvest ${feed.feedUrl} to $nextHarvestAt")

    val message = Optional.ofNullable(e.message).orElse(e.javaClass.toString())
    val json = JsonUtil.gson.toJson(message)
    feedEventRepository.save(FeedEvent(json, feed, true))

    val twoWeeksAgo = Date.from(Date().toInstant().minus(Duration.of(2, ChronoUnit.HOURS))) // todo mag externalize
    feedEventRepository.deleteAllByFeedIdAndCreatedAtBefore(feed.id!!, twoWeeksAgo)

    val errorCount =
      feedEventRepository.countByFeedIdAndCreatedAtAfterAndErrorIsTrueOrderByCreatedAtDesc(feed.id!!, twoWeeksAgo)
    if (errorCount >= 5) {
      feed.status = FeedStatus.stopped
      log.info("[$cid] Stopped harvesting, max-error threshold reached")
    } else {
      log.info("[$cid] Errornous feed with errorCount $errorCount")
      feed.status = FeedStatus.errornous
    }
    feed.nextHarvestAt = nextHarvestAt

    feedRepository.save(feed)
  }

  companion object {
    fun absUrl(baseUrl: String, relativeUrl: String): String {
      return try {
        URL(URL(baseUrl), relativeUrl).toURI().toString()
      } catch (e: Exception) {
        relativeUrl
      }
    }
  }

  fun updateNextHarvestDate(cid: String, feed: Feed, hasNewEntries: Boolean) {
    val harvestIntervalMinutes = Optional.ofNullable(feed.harvestIntervalMinutes).orElse(10)
    val harvestInterval = if (hasNewEntries) {
      (harvestIntervalMinutes * 0.5).coerceAtLeast(10.0)
    } else {
      (harvestIntervalMinutes * 4).coerceAtMost(700) // twice a day
    }
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
//    val retryAfter = responses.map { response -> response.response.getHeaders("Retry-After") }
//      .filter { retryAfter -> !retryAfter.isEmpty() }
//    slow down fetching if no content, until once a day

    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval.toLong(), ChronoUnit.MINUTES)))
    log.debug("[$cid] Scheduling next harvest for ${feed.feedUrl} to $nextHarvestAt")

    feedRepository.updateNextHarvestAtAndHarvestInterval(feed.id!!, nextHarvestAt, harvestInterval.toInt())
  }

  fun redeemStatus(source: Feed) {
    feedRepository.updateStatus(source.id!!, FeedStatus.ok)
  }

  fun findByStreamId(streamId: String): FeedJsonDto {
    val feed = feedRepository.findByStreamId(streamId)

    val pageable = PageRequest.of(0, 10)

    val articles = articleRepository.findAllByStreamId(streamId, pageable)

    return FeedJsonDto(
      id = null,
      name = feed.title!!,
      description = feed.description!!,
      home_page_url = feed.homePageUrl!!,
      date_published = feed.lastUpdatedAt!!,
      items = articles.map { article -> article.toDto() },
      feed_url = "${propertyService.host()}/stream:$streamId",
      expired = false
    )
  }

  fun queryViaEngines(query: String, token: String) {
    TODO("Not yet implemented")
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun update(feed: Feed) {
    feedRepository.save(feed)
  }

  fun findRelatedByUrl(homepageUrl: String): List<Feed> {
    val url = URL(homepageUrl)
    return feedRepository.findAllByDomainEquals(url.host)
  }
}
