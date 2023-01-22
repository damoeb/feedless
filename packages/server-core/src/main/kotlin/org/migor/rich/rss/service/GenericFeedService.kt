package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.GenericFeedStatus
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.repositories.GenericFeedDAO
import org.migor.rich.rss.generated.GenericFeedCreateInputDto
import org.migor.rich.rss.transform.WebToFeedTransformer
import org.migor.rich.rss.util.GenericFeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
class GenericFeedService {
  private val log = LoggerFactory.getLogger(GenericFeedService::class.simpleName)

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

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
    val feedSpecification = GenericFeedUtil.fromDto(data.specification)

    val feedUrl = webToFeedTransformer.createFeedUrl(
      URL(data.websiteUrl),
      feedSpecification.selectors!!,
      feedSpecification.parserOptions,
      feedSpecification.fetchOptions,
      feedSpecification.refineOptions
    )

    val nativeFeed = nativeFeedService.createNativeFeed(
      data.title,
      data.description,
      feedUrl,
      data.websiteUrl,
      data.harvestSiteWithPrerender
    )

    val genericFeed = GenericFeedEntity()
    genericFeed.feedSpecification = feedSpecification
    genericFeed.managingFeed = nativeFeed
    genericFeed.managingFeedId = nativeFeed.id
    genericFeed.status = GenericFeedStatus.OK

    return genericFeedDAO.save(genericFeed)
  }

}
