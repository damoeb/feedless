package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.AttachmentEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HttpResponse
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PluginsService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.WebDocumentService
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.HtmlUtil.cleanHtml
import org.migor.rich.rss.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URL


@Service
@Profile(AppProfiles.database)
class FeedHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(FeedHarvester::class.simpleName)

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var feedService: FeedService

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
  lateinit var environment: Environment

  // http://localhost:8080/api/web-to-feed?v=0.1&u=https%3A%2F%2Fwww.stadtaffoltern.ch%2Fanlaesseaktuelles%3Fort%3D&l=.%2Ftd%2Fa%5B1%5D&cp=%2F%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Ftable%5B1%5D%2Ftbody%5B1%5D%2Ftr&dp=.%2Ftd%2Fspan%5B2%5D&ec=&p=true&ps=&aw=load&q=&ar=NONE&

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun harvestFeed(corrId: String, feed: NativeFeedEntity) {
    runCatching {
      log.debug("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val fetchContext = createFetchContext(feed)
      val httpResponse = fetchFeed(corrId, fetchContext)
      val parsedFeed = feedService.parseFeed(corrId, HarvestResponse(fetchContext.url, httpResponse))
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
        is ServiceUnavailableException -> feedService.changeStatus(corrId, feed, NativeFeedStatus.SERVICE_UNAVAILABLE, it)
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

    val plugins = pluginsService.resolvePlugins(feed.harvestItems, feed.inlineImages).map { it.id() }
    log.debug("[$corrId] with plugins ${plugins.joinToString(", ")}")

    val contents = richArticles.map { webDocumentDAO.findByUrl(it.url) ?: webDocumentService.save(toContentEntity(corrId, it, plugins)) }

    log.debug("[$corrId] saved")

    feedService.updateLastUpdatedAt(corrId, feed)
    feedService.applyRetentionStrategy(corrId, feed)

    val neverSeenContents = contents.filter { !feedService.existsByContentInFeed(it, feed) }
    val hasUpdates = neverSeenContents.isEmpty()
    if (hasUpdates) {
      log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
    } else {
      runCatching {
        feedService.updateLastChangedAt(corrId, feed)
        log.debug("[${corrId}] Appending ${contents.size} articles to feed ${propertyService.apiGatewayUrl}/feed:${feed.id}")

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
        .onSuccess { log.info("[${corrId}] Appended ${neverSeenContents.size} articles to feed ${propertyService.apiGatewayUrl}/feed:${feed.id}") }
    }
    feedService.updateNextHarvestDate(corrId, feed, neverSeenContents.isNotEmpty())

//    harvestTaskDAO.saveAll(harvestTasks)
//    webGraphService.recordOutgoingLinks(corrId, unharvestableContents)
  }

  private fun toContentEntity(corrId: String, article: RichArticle, plugins: List<String>): WebDocumentEntity {
    val entity = WebDocumentEntity()
    entity.url = article.url
    entity.title = article.title
    entity.plugins = plugins
    entity.imageUrl = StringUtils.trimToNull(article.imageUrl)
    val isHtml = article.contentText.trimStart().startsWith("<")
    if (isHtml) {
      val document = parseHtml(article.contentText, article.url)
      entity.description = document.text()
      entity.contentRaw = document.body().html()
      entity.contentRawMime = "text/html"
    } else {
      entity.description = article.contentText
    }

    entity.releasedAt = article.publishedAt
    entity.startingAt = article.startingAt
    entity.updatedAt = article.publishedAt
    entity.attachments = article.attachments.map { toAttachment(it) }

    return entity
  }

  private fun toAttachment(enclosure: JsonAttachment): AttachmentEntity {
    val attachment = AttachmentEntity()
    attachment.url = enclosure.url
    attachment.mimeType = enclosure.type
    attachment.size = enclosure.size
    attachment.duration = enclosure.duration
    return attachment
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
