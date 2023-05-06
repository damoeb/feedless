package org.migor.rich.rss.service

import org.junit.platform.commons.util.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.auth.CurrentUser
import org.migor.rich.rss.data.es.FulltextDocumentService
import org.migor.rich.rss.data.es.documents.ContentDocumentType
import org.migor.rich.rss.data.es.documents.FulltextDocument
import org.migor.rich.rss.data.jpa.enums.NativeFeedStatus
import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.models.NativeFeedEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.NativeFeedDAO
import org.migor.rich.rss.data.jpa.repositories.StreamDAO
import org.migor.rich.rss.generated.types.NativeFeedUpdateDataInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
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
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var fulltextDocumentService: FulltextDocumentService

  @Transactional(propagation = Propagation.REQUIRED)
  fun createNativeFeed(
    corrId: String, title: String, description: String?, feedUrl: String, websiteUrl: String?,
    harvestItems: Boolean, user: UserEntity, genericFeed: GenericFeedEntity? = null
  ): NativeFeedEntity {
    log.info("[$corrId] create native feed '$feedUrl'")
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
    nativeFeed.streamId = stream.id
    nativeFeed.genericFeed = genericFeed
    nativeFeed.harvestItems = harvestItems
    nativeFeed.harvestSiteWithPrerender = false
    nativeFeed.ownerId = user.id

    val saved = nativeFeedDAO.save(nativeFeed)
    log.debug("[${corrId}] created ${saved.id}")
    return this.index(saved)
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
    log.debug("[${corrId}] delete $id")
    val feed = nativeFeedDAO.findById(id).orElseThrow {IllegalArgumentException("nativeFeed not found")}
    assertOwnership(feed.ownerId)
    nativeFeedDAO.deleteById(id)
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun findByFeedUrl(feedUrl: String): Optional<NativeFeedEntity> {
    return nativeFeedDAO.findByFeedUrl(feedUrl)
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun findById(id: UUID): Optional<NativeFeedEntity> {
    return nativeFeedDAO.findById(id)
  }

  private fun assertOwnership(ownerId: UUID?) {
    if (ownerId != currentUser.userId() && !currentUser.isAdmin()) {
      throw AccessDeniedException("insufficient privileges")
    }
  }

  fun update(corrId: String, data: NativeFeedUpdateDataInput, id: UUID): NativeFeedEntity {
    log.info("[$corrId] update $id")
    val feed = nativeFeedDAO.findById(id).orElseThrow {IllegalArgumentException("nativeFeed not found")}

    assertOwnership(feed.ownerId)

    var changed = false
    if (data.feedUrl != null) {
      feed.feedUrl = data.feedUrl.set
      changed = true
      log.info("[$corrId] set feedUrl = ${data.feedUrl.set}")
    }

    return if (changed) {
      feed.nextHarvestAt = null
      feed.status = NativeFeedStatus.OK
      nativeFeedDAO.saveAndFlush(feed)
    } else {
      log.info("[$corrId] unchanged")
      feed
    }
  }
}
