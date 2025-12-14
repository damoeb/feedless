package org.migor.feedless.feed

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.asynchttpclient.exception.TooManyConnectionsPerHostException
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.PageableRequest
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.SortableRequest
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.common.PropertyService
import org.migor.feedless.config.CacheNames
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.ItemFilterParams
import org.migor.feedless.pipeline.plugins.asJsonItem
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryClaim
import org.migor.feedless.repository.RepositoryClaimId
import org.migor.feedless.repository.RepositoryClaimRepository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.scrape.GenericFeedSelectors
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.scrape.ScrapeService
import org.migor.feedless.scrape.WebToFeedTransformer
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceId
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.corrId
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.*

typealias ShipFeedItems = Boolean;


@Service
@Profile("${AppProfiles.feed} & ${AppLayer.service}")
class FeedService(
  private val propertyService: PropertyService,
  private val webToFeedTransformer: WebToFeedTransformer,
  private val feedParserService: FeedParserService,
  private val scrapeService: ScrapeService,
  private val authService: AuthService,
  private val documentUseCase: DocumentUseCase,
  private val documentRepository: DocumentRepository,
  private val filterPlugin: CompositeFilterPlugin,
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val repositoryClaimRepository: RepositoryClaimRepository,
  private val repositoryRepository: RepositoryRepository,
  private val featureService: FeatureService,
  private val sourceRepository: SourceRepository,
) {

  val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun getFeed(sourceId: SourceId, feedUrl: String): JsonFeed {
    val feed = sourceRepository.findById(sourceId)?.toJsonFeed(feedUrl) ?: throw NotFoundException("feedId not found")

    val sortable = SortableRequest("publishedAt", false)
    val pageable = PageableRequest(pageNumber = 0, pageSize = 10, sortBy = listOf(sortable));
    feed.items = documentRepository.findAllBySourceId(sourceId, pageable).map { it.asJsonItem() }

    return feed
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun webToFeed(
    url: String,
    selectors: GenericFeedSelectors,
    prerender: Boolean,
    filter: String?,
    token: String? = null,
    feedUrl: String
  ): JsonFeed {
    val fromUrl = suspend { fetchFeedFromUrl(url, prerender, selectors, feedUrl) }
    return resolveFeed(fromUrl, feedUrl, token, filter)
  }

  private suspend fun resolveFeed(
    fromUrlLazy: suspend () -> JsonFeed,
    publicFeedUrl: String,
    token: String? = null,
    filter: String? = null,
  ): JsonFeed {

    return try {
      val hasToken = token != null
      val requiresToken = featureService.isDisabled(FeatureName.legacyFeedApiBool)

      val claim = resolveClaim(token)
      val hasExpiredTrial = claim?.createdAt?.isBefore(LocalDateTime.now().minusDays(30)) ?: requiresToken
      val hasValidContract = claim?.repositoryId?.let { hasUserValidContract(claim.repositoryId!!) } ?: false

      val fromRepository = suspend { fetchFeedFromRepository(claim!!.repositoryId!!, publicFeedUrl) }
      val fromUrl = suspend { fromUrlLazy().applyFilter(filter) }
      val continuePostTokenEval = suspend {
        if (hasExpiredTrial) {
          if (hasValidContract) {
            fromRepository().appendWelcomeMessage(token)
          } else {
            // show no-subscription message
            sendNoSubscriptionMessage()
          }
        } else {
          // still on trial
          if (claim?.repositoryId == null) {
            fromUrl().appendWelcomeMessage(token)
          } else {
            fromRepository()
          }
        }
      }

      if (requiresToken) {
        if (hasToken) {
          continuePostTokenEval()
        } else {
          sendEolMessage()
        }
      } else {
        continuePostTokenEval()
      }
    } catch (e: Exception) {
      createErrorFeed(publicFeedUrl, e)
    }
  }

  private fun hasUserValidContract(repositoryId: RepositoryId): Boolean {
    // TODO("Not yet implemented")
    return true
  }

  private suspend fun fetchFeedFromRepository(repositoryId: RepositoryId, feedUrl: String): JsonFeed {
    val feed = repositoryRepository.findById(repositoryId)?.toJsonFeed(feedUrl)
      ?: throw NotFoundException("repositoryId not found")

    feed.items =
      documentUseCase.findAllByRepositoryId(
        repositoryId,
        pageable = PageableRequest(
          pageNumber = 0,
          pageSize = 10,
          sortBy = listOf(SortableRequest("publishedAt", false))
        )
      )
        .map { it.asJsonItem() }

    return feed
  }

  private suspend fun JsonFeed.appendWelcomeMessage(token: String?): JsonFeed {
    // todo implement
    return this
  }

  private suspend fun fetchFeedFromUrl(
    url: String,
    prerender: Boolean,
    selectors: GenericFeedSelectors,
    feedUrl: String,
  ): JsonFeed {
    val sourceId = SourceId()
    val source = Source(
      id = sourceId,
      title = "Feed from $url",
      actions = listOf(
        FetchAction(
          sourceId = sourceId,
          pos = 1,
          forcePrerender = prerender,
          url = url,
          isVariable = false
        )
      )
    )
    val scrapeOutput = scrapeService.scrape(source, LogCollector())

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

    return feed
  }

  private suspend fun JsonFeed.applyFilter(filter: String?): JsonFeed {
    filter?.let {
      items = items.filterIndexed { index, jsonItem ->
        filterPlugin.filterEntity(
          jsonItem,
          convertFilterStringToPluginParams(it),
          index,
          LogCollector()
        )
      }
    }
    return this
  }

  private fun convertFilterStringToPluginParams(jsonOrExpressionFilter: String): List<ItemFilterParams> {
    return try {
      stringToArray(jsonOrExpressionFilter, Array<ItemFilterParams>::class.java)
    } catch (e: Exception) {
      listOf(
        ItemFilterParams(
          expression = jsonOrExpressionFilter,
        )
      )
    }
  }

  private fun <T> stringToArray(s: String, clazz: Class<Array<T>>): List<T> {
    val arr: Array<T> = Gson().fromJson(s, clazz)
    return arr.toList()
  }

  @Cacheable(value = [CacheNames.FEED_LONG_TTL], key = "\"feed/\" + #feedUrl")
  suspend fun transformFeed(
    nativeFeedUrl: String,
    filter: String?,
    token: String? = null,
    feedUrl: String
  ): JsonFeed {

    val feedFromUrlLazy = suspend { feedParserService.parseFeedFromUrl(nativeFeedUrl) }

    return resolveFeed(feedFromUrlLazy, nativeFeedUrl, token, filter)
  }

  fun getRepository(repositoryId: String): ResponseEntity<String> {
    val headers = HttpHeaders()
    headers.add("Location", "/f/$repositoryId/atom")
    return ResponseEntity(headers, HttpStatus.FOUND)
  }

  // --

  private suspend fun appendNotifications(feed: JsonFeed, feedSupport: FeedMessage, jwt: Jwt? = null): JsonFeed {
    when (feedSupport) {
      FeedMessage.NO_TOKEN_FEED -> ""
      FeedMessage.WELCOME_MESSAGE -> ""
      FeedMessage.END_OF_LIFE_FEED -> ""
      FeedMessage.EXPIRED_FEED -> ""
    }

    return feed
  }

  suspend fun resolveClaim(token: String?): RepositoryClaim? {
    val jwt = try {
      token?.let { authService.parseAndVerify(token)!! }
    } catch (_: Throwable) {
      null
    }
    if (jwt != null) {
      try {
        return repositoryClaimRepository.findById(RepositoryClaimId(jwt.getClaimAsString("id")))
      } catch (e: Exception) {
        log.error("Error resolving id claim from token: ${e.message}")
      }
    }
    return null
  }

//    private suspend fun createEolFeed(feedUrl: String): JsonFeed {
//      val feed = JsonFeed()
//      feed.id = "rss-proxy:2"
//      feed.title = "End Of Trial"
//      feed.feedUrl = feedUrl
//      feed.expired = true
//      feed.publishedAt = LocalDateTime.now()
//
//      appendNotifications(feed, FeedMessage.END_OF_LIFE_FEED)
//
//      return feed
//    }

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
      listOf(createErrorMessage(t, feedUrl))
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

  private fun createFeedMessage(feedUrl: String): JsonItem {
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

  private suspend fun createErrorMessage(t: Throwable, feedUrl: String): JsonItem {
    val corrId = currentCoroutineContext().corrId()
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

  suspend fun createAnonymousFeedUrl(baseUri: URI): AuthToken = withContext(Dispatchers.IO) {
    val repositoryClaim = RepositoryClaim()
    repositoryClaimRepository.save(repositoryClaim)
    val jwt = jwtTokenIssuer.createJwtForAnonymousFeed(baseUri.host, repositoryClaim.id)
    AuthToken(token = jwt.tokenValue)
  }

  private fun sendEolMessage(): JsonFeed {
    TODO("Not yet implemented")
  }

  private fun sendNoSubscriptionMessage(): JsonFeed {
    TODO("Not yet implemented")
  }

  private fun Source.toJsonFeed(feedUrl: String): JsonFeed {
    val feed = JsonFeed()
    feed.id = "legacy-feed"
    feed.title = title
    feed.publishedAt = createdAt
    feed.items = emptyList()
    feed.feedUrl = feedUrl
    return feed
  }

  private fun Repository.toJsonFeed(feedUrl: String): JsonFeed {
    TODO("Not yet implemented")
  }
}
