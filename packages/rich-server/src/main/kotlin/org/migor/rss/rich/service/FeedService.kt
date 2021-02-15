package org.migor.rss.rich.service

import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.repository.FeedRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Transactional
  fun updatePubDate(feed: Feed) {
//    log.info("Updating pubDate for ${feed.id}")
    feedRepository.updatePubDate(feed.id!!, Date())
  }

  @Transactional
  fun updateUpdatedAt(feed: Feed) {
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  fun findById(feedId: String): FeedDto {
    return feedRepository.findById(feedId).orElseThrow().toDto()
  }

  fun findByOwnerIdAndName(ownerId: String, feedName: String): Feed {
    return feedRepository.findFirstByOwnerIdAndName(ownerId, feedName)
  }

  fun findPublicFeedByOwnerId(userId: String): Feed {
    return feedRepository.findFirstByOwnerIdAndName(userId, "public")
  }

}
