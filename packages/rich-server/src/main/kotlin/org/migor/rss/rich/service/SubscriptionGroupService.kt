package org.migor.rss.rich.service

import org.migor.rss.rich.api.dto.FeedDto
import org.migor.rss.rich.database.repository.SubscriptionGroupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class SubscriptionGroupService {

  @Autowired
  lateinit var groupRepository: SubscriptionGroupRepository

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var propertyService: PropertyService

  fun findFeedByGroupId(groupId: String): FeedDto {
    val group = groupRepository.findById(groupId).orElseThrow { RuntimeException("group $groupId does not exit") }
    val entries = entryService.findLatestBySubscriptionGroupId(groupId)
    val lastUpdatedAt = entries.first()!!["pubDate"] as Date
    return FeedDto(groupId, group.name!!, "subscription group", lastUpdatedAt, entries, link = "${propertyService.host()}/group:${groupId}")
  }
}
