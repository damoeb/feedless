package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.api.dto.FeedJsonDto
import org.migor.rich.rss.database.enums.FeedStatus
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.ArticleRefType
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.database.repository.FeedRepository
import org.migor.rich.rss.harvest.FeedData
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedBodyParser
import org.migor.rich.rss.harvest.feedparser.JsonFeedParser
import org.migor.rich.rss.harvest.feedparser.NullFeedParser
import org.migor.rich.rss.harvest.feedparser.XmlFeedParser
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
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
  lateinit var environment: Environment

  @Autowired(required=false)
  lateinit var feedRepository: FeedRepository

  @Autowired(required=false)
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired(required=false)
  lateinit var notificationService: NotificationService

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

  fun parseFeedFromUrl(corrId: String, url: String, authHeader: String?): FeedData {
    val request = httpService.prepareGet(url)
    authHeader?.let {
      request.setHeader("Authorization", it)
    }
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    log.info("[$branchedCorrId] GET $url")
    val response = httpService.executeRequest(branchedCorrId, request, 200)
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
    return runCatching {
      bodyParser.process(corrId, response)
    }.onFailure { log.error("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}") }
      .getOrThrow()
  }

  fun updateUpdatedAt(corrId: String, feed: Feed) {
    log.debug("[$corrId] Updating updatedAt for feed=${feed.id}")
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun updateNextHarvestDateAfterError(corrId: String, feed: Feed, e: Throwable) {
    // todo mag externalize nextHarvest interval

    feed.failedAttemptCount += 1
    val nextHarvestAt = if (feed.failedAttemptCount >= 5) {
      log.info("[$corrId] Critical errorCount reached, quasi-stopping harvesting, retrying every 2 days")
      Date.from(Date().toInstant().plus(Duration.of(2, ChronoUnit.DAYS)))
    } else {
      log.info("[$corrId] Erroneous feed with errorCount ${feed.failedAttemptCount}")
      Date.from(Date().toInstant().plus(Duration.of((10 * (feed.failedAttemptCount + 1)).toLong(), ChronoUnit.MINUTES)))
    }

    log.info("[$corrId] Rescheduling failed harvest ${feed.feedUrl} to $nextHarvestAt")
    feed.nextHarvestAt = nextHarvestAt

    notificationService.createOpsNotificationForUser(corrId, feed, e)

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
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval.toLong(), ChronoUnit.MINUTES)))
    log.debug("[$corrId] Scheduling next harvest for ${feed.feedUrl} to $nextHarvestAt")

    feedRepository.updateNextHarvestAtAndHarvestInterval(feed.id!!, nextHarvestAt, harvestInterval.toInt())
  }

  fun redeemStatus(source: Feed) {
    feedRepository.updateStatus(source.id!!, FeedStatus.ok)
  }

  fun findByFeedId(feedId: String, page: Int = 0, type: String?): FeedJsonDto {
    val feed = feedRepository.findById(feedId).orElseThrow()

    val articleRefType = Optional.ofNullable(ArticleRefType.findByName(type)).orElse(ArticleRefType.feed)

    val pageable = PageRequest.of(page, 10)

    val pageResult = articleRepository.findAllByStreamId(feed.streamId!!, articleRefType, pageable)

    return FeedJsonDto(
      id = null,
      name = feed.title!!,
      description = feed.description,
      home_page_url = feed.homePageUrl!!,
      date_published = feed.lastUpdatedAt!!,
      items = pageResult.get().map { result -> (result[0] as Article).toDto(result[1] as Date) }.toList(),
      feed_url = "${propertyService.host}/feed:$feedId",
      expired = false,
      lastPage = pageResult.totalPages -1,
      selfPage = page,
      tags = feed.tags?.map { nsTag -> nsTag.tag }
    )
  }

//  fun queryViaEngines(query: String, token: String) {
//    TODO("Not yet implemented")
////    bing.com/search?format=rss&q=khayrirrw
//  }

  fun update(feed: Feed) {
    feedRepository.updateMetadata(
      homepageUrl = feed.homePageUrl,
      id = feed.id!!,
      title = feed.title,
      author = feed.author,
    )
  }

  fun findRelatedByUrl(homepageUrl: String): List<Feed> {
    val url = URL(homepageUrl)
    return if (environment.acceptsProfiles(Profiles.of("proxy"))) {
      emptyList()}else {feedRepository.findAllByDomainEquals(url.host)
    }
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
