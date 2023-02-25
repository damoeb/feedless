package org.migor.rich.rss.service

import org.junit.platform.commons.util.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.repositories.NativeFeedDAO
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
@Profile(AppProfiles.database)
class NativeFeedService {
  private val log = LoggerFactory.getLogger(NativeFeedService::class.simpleName)

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var contentService: ContentService

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  fun createNativeFeed(
    corrId: String, title: String, description: String?, feedUrl: String, websiteUrl: String, autoRelease: Boolean,
    harvestItems: Boolean, harvestSiteWithPrerender: Boolean
  ): NativeFeedEntity {
    val stream = streamDAO.save(StreamEntity())

    val nativeFeed = NativeFeedEntity()
    nativeFeed.title = title
    nativeFeed.feedUrl = feedUrl
    nativeFeed.description = description
    if (StringUtils.isNotBlank(websiteUrl)) {
      nativeFeed.domain = URL(websiteUrl).host
      nativeFeed.websiteUrl = websiteUrl
    }
    nativeFeed.status = NativeFeedStatus.OK
    nativeFeed.stream = stream
    nativeFeed.autoRelease = autoRelease
    nativeFeed.harvestItems = harvestItems
    nativeFeed.harvestSiteWithPrerender = harvestSiteWithPrerender

    return this.index(nativeFeedDAO.save(nativeFeed))
  }

  private fun index(nativeFeedEntity: NativeFeedEntity): NativeFeedEntity {
    val doc = FulltextDocument()
    doc.id = nativeFeedEntity.id
    doc.type = ContentDocumentType.NATIVE_FEED
    doc.body = nativeFeedEntity.description + nativeFeedEntity.websiteUrl
    doc.title = nativeFeedEntity.title
    doc.url = nativeFeedEntity.feedUrl
    if (environment.acceptsProfiles(Profiles.of("!${AppProfiles.database}"))) {
      fulltextDocumentService.save(doc)
    }

    return nativeFeedEntity
  }

  fun delete(corrId: String, id: UUID) {
    nativeFeedDAO.deleteById(id)
  }

  fun findByFeedUrl(feedUrl: String): Optional<NativeFeedEntity> {
    return nativeFeedDAO.findByFeedUrl(feedUrl)
  }

}
