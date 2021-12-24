package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.api.dto.FeedJsonDto
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.FeedData
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
import kotlin.streams.toList

@Service
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var feedRepository: FeedRepository

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
    log.info(
      "Using bodyParsers ${
        feedBodyParsers.joinToString(", ") { contentStrategy -> "$contentStrategy priority: ${contentStrategy.priority()}" }
      }"
    )
  }

  fun parseFeedFromUrl(corrId: String, url: String): FeedData {
    val response = httpService.httpGet(corrId, url, 200)
    return this.parseFeed(corrId, HarvestResponse(url, response))
  }

  fun parseFeed(corrId: String, response: HarvestResponse): FeedData {
    val (feedType, mimeType) = FeedUtil.detectFeedTypeForResponse(
      response.response
    )
    log.debug("[$corrId] Find bodyParser for feedType=$feedType mimeType=$mimeType")
    val bodyParser = feedBodyParsers.first { bodyParser ->
      bodyParser.canProcess(
        feedType,
        mimeType
      )
    }
    return bodyParser.process(response)
  }

  fun updateUpdatedAt(corrId: String, feed: Feed) {
    log.debug("[$corrId] Updating updatedAt for feed=${feed.id}")
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun updateNextHarvestDateAfterError(corrId: String, feed: Feed, e: Exception) {
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(2, ChronoUnit.MINUTES)))
    log.info("[$corrId] Rescheduling failed harvest ${feed.feedUrl} to $nextHarvestAt")

    val message = Optional.ofNullable(e.message).orElse(e.javaClass.toString())
    val json = JsonUtil.gson.toJson(message)
//    saveOpsMessage(message, feed, true)
//    feedEventRepository.save(FeedEvent(json, feed, true))

    val twoWeeksAgo = Date.from(Date().toInstant().minus(Duration.of(2, ChronoUnit.HOURS))) // todo mag externalize

    val errorCount = 0
//      feedEventRepository.countByFeedIdAndCreatedAtAfterAndErrorIsTrueOrderByCreatedAtDesc(feed.id!!, twoWeeksAgo)
    if (errorCount >= 2) {
      feed.status = FeedStatus.stopped
      log.info("[$corrId] Stopped harvesting, max-error threshold reached")
    } else {
      log.info("[$corrId] Erroneous feed with errorCount $errorCount")
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

  fun updateNextHarvestDate(corrId: String, feed: Feed, hasNewEntries: Boolean) {
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
    log.debug("[$corrId] Scheduling next harvest for ${feed.feedUrl} to $nextHarvestAt")

    feedRepository.updateNextHarvestAtAndHarvestInterval(feed.id!!, nextHarvestAt, harvestInterval.toInt())
  }

  fun redeemStatus(source: Feed) {
    feedRepository.updateStatus(source.id!!, FeedStatus.ok)
  }

  fun findByFeedId(feedId: String, page: Int = 0): FeedJsonDto {
    val feed = feedRepository.findById(feedId).orElseThrow()

    val pageable = PageRequest.of(page, 10)

    val pageResult = articleRepository.findAllByStreamId(feed.streamId!!, pageable)

    return FeedJsonDto(
      id = null,
      name = feed.title!!,
      description = feed.description!!,
      home_page_url = feed.homePageUrl!!,
      date_published = feed.lastUpdatedAt!!,
      items = pageResult.get().map { result -> (result[0] as Article).toDto(result[1] as Date) }.toList(),
      feed_url = "${propertyService.host}/feed:$feedId",
      expired = false,
      lastPage = pageResult.totalPages,
      selfPage = page
    )
  }

//  fun queryViaEngines(query: String, token: String) {
//    TODO("Not yet implemented")
////    bing.com/search?format=rss&q=khayrirrw
//  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun update(feed: Feed) {
    feedRepository.save(feed)
  }

  fun findRelatedByUrl(homepageUrl: String): List<Feed> {
    val url = URL(homepageUrl)
    return feedRepository.findAllByDomainEquals(url.host)
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun applyRetentionStrategy(corrId: String, feed: Feed) {
    // todo mag implement
//    feed.retentionSize?.let { articleRefRepository.applyRetentionStrategyOfSize(feed.streamId, it) }
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun addToFeed(corrId: String, feedId: String, article: ArticleJsonDto, feedSecret: String) {
    TODO("Not yet implemented")
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun deleteFromFeed(corrId: String, feedId: String, articleId: String, feedSecret: String) {
    TODO("Not yet implemented")
  }

}
