package org.migor.feedless.feed

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.time.DateUtils
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.asJsonItem
import org.migor.feedless.plan.FeatureName
import org.migor.feedless.plan.FeatureService
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.service.ScrapeService
import org.migor.feedless.source.SourceDAO
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.user.UserDAO
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.migor.feedless.web.ExtendContext
import org.migor.feedless.web.GenericFeedSelectors
import org.migor.feedless.web.WebToFeedTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Service
@Profile(AppProfiles.legacyFeeds)
class LegacyFeedService {

  private val log = LoggerFactory.getLogger(LegacyFeedService::class.simpleName)

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var webToFeedTransformer: WebToFeedTransformer

  @Autowired
  private lateinit var feedParserService: FeedParserService

  @Autowired
  private lateinit var scrapeService: ScrapeService

  @Autowired
  private lateinit var repositoryDAO: RepositoryDAO

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var documentDAO: DocumentDAO

  @Autowired
  private lateinit var sourceDAO: SourceDAO

  @Autowired
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var documentService: DocumentService

  @Autowired
  private lateinit var filterPlugin: CompositeFilterPlugin

  fun getRepoTitleForLegacyFeedNotifications(): String = "legacyFeedNotifications"

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedId")
  fun getFeed(corrId: String, feedId: String, feedUrl: String): JsonFeed {
    return if (legacySupport()) {
      val sourceId = UUID.fromString(feedId)
      val feed = sourceDAO.findById(sourceId).orElseThrow().toJsonFeed(feedUrl)
      feed.items =
        documentDAO.findAllBySourceId(sourceId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt")))
          .map { it.asJsonItem() }

      appendNotifications(corrId, feed)
    } else {
      createEolFeed(feedUrl)
    }
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #url + #linkXPath + #contextXPath + #filter")
  fun webToFeed(
    corrId: String,
    url: String,
    linkXPath: String,
    extendContext: String,
    contextXPath: String,
    dateXPath: String?,
    prerender: Boolean,
    filter: String?,
    feedUrl: String
  ): JsonFeed {
    return if (legacySupport()) {
      val source = SourceEntity()
      source.title = "Feed from $url"
      val fetch = FetchActionEntity()
      fetch.pos = 1
      fetch.forcePrerender = prerender
      fetch.url = url
      fetch.isVariable = false
      source.actions.add(fetch)

      val scrapeOutput = runBlocking {
        scrapeService.scrape(corrId, source)
      }

      val selectors = GenericFeedSelectors(
        linkXPath = linkXPath,
        extendContext = when (extendContext) {
          "p" -> ExtendContext.PREVIOUS
          "n" -> ExtendContext.NEXT
          "pn" -> ExtendContext.PREVIOUS_AND_NEXT
          else -> ExtendContext.NONE
        },
        contextXPath = contextXPath,
        dateXPath = dateXPath,
      )

      val feed = webToFeedTransformer.getFeedBySelectors(
        corrId,
        selectors,
        HtmlUtil.parseHtml(
          scrapeOutput.outputs.find { o -> o.fetch != null }!!.fetch!!.response.responseBody.toString(
            StandardCharsets.UTF_8
          ), url
        ),
        URL(url)
      )
      feed.feedUrl = feedUrl

      try {
        filter?.let {
          val params = PluginExecutionParamsInput(
            org_feedless_filter = listOf(
              ItemFilterParamsInput(
                expression = it
              )
            )
          )
          feed.items = feed.items.filterIndexed { index, jsonItem ->
            filterPlugin.filterEntity(
              corrId,
              jsonItem,
              params,
              index
            )
          }
        }
      } catch (e: Throwable) {
        log.warn("[$corrId] webToFeed failed: ${e.message}", e)
      }

      appendNotifications(corrId, feed)
    } else {
      createEolFeed(url)
    }
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #nativeFeedUrl + #filter")
  fun transformFeed(corrId: String, nativeFeedUrl: String, filter: String?, feedUrl: String): JsonFeed {
    return if (legacySupport()) {
      appendNotifications(
        corrId,
        feedParserService.parseFeedFromUrl(corrId, nativeFeedUrl)
      )
    } else {
      createEolFeed(feedUrl)
    }
  }

  fun getRepository(repositoryId: String): ResponseEntity<String> {
    val headers = HttpHeaders()
    headers.add("Location", "/f/$repositoryId/atom")
    return ResponseEntity(headers, HttpStatus.FOUND)
  }

  // --

  private fun appendNotifications(corrId: String, feed: JsonFeed): JsonFeed {
    val root = userDAO.findFirstByRootIsTrue()
    repositoryDAO.findByTitleAndOwnerId(getRepoTitleForLegacyFeedNotifications(), root!!.id)?.let { repo ->
      val pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "publishedAt"))
      val documents = documentService.findAllByRepositoryId(
        repo.id,
        status = ReleaseStatus.released,
        pageable = pageable,
        ignoreVisibility = true
      )
        .filterNotNull()
        .map {
          it.publishedAt = Date()
          it.asJsonItem()
        }
      feed.items = documents.plus(feed.items)
    } ?: log.error("[$corrId] Repo for legacy notification not found")
    return feed
  }

  private fun legacySupport(): Boolean {
    return !featureService.isDisabled(FeatureName.legacyApiBool)
  }

  private fun createEolFeed(feedUrl: String): JsonFeed {
    val feed = JsonFeed()
    feed.id = "rss-proxy:2"
    feed.title = "End Of Life"
    feed.feedUrl = feedUrl
    feed.expired = true
    feed.publishedAt = Date()
    feed.items = listOf(createEolArticle(feedUrl))

    return feed
  }

  fun createErrorFeed(corrId: String, feedUrl: String, t: Throwable): JsonFeed {
    val feed = JsonFeed()
    feed.id = "rss-proxy:2"
    feed.title = "Feed"
    feed.feedUrl = feedUrl
    feed.expired = false
    feed.publishedAt = Date()

    feed.items = if (t is ResumableHarvestException || t is TooManyConnectionsPerHostException) {
      emptyList()
    } else {
      listOf(createErrorArticle(corrId, t, feedUrl))
    }

    return feed
  }

  private fun createEolArticle(url: String): JsonItem {
    val article = JsonItem()
    val preregistrationLink = "${propertyService.appHost}?url=${URLEncoder.encode(url, StandardCharsets.UTF_8)}"
    article.id = FeedUtil.toURI("end-of-life", preregistrationLink)
    article.title = "SERVICE ANNOUNCEMENT: RSS-Proxy Urls have reached End-of-life"
    article.contentHtml = """Dear User,

I hope this message finds you well. As of now, RSS-Proxy urls are no longer supported.

However, you can easily migrate to feedless and take advantage of all new features.

$preregistrationLink

Should you have any questions or concerns, reach out to me.

Regards,
Markus

    """.trimIndent()
//    article.contentText = "Thanks for using rssproxy or feedless. I have terminated the service has has ended. You may migrate to the latest version using this link $migrationUrl"
    article.url = preregistrationLink
    article.publishedAt = Date()
    return article
  }

  private fun createErrorArticle(corrId: String, t: Throwable, feedUrl: String): JsonItem {
    val article = JsonItem()
    article.id = FeedUtil.toURI("error", DateUtils.truncate(Date(), Calendar.MONTH).time.toString() + t.message)
    article.title = "ALERT: Potential Issue with Feed"
    article.contentText = """Dear User,
an error occurred while fetching your feed: '${t.message}'. This may require your attention. Please note, this error will only be reported once per month.
If you believe this is a bug, maybe related with the new release, here is the stack trace so you can report it.

---
Feed URL: $feedUrl
corrId: $corrId
${StringUtils.truncate(t.stackTraceToString(), 800)}
""".trimIndent()
    article.contentHtml = """<p>Dear User,</p>
<p>an error occurred while fetching your feed: '${t.message}'. This may require your attention. Please note, this error will only be reported once per month.</p>

<p>If you believe this is a bug, maybe related with the new release, here is the stack trace so you can report it.<p>

<p>---</p>
<p>Feed URL: $feedUrl</p>
<p>corrId: $corrId</p>
<p>
<pre>
${StringUtils.truncate(t.stackTraceToString(), 800)}
</pre>
</p>
""".trimIndent()
    article.url = "https://github.com/damoeb/feedless/wiki/Errors#error-potential-issue-with-feed"
    article.publishedAt = Date()
    return article
  }
}

private fun SourceEntity.toJsonFeed(feedUrl: String): JsonFeed {
  val feed = JsonFeed()
  feed.id = "legacy-feed"
  feed.title = title
  feed.publishedAt = createdAt
  feed.items = emptyList()
  feed.feedUrl = feedUrl
  return feed
}
