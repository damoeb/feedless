package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.AttachmentEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.HarvestTaskEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.migor.rich.rss.data.jpa.repositories.HarvestTaskDAO
import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment
import org.migor.rich.rss.service.ContentService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HarvestTaskService.Companion.isBlacklistedForHarvest
import org.migor.rich.rss.service.HttpResponse
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.ScoreService
import org.migor.rich.rss.service.graph.WebGraphService
import org.migor.rich.rss.util.CryptUtil
import org.migor.rich.rss.util.HtmlUtil.cleanHtml
import org.migor.rich.rss.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URL


@Service
@Profile(AppProfiles.database)
class FeedHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(FeedHarvester::class.simpleName)

  @Autowired
  lateinit var harvestTaskDAO: HarvestTaskDAO

  @Autowired
  lateinit var webGraphService: WebGraphService

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var importerService: ImporterService

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var contentService: ContentService

  // http://localhost:8080/api/web-to-feed?v=0.1&u=https%3A%2F%2Fwww.stadtaffoltern.ch%2Fanlaesseaktuelles%3Fort%3D&l=.%2Ftd%2Fa%5B1%5D&cp=%2F%2Fdiv%5B1%5D%2Fsection%5B1%5D%2Fdiv%5B3%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B1%5D%2Fdiv%5B2%5D%2Fdiv%5B1%5D%2Ftable%5B1%5D%2Ftbody%5B1%5D%2Ftr&dp=.%2Ftd%2Fspan%5B2%5D&ec=&p=true&ps=&aw=load&q=&ar=NONE&

  @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
  fun harvestFeed(corrId: String, feed: NativeFeedEntity) {
    runCatching {
      this.log.info("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val fetchContext = createFetchContext(feed)
      val httpResponse = fetchFeed(corrId, fetchContext)
      val parsedFeed = feedService.parseFeed(corrId, HarvestResponse(fetchContext.url, httpResponse))
      if (StringUtils.isBlank(feed.iconUrl) && StringUtils.isNotBlank(feed.websiteUrl)) {
        assignFavIconUrl(corrId, parsedFeed, feed)
      }
      feedService.updateMeta(feed, parsedFeed)
      handleArticles(corrId, feed, parsedFeed.items)

    }.onFailure {
      when (it) {
        is SiteNotFoundException -> feedService.changeStatus(corrId, feed, NativeFeedStatus.DEACTIVATED)
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
    feed.managedBy?.let {
      if (richArticles.isEmpty()) {
        throw IllegalArgumentException("Generated Feed returns 0 items")
      }
    }
    val contents = contentService.saveAll(richArticles.filter { !contentDAO.existsByUrl(it.url) }
      .map { toContentEntity(corrId, it, feed.inlineImages) }).toList()
    log.debug("[$corrId] saved")

    val harvestTasks = mutableListOf<HarvestTaskEntity>()
    val unharvestableContents = mutableListOf<ContentEntity>()

    if (feed.harvestItems) {
      contents.forEach {
        run {
          if (!isBlacklistedForHarvest(it.url) && it.url.startsWith("http")) {
            val harvestTask = HarvestTaskEntity()
            harvestTask.content = it
            harvestTask.feed = feed
            harvestTasks.add(harvestTask)
          } else {
            unharvestableContents.add(it)
          }
        }
      }
    } else {
      unharvestableContents.addAll(contents)
    }

    if (contents.isEmpty()) {
      log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
    } else {
      log.debug("[$corrId] Appending ${contents.size} articles")
    }
    feedService.updateUpdatedAt(corrId, feed)
    feedService.applyRetentionStrategy(corrId, feed)

    val stream = feed.stream!!

    contents.forEach {
      importerService.importArticleToTargets(
        corrId,
        it,
        stream,
        feed,
        ArticleType.feed,
        ReleaseStatus.released,
        it.publishedAt
      )
    }

    log.info("[${corrId}] Updated feed ${propertyService.publicUrl}/feed:${feed.id}")
    feedService.updateNextHarvestDate(corrId, feed, contents.isNotEmpty())

    harvestTaskDAO.saveAll(harvestTasks)
    webGraphService.recordOutgoingLinks(corrId, unharvestableContents)
  }

  private fun toContentEntity(corrId: String, article: RichArticle, inlineImages: Boolean): ContentEntity {
    val entity = ContentEntity()
    entity.url = article.url
    entity.title = article.title
    entity.imageUrl = StringUtils.trimToNull(article.imageUrl)
    val isHtml = article.contentText.trimStart().startsWith("<")
    if (isHtml) {
      val document = parseHtml(article.contentText, article.url)
      entity.description = document.text()
      entity.contentRaw = if (inlineImages) {
        contentService.inlineImages(corrId, document)
      } else {
        document.body().html()
      }
      entity.contentRawMime = "text/html"
    } else {
      entity.description = article.contentText
    }

    entity.publishedAt = article.publishedAt
    entity.startingAt = article.startingAt
    entity.updatedAt = article.publishedAt
    entity.attachments = article.attachments.map { toAttachment(it) }

    return entity
  }

  private fun toAttachment(enclosure: JsonAttachment): AttachmentEntity {
    val attachment = AttachmentEntity()
    attachment.url = enclosure.url
    attachment.mimeType = enclosure.type
    attachment.length = enclosure.length
    return attachment
  }

  private fun fetchFeed(corrId: String, context: FetchContext): HttpResponse {
    val branchedCorrId = CryptUtil.newCorrId(parentCorrId = corrId)
    val request = httpService.prepareGet(context.url)
    log.info("[$branchedCorrId] GET ${context.url}")
    return httpService.executeRequest(branchedCorrId, request, context.expectedStatusCode)
  }
}

data class FetchContext(
    val url: String,
    val feed: NativeFeedEntity,
    val expectedStatusCode: Int = 200
)
