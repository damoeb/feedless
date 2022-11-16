package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.AttachmentEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.HarvestTaskEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HarvestTaskService.Companion.isBlacklistedForHarvest
import org.migor.rich.rss.service.HttpResponse
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.ScoreService
import org.migor.rich.rss.service.WebGraphService
import org.migor.rich.rss.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Profile("database")
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

  @Transactional(readOnly = false)
  fun harvestFeed(corrId: String, feed: NativeFeedEntity) {
    runCatching {
      this.log.info("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val fetchContext = createFetchContext(feed)
      val httpResponse = fetchFeed(corrId, fetchContext)
      val parsedFeed = feedService.parseFeed(corrId, HarvestResponse(fetchContext.url, httpResponse))
      handleArticles(corrId, feed, parsedFeed.items)

    }.onFailure {
      it.printStackTrace()
      when(it) {
        is SiteNotFoundException -> feedService.changeStatus(corrId, feed, NativeFeedStatus.DEACTIVATED)
        else -> feedService.updateNextHarvestDateAfterError(corrId, feed, it)
      }
    }
  }

  private fun createFetchContext(feed: NativeFeedEntity): FetchContext {
    return FetchContext(feed.feedUrl!!, feed)
  }

  private fun handleArticles(
    corrId: String,
    feed: NativeFeedEntity,
    richArticles: List<RichArticle>
  ) {
    log.info("[$corrId] handleArticles")
    val contents = contentDAO.saveAll(richArticles.filter { !contentDAO.existsByUrl(it.url) }.map { toContentEntity(it) }).toList()
    log.info("[$corrId] saved")

    if (feed.harvestSite) {
      val harvestTasks = mutableListOf<HarvestTaskEntity>()
      val unharvastableContents = mutableListOf<ContentEntity>()

      contents.forEach {
        run {
          if (!isBlacklistedForHarvest(it.url!!) && it.url!!.startsWith("http")) {
            val harvestTask = HarvestTaskEntity()
            harvestTask.content = it
            harvestTask.feed = feed
            harvestTasks.add(harvestTask)
          } else {
            unharvastableContents.add(it)
          }
        }
      }

      harvestTaskDAO.saveAll(harvestTasks)
      webGraphService.recordOutgoingLinks(corrId, unharvastableContents)
    } else {
      webGraphService.recordOutgoingLinks(corrId, contents)
    }

    if (contents.isEmpty()) {
      log.info("[$corrId] Up-to-date ${feed.feedUrl}")
    } else {
      log.info("[$corrId] Appended ${contents.size} articles")
      feedService.updateUpdatedAt(corrId, feed)
      feedService.applyRetentionStrategy(corrId, feed)
    }

    val stream = feed.stream!!

    importerService.importArticlesToTargets(
      corrId,
      contents,
      stream,
      feed,
      ArticleType.feed,
      ReleaseStatus.released,
      releasedAt = null
    )

    log.info("[${corrId}] Updated feed ${propertyService.publicUrl}/feed:${feed.id}")
    feedService.updateNextHarvestDate(corrId, feed, contents.isNotEmpty())
  }

  private fun toContentEntity(article: RichArticle): ContentEntity {
    val entity = ContentEntity()
    entity.url = article.url
    entity.title = article.title
    entity.imageUrl = StringUtils.trimToNull(article.imageUrl)
    entity.description = article.contentText
    entity.publishedAt = article.publishedAt
    entity.updatedAt = article.publishedAt
//  todo mag fix  entity.attachments = article.enclosures?.map { toAttachment(it) }
    return entity
  }

  private fun toAttachment(enclosure: RichEnclosure): AttachmentEntity {
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
