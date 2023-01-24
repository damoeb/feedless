package org.migor.rich.rss.service

import org.junit.platform.commons.util.StringUtils
import org.migor.rich.rss.data.es.documents.ContentDocument
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.repositories.ContentRepository
import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.database.repositories.StreamDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
class NativeFeedService {
  private val log = LoggerFactory.getLogger(NativeFeedService::class.simpleName)

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  @Autowired
  lateinit var contentRepository: ContentRepository

  fun createNativeFeed(title: String, description: String?, feedUrl: String, websiteUrl: String, harvestItems: Boolean, harvestSiteWithPrerender: Boolean): NativeFeedEntity {
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
    nativeFeed.harvestItems = false
    nativeFeed.harvestSiteWithPrerender = harvestSiteWithPrerender

    return this.index(nativeFeedDAO.save(nativeFeed))
  }

  private fun index(nativeFeedEntity: NativeFeedEntity): NativeFeedEntity {
    val doc = ContentDocument()
    doc.id = nativeFeedEntity.id
    doc.type = ContentDocumentType.NATIVE_FEED
    doc.body = nativeFeedEntity.description + nativeFeedEntity.websiteUrl
    doc.title = nativeFeedEntity.title
    doc.url = nativeFeedEntity.feedUrl
    contentRepository.save(doc)

    return nativeFeedEntity
  }

  fun delete(id: UUID) {
    nativeFeedDAO.deleteById(id)
  }

    fun findByFeedUrl(feedUrl: String): Optional<NativeFeedEntity> {
      return nativeFeedDAO.findByFeedUrl(feedUrl)
    }

}
