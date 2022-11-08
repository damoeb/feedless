package org.migor.rich.rss.service

import org.migor.rich.rss.database.enums.NativeFeedStatus
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.GenericFeedDAO
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.database.repositories.StreamDAO
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

  fun delete(id: UUID) {
    genericFeedDAO.deleteById(id)
  }

}
