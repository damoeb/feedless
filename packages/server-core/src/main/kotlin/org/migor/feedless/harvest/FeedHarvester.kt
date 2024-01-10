package org.migor.feedless.harvest

import io.micrometer.core.instrument.DistributionSummary
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.MediaItem
import org.migor.feedless.data.jpa.models.NativeFeedEntity
import org.migor.feedless.data.jpa.models.WebDocumentAttachments
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.NativeFeedDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.service.FeedParserService
import org.migor.feedless.service.FeedService
import org.migor.feedless.service.HttpResponse
import org.migor.feedless.service.HttpService
import org.migor.feedless.service.ImporterService
import org.migor.feedless.service.PluginsService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.RetentionStrategyService
import org.migor.feedless.service.WebDocumentService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.HtmlUtil.cleanHtml
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.migor.feedless.web.WebToTextTransformer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.net.UnknownHostException
import java.util.*


@Service
@Profile(AppProfiles.database)
class FeedHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(FeedHarvester::class.simpleName)
  private lateinit var distributionSummary: DistributionSummary

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var webToTextTransformer: WebToTextTransformer

  @Autowired
  lateinit var retentionStrategyService: RetentionStrategyService

  @Autowired
  lateinit var feedParserService: FeedParserService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var pluginsService: PluginsService

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var environment: Environment

  @PostConstruct
  fun onInit() {
    this.distributionSummary = DistributionSummary
      .builder(AppMetrics.feedHarvestDelay)
      .baseUnit("ms")
      .register(meterRegistry)
  }

  // http://localhost:8080/api/web-to-feed?v=0.1&u=https%3A%2F%2Fwww.stadtaffoltern.ch%2Fanlaesseaktuelles%3Fort%3D&l=.%2Ftd%2Fa%5B1%5D&cp=%2F%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Ftable%5B1%5D%2Ftbody%5B1%5D%2Ftr&dp=.%2Ftd%2Fspan%5B2%5D&ec=&p=true&ps=&aw=load&q=&ar=NONE&
  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun harvestFeed(corrId: String, feed: NativeFeedEntity) {
    runCatching {
      log.info("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      feed.nextHarvestAt?.let {
        this.distributionSummary.record((Date().time - it.time).toDouble())
      } ?: this.distributionSummary.record((Date().time - feed.createdAt.time).toDouble())

      val fetchContext = createFetchContext(feed)
      val httpResponse = fetchFeed(corrId, fetchContext)
      val parsedFeed = feedParserService.parseFeed(corrId, HarvestResponse(fetchContext.url, httpResponse))
      if (StringUtils.isBlank(feed.iconUrl) && StringUtils.isNotBlank(feed.websiteUrl)) {
        assignFavIconUrl(corrId, parsedFeed, feed)
      }
      feedService.updateMeta(feed, parsedFeed)
      handleArticles(corrId, feed, parsedFeed.items)

    }.onFailure {
      log.error("[$corrId] feed harvest failed with ${it.message}")
//      it.printStackTrace()
      when (it) {
        is SiteNotFoundException -> feedService.changeStatus(corrId, feed, NativeFeedStatus.NOT_FOUND, it)
        is ServiceUnavailableException, is UnknownHostException -> feedService.changeStatus(corrId, feed, NativeFeedStatus.SERVICE_UNAVAILABLE, it)
        else -> feedService.updateNextHarvestDateAfterError(corrId, feed, it)
      }
    }
  }

  private fun assignFavIconUrl(corrId: String, feed: RichFeed, nativeFeed: NativeFeedEntity) {
    runCatching {
      val response = this.httpService.httpGetCaching(corrId, nativeFeed.websiteUrl!!, 200)
      val doc = parseHtml(cleanHtml(String(response.responseBody)), nativeFeed.websiteUrl!!)
      val linkElement = doc.select("link[rel~=icon]")
      linkElement.first()?.let {
        val iconUrl = URL(URL(nativeFeed.websiteUrl), it.attr("href")).toString()
        feed.iconUrl = iconUrl
        log.info("[$corrId] iconUrl= ${feed.iconUrl}")
      }
    }
  }

  private fun createFetchContext(feed: NativeFeedEntity): FetchContext {
    return FetchContext(feed.feedUrl, feed)
  }

  private fun handleArticles(
      corrId: String,
      feed: NativeFeedEntity,
      richArticles: List<RichArticle>
  ) {
    log.debug("[$corrId] handleArticles")
    feed.genericFeed?.let {
      if (richArticles.isEmpty()) {
        throw IllegalArgumentException("Generated Feed returns 0 items")
      }
    }

    val plugins = pluginsService.resolvePlugins(feed.plugins).map { it.id() }
    log.debug("[$corrId] with plugins ${plugins.joinToString(", ")}")

    val contents = richArticles.map { webDocumentDAO.findByUrlOrAliasUrl(it.url, it.url) ?: webDocumentService.save(toWebDocumentEntity(corrId, it, plugins)) }

    log.debug("[$corrId] saved")

    val neverSeenContents = contents.filter { !feedService.existsByContentInFeed(it, feed) }
    val hasUpdates = neverSeenContents.isEmpty()
    if (hasUpdates) {
      log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
    } else {
      runCatching {
        updateLastChangedAt(corrId, feed)
        log.info("[${corrId}] Appending ${contents.size} articles to feed ${propertyService.apiGatewayUrl}/feed:${feed.id}")

        val stream = feed.stream!!

        importerService.importArticleToTargets(
          corrId,
          neverSeenContents,
          stream,
          feed,
          ArticleType.feed,
          ReleaseStatus.released,
        )
      }.onFailure { log.error("[${corrId}] importArticleToTargets failed: ${it.message}") }
        .onSuccess { log.debug("[${corrId}] Appended ${neverSeenContents.size} articles to feed ${propertyService.apiGatewayUrl}/feed:${feed.id}") }
    }

    updateLastUpdatedAt(corrId, feed)
    retentionStrategyService.applyRetentionStrategy(corrId, feed)
    feedService.updateNextHarvestDate(corrId, feed, neverSeenContents.isNotEmpty())
  }

  fun updateLastUpdatedAt(corrId: String, feed: NativeFeedEntity) {
    log.debug("[$corrId] Updating lastUpdatedAt for feed=${feed.id}")
    nativeFeedDAO.updateLastUpdatedAt(feed.id, Date())
  }

  fun updateLastChangedAt(corrId: String, feed: NativeFeedEntity) {
    log.debug("[$corrId] Updating lastChangedAt for feed=${feed.id}")
    nativeFeedDAO.updateLastChangedAt(feed.id, Date())
  }

  private fun toWebDocumentEntity(corrId: String, article: RichArticle, plugins: List<String>): WebDocumentEntity {
    meterRegistry.counter(AppMetrics.createWebDocument).increment()
    val entity = WebDocumentEntity()
    entity.url = article.url
    entity.title = article.title
    entity.pendingPlugins = plugins
    entity.imageUrl = StringUtils.trimToNull(article.imageUrl)
    if (article.contentRawMime?.contains("html") == true) {
      entity.contentRaw = article.contentRaw
      entity.contentRawMime = article.contentRawMime
      val doc = parseHtml(article.contentRaw!!, article.url)
//      entity.description = webToTextTransformer.extractText(doc.body())
    } else {
      val isHtml = article.contentText.trimStart().startsWith("<") && article.contentText.trimEnd().endsWith(">")
      if (isHtml) {
        val doc = parseHtml(article.contentText, article.url)
//        entity.description = webToTextTransformer.extractText(doc.body())
        entity.contentRaw = article.contentText
        entity.contentRawMime = "text/html"
      } else {
        entity.contentRaw = article.contentRaw
        entity.contentRawMime = article.contentRawMime
//        entity.description = article.contentText
      }
    }

    entity.releasedAt = article.publishedAt
    entity.startingAt = article.startingAt
    entity.updatedAt = article.publishedAt
    entity.attachments = toAttachments(article.attachments)

    return entity
  }

  private fun toAttachments(attachments: List<JsonAttachment>): WebDocumentAttachments? {
    return if (attachments.isEmpty()) {
      null
    } else {
      WebDocumentAttachments(
        thumbnails = emptyList(),
        media = attachments.map {
          MediaItem(url = it.url, format = it.type, duration = it.duration)
        },
      )
    }
  }

  private fun fetchFeed(corrId: String, context: FetchContext): HttpResponse {
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    val request = httpService.prepareGet(context.url)
    log.debug("[$branchedCorrId] GET ${context.url}")
    return httpService.executeRequest(branchedCorrId, request, context.expectedStatusCode)
  }
}

data class FetchContext(
    val url: String,
    val feed: NativeFeedEntity,
    val expectedStatusCode: Int = 200
)
