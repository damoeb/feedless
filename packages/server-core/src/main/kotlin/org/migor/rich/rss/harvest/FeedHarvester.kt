package org.migor.rich.rss.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.AttachmentEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.FeedService
import org.migor.rich.rss.service.HttpResponse
import org.migor.rich.rss.service.HttpService
import org.migor.rich.rss.service.ImporterService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.ScoreService
import org.migor.rich.rss.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*


@Service
@Profile("database")
class FeedHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(FeedHarvester::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

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

  fun harvestFeed(corrId: String, feed: NativeFeedEntity) {
    runCatching {
      this.log.info("[$corrId] Harvesting feed ${feed.id} (${feed.feedUrl})")
      val fetchContext = createFetchContext(feed)
      val httpResponse = fetchFeed(corrId, fetchContext)
      val parsedFeed = feedService.parseFeed(corrId, HarvestResponse(fetchContext.url, httpResponse))
      handleArticles(corrId, feed, parsedFeed.items)

    }.onFailure {
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
    val savedContents = richArticles
      .map { saveOrUpdateContent(corrId, it, feed) }
      .filter { pair: Pair<Boolean, ContentEntity> -> pair.first }
      .map { pair -> pair.second }


    if (savedContents.isEmpty()) {
      log.debug("[$corrId] Up-to-date ${feed.feedUrl}")
    } else {
      log.info("[$corrId] Appended ${savedContents.size} articles")
      feedService.updateUpdatedAt(corrId, feed)
      feedService.applyRetentionStrategy(corrId, feed)
    }

    val stream = feed.stream!!

    importerService.importArticlesToTargets(
      corrId,
      savedContents,
      stream,
      feed,
      ArticleType.feed,
      ReleaseStatus.released,
      releasedAt = null
    )

    log.info("[${corrId}] Updated feed ${propertyService.publicUrl}/feed:${feed.id}")
    feedService.updateNextHarvestDate(corrId, feed, savedContents.isNotEmpty())
  }

  private fun saveOrUpdateContent(
    corrId: String,
    richArticle: RichArticle,
    feed: NativeFeedEntity
  ): Pair<Boolean, ContentEntity> {
    val optionalEntry = Optional.ofNullable(contentDAO.findByUrl(richArticle.url))
    return if (optionalEntry.isPresent) {
      Pair(false, updateContentEntity(corrId, optionalEntry.get(), richArticle))
    } else {
      Pair(true, toContentEntity(corrId, richArticle, feed))
    }
  }

  private fun toContentEntity(corrId: String, article: RichArticle, feed: NativeFeedEntity): ContentEntity {
    val entity = ContentEntity()
    entity.url = article.url
    entity.title = article.title
    entity.imageUrl = StringUtils.trimToNull(article.imageUrl)
    entity.description = article.contentText
    entity.publishedAt = article.publishedAt
    entity.updatedAt = article.publishedAt
    entity.attachments = article.enclosures?.map { toAttachment(it) }
    return articleService.create(corrId, entity, feed)
  }

  private fun toAttachment(enclosure: RichEnclosure): AttachmentEntity {
    val attachment = AttachmentEntity()
    attachment.url = enclosure.url
    attachment.mimeType = enclosure.type
    attachment.length = enclosure.length
    return attachment
  }

  private fun updateContentEntity(
    corrId: String,
    existingContent: ContentEntity,
    richArticle: RichArticle
  ): ContentEntity {
    val changedTitle = existingContent.title.equals(richArticle.title)
    if (changedTitle) {
      existingContent.title = richArticle.title
    }
    val changedContentText = existingContent.description.equals(richArticle.contentText)
    if (changedContentText) {
      existingContent.description = richArticle.contentText
    }

    return articleService.updateContent(corrId, existingContent)
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
