package org.migor.rich.rss.service

import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.migor.rich.rss.data.jpa.repositories.GenericFeedDAO
import org.migor.rich.rss.generated.types.GenericFeedsWhereInput
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenericFeedService {
  private val log = LoggerFactory.getLogger(GenericFeedService::class.simpleName)

  @Autowired
  lateinit var genericFeedDAO: GenericFeedDAO

  fun delete(corrId: String, id: UUID) {
//    RequestContextHolder.currentRequestAttributes().getAttribute("corrId", RequestAttributes.SCOPE_REQUEST)
    log.debug("[${corrId}] delete $id")
    genericFeedDAO.deleteById(id)
  }

  fun findById(id: UUID): Optional<GenericFeedEntity> {
    return genericFeedDAO.findById(id)
  }

  fun findByNativeFeedId(nativeFeedId: UUID): Optional<GenericFeedEntity> {
    return genericFeedDAO.findByManagingFeedId(nativeFeedId)
  }

  fun findAllByFilter(where: GenericFeedsWhereInput, pageable: Pageable): List<GenericFeedEntity> {
    return genericFeedDAO.findAllByWebsiteUrl(where.websiteUrl, pageable)
  }

}
