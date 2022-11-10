package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.GenericFeedStatus
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.repositories.GenericFeedDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.GenericFeedCreateInputDto
import org.migor.rich.rss.transform.GenericFeedRule
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenericFeedService {
  private val log = LoggerFactory.getLogger(GenericFeedService::class.simpleName)

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  fun delete(id: UUID) {
    genericFeedDAO.deleteById(id)
  }

  fun findById(id: UUID): Optional<GenericFeedEntity> {
    return genericFeedDAO.findById(id)
  }

  fun findByNativeFeedId(nativeFeedId: UUID): Optional<GenericFeedEntity> {
    return genericFeedDAO.findByManagingFeedId(nativeFeedId)
  }

  fun createGenericFeed(data: GenericFeedCreateInputDto): GenericFeedEntity {
    val genericFeedRule = JsonUtil.gson.fromJson(data.feedRule, GenericFeedRule::class.java)
    val feedRule = feedDiscoveryService.asExtendedRule(newCorrId(), data.websiteUrl, genericFeedRule)

    val nativeFeed = nativeFeedService.createNativeFeed(
      data.title,
      data.description,
      feedRule.feedUrl,
      data.websiteUrl,
      data.harvestSite,
      data.harvestSiteWithPrerender
    )

    val genericFeed = GenericFeedEntity()
    genericFeed.feedRule = feedRule
    genericFeed.managingFeed = nativeFeed
    genericFeed.status = GenericFeedStatus.OK

    return genericFeedDAO.save(genericFeed)
  }

}
