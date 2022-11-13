package org.migor.rich.rss.service

import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.generated.NativeFeedsWhereInputDto
import org.migor.rich.rss.harvest.HarvestResponse
import org.migor.rich.rss.harvest.feedparser.FeedBodyParser
import org.migor.rich.rss.harvest.feedparser.JsonFeedParser
import org.migor.rich.rss.harvest.feedparser.NullFeedParser
import org.migor.rich.rss.harvest.feedparser.XmlFeedParser
import org.migor.rich.rss.transform.ExtendedFeedRule
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
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
  lateinit var environment: Environment

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

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

  fun parseFeedFromUrl(corrId: String, url: String): RichFeed {
    httpService.guardedHttpResource(corrId, url, 200, listOf("text/"))
    val request = httpService.prepareGet(url)
//    authHeader?.let {
//      request.setHeader("Authorization", it)
//    }
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    log.info("[$branchedCorrId] GET $url")
    val response = httpService.executeRequest(branchedCorrId, request, 200)
    return this.parseFeed(corrId, HarvestResponse(url, response))
  }

  fun parseFeed(corrId: String, response: HarvestResponse): RichFeed {
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
    }.onFailure {
      log.error("[${corrId}] bodyParser ${bodyParser::class.simpleName} failed with ${it.message}")
    }.getOrThrow()
  }

  fun updateUpdatedAt(corrId: String, feed: NativeFeedEntity) {
    log.debug("[$corrId] Updating updatedAt for feed=${feed.id}")
    nativeFeedDAO.updateUpdatedAt(feed.id, Date())
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun updateNextHarvestDateAfterError(corrId: String, feed: NativeFeedEntity, e: Throwable) {
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

    // todo mag push ops notificationService.createOpsNotificationForUser(corrId, feed, e)

    nativeFeedDAO.save(feed)
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

  fun updateNextHarvestDate(corrId: String, feed: NativeFeedEntity, hasNewEntries: Boolean) {
    val harvestIntervalMinutes = Optional.ofNullable(feed.harvestIntervalMinutes).orElse(10)
    val harvestInterval = if (hasNewEntries) {
      (harvestIntervalMinutes * 0.5).coerceAtLeast(10.0)
    } else {
      (harvestIntervalMinutes * 4).coerceAtMost(700) // twice a day
    }
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval.toLong(), ChronoUnit.MINUTES)))
    log.debug("[$corrId] Scheduling next harvest for ${feed.feedUrl} to $nextHarvestAt")

    nativeFeedDAO.updateNextHarvestAtAndHarvestInterval(feed.id, nextHarvestAt, harvestInterval.toInt())
  }

  fun findRelatedByUrl(homepageUrl: String): List<NativeFeedEntity> {
    val url = URL(homepageUrl)
    return if (environment.acceptsProfiles(Profiles.of("!database"))) {
      emptyList()
    } else {
      nativeFeedDAO.findAllByDomainEquals(url.host)
    }
  }

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun applyRetentionStrategy(corrId: String, feed: NativeFeedEntity) {
    // todo mag implement
//    feed.retentionSize?.let { articleRefRepository.applyRetentionStrategyOfSize(feed.streamId, it) }
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun addToFeed(corrId: String, feedId: String, article: RichArticle, feedSecret: String) {
    TODO("Not yet implemented")
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun deleteFromFeed(corrId: String, feedId: String, articleId: String, feedSecret: String) {
    TODO("Not yet implemented")
  }

  @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
  fun persist(corrId: String, extendedFeedRule: ExtendedFeedRule): ResponseEntity<String> {
    val genericFeed = GenericFeedEntity()

//    genericFeedDAO.save(genericFeed)
    return ResponseEntity.ok("")
  }

  fun findByFeedId(feedId: String, page: Int, type: String?): RichFeed {
    val id = UUID.fromString(feedId)
    val feed = nativeFeedDAO.findById(id).orElseThrow()

    val pagedItems = articleService.findByStreamId(feed.streamId!!, page, ArticleType.feed, ReleaseStatus.released)
    val lastPage = pagedItems.totalPages
    val items = pagedItems.toList()

    return RichFeed(
      // todo mag next and previous
      id = "feed:${feedId}",
      author = "",
      description = feed.description,
      title = feed.title,
      items = items,
      lastPage = lastPage,
      language = "en",
      home_page_url = feed.websiteUrl,
      feed_url = "${propertyService.publicUrl}/feed:$feedId",
      date_published = items.maxOfOrNull { it.publishedAt }
    )
  }

  fun changeStatus(corrId: String, feed: NativeFeedEntity, status: NativeFeedStatus) {
    nativeFeedDAO.setStatus(feed.id, status)
  }

  fun findNativeById(id: UUID): Optional<NativeFeedEntity> {
    return nativeFeedDAO.findById(id)
  }

  fun findAllByFilter(where: NativeFeedsWhereInputDto, pageable: PageRequest): Page<NativeFeedEntity> {
    return nativeFeedDAO.findAllMatching(pageable)
  }

}
