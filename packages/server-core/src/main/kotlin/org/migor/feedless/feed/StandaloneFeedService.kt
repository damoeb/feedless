package org.migor.feedless.feed

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.actions.FetchActionEntity
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.document.DocumentService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.ItemFilterParamsInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.asJsonItem
import org.migor.feedless.repository.RepositoryService
import org.migor.feedless.repository.toEntity
import org.migor.feedless.scrape.ExtendContext
import org.migor.feedless.scrape.GenericFeedSelectors
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToFeedTransformer
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.source.SourceService
import org.migor.feedless.user.UserService
import org.migor.feedless.user.corrId
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.standaloneFeeds} & ${AppLayer.service}")
class StandaloneFeedService(
  private val propertyService: PropertyService,
  private val webToFeedTransformer: WebToFeedTransformer,
  private val feedParserService: FeedParserService,
  private val scrapeService: ScrapeService,
  private val repositoryService: RepositoryService,
  private val userService: UserService,
  private val sourceService: SourceService,
  private val documentService: DocumentService,
  private val filterPlugin: CompositeFilterPlugin,
  private val environment: Environment
) {

  private val log = LoggerFactory.getLogger(StandaloneFeedService::class.simpleName)

  fun getRepoTitleForStandaloneFeedNotifications(): String = "legacyFeedNotifications"

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun getFeed(sourceId: UUID, feedUrl: String): JsonFeed {
    val feed =
      sourceService.findById(sourceId).orElseThrow { NotFoundException("feedId not found") }.toJsonFeed(feedUrl)
    feed.items =
      documentService.findAllBySourceId(sourceId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt")))
        .map { it.asJsonItem() }

    return appendNotifications(feed)
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun webToFeed(
    url: String,
    linkXPath: String,
    extendContext: String,
    contextXPath: String,
    dateXPath: String?,
    prerender: Boolean,
    filter: String?,
    ts: LocalDateTime? = null,
    feedUrl: String
  ): JsonFeed {
    val corrId = coroutineContext.corrId()
    return if (standaloneSupport(ts)) {
      val source = SourceEntity()
      source.title = "Feed from $url"
      val fetch = FetchActionEntity()
      fetch.pos = 1
      fetch.forcePrerender = prerender
      fetch.url = url
      fetch.isVariable = false
      source.actions.add(fetch)

      val scrapeOutput = scrapeService.scrape(source, LogCollector())

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

      val document = HtmlUtil.parseHtml(
        scrapeOutput.outputs.find { o -> o.fetch != null }!!.fetch!!.response.responseBody.toString(
          StandardCharsets.UTF_8
        ), url
      )
      val feed = webToFeedTransformer.getFeedBySelectors(
        selectors,
        document,
        URI(url),
        LogCollector()
      )
      feed.title = StringUtils.trimToNull(document.title()) ?: "Feed"
      feed.feedUrl = feedUrl
      feed.websiteUrl = url

      try {
        filter?.let {
          val params = convertFilterStringToPluginParams(it)
          feed.items = feed.items.filterIndexed { index, jsonItem ->
            filterPlugin.filterEntity(
              jsonItem,
              params.toEntity(),
              index,
              LogCollector()
            )
          }
        }
      } catch (e: Throwable) {
        log.warn("[$corrId] webToFeed failed: ${e.message}", e)
      }

      appendNotifications(feed)
    } else {
      createEolFeed(feedUrl)
    }
  }

  private fun convertFilterStringToPluginParams(jsonOrExpressionFilter: String): PluginExecutionParamsInput {
    return PluginExecutionParamsInput(
      org_feedless_filter = try {
        stringToArray(jsonOrExpressionFilter, Array<ItemFilterParamsInput>::class.java)
      } catch (e: Exception) {
        listOf(
          ItemFilterParamsInput(
            expression = jsonOrExpressionFilter,
          )
        )
      }
    )
  }

  private fun <T> stringToArray(s: String, clazz: Class<Array<T>>): List<T> {
    val arr: Array<T> = Gson().fromJson(s, clazz)
    return arr.toList()
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun transformFeed(nativeFeedUrl: String, filter: String?, ts: LocalDateTime? = null, feedUrl: String): JsonFeed {
    return if (standaloneSupport(ts)) {
      val feed = feedParserService.parseFeedFromUrl(nativeFeedUrl)
      filter?.let {
        val params = convertFilterStringToPluginParams(filter)
        feed.items = feed.items.filterIndexed { index, jsonItem ->
          filterPlugin.filterEntity(
            jsonItem,
            params.toEntity(),
            index,
            LogCollector()
          )
        }
      }

      appendNotifications(feed)
      feed
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

  private suspend fun appendNotifications(feed: JsonFeed): JsonFeed {
    val corrId = coroutineContext.corrId()
    val root = userService.findAdminUser()
    repositoryService.findByTitleAndOwnerId(getRepoTitleForStandaloneFeedNotifications(), root!!.id)?.let { repo ->
      val pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "publishedAt"))
      val documents = documentService.findAllByRepositoryId(
        repo.id,
        status = ReleaseStatus.released,
        pageable = pageable,
      )
        .map {
          it.publishedAt = LocalDateTime.now()
          it.asJsonItem()
        }

      feed.items = documents.plus(feed.items)
    } ?: log.error("[$corrId] Repo for standalone notification not found")
    return feed
  }

  suspend fun standaloneSupport(ts: LocalDateTime?): Boolean {
//    return environment.acceptsProfiles(Profiles.of(AppProfiles.selfHosted)) || (ts != null && ts.isAfter(LocalDateTime.now().minusMonths(2)))
//    return !featureService.isDisabled(FeatureName.legacyApiBool)
    // todo enable some time
    return true
  }

  private fun createEolFeed(feedUrl: String): JsonFeed {
    val feed = JsonFeed()
    feed.id = "rss-proxy:2"
    feed.title = "End Of Trial"
    feed.feedUrl = feedUrl
    feed.expired = true
    feed.publishedAt = LocalDateTime.now()
    feed.items = listOf(createEolArticle(feedUrl))

    return feed
  }

  suspend fun createErrorFeed(feedUrl: String, t: Throwable): JsonFeed {
    val feed = JsonFeed()
    feed.id = "rss-proxy:2"
    feed.title = "Feed"
    feed.feedUrl = feedUrl
    feed.expired = false
    feed.publishedAt = LocalDateTime.now()

    feed.items = if (t is ResumableHarvestException || t is TooManyConnectionsPerHostException) {
      emptyList()
    } else {
      listOf(createErrorArticle(t, feedUrl))
    }

    return feed
  }

  private fun createEolArticle(feedUrl: String): JsonItem {
    val article = JsonItem()
    val feedActivationLink = "${propertyService.appHost}?url=${URLEncoder.encode(feedUrl, StandardCharsets.UTF_8)}"
    article.id = FeedUtil.toURI("end-of-life", feedActivationLink)
    article.title = "ACTION REQUIRED – Reenable Your Feed"
    article.html = """<p>Dear user, 2 month trial is over, and this feed is no longer being served (╥﹏╥).</p>
      <p>If you liked it, restore your feed simply by <a href="$feedActivationLink">reenabling it</a>, it is just a couple of clicks.</p>
<p>Reenable your feed today to get back to seamless service.</p>
<p>Thanks!</p>
    """.trimIndent()
//    article.text = "Thanks for using rssproxy or feedless. I have terminated the service has has ended. You may migrate to the latest version using this link $migrationUrl"
    article.url = feedActivationLink
    article.publishedAt = LocalDateTime.now()
    return article
  }

  private suspend fun createErrorArticle(t: Throwable, feedUrl: String): JsonItem {
    val corrId = coroutineContext.corrId()
    val article = JsonItem()
    article.id = FeedUtil.toURI("error", DateUtils.truncate(Date(), Calendar.MONTH).time.toString() + t.message)
    article.title = "ALERT: Potential Issue with Feed"
    article.text = """Dear User,
an error occurred while fetching your feed: '${t.message}'. This may require your attention. Please note, this error will only be reported once per month.
If you believe this is a bug, maybe related with the new release, here is the stack trace so you can report it.

---
Feed URL: $feedUrl
corrId: $corrId
${StringUtils.truncate(t.stackTraceToString(), 800)}
""".trimIndent()
    article.html = """<p>Dear User,</p>
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
    article.publishedAt = LocalDateTime.now()
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
