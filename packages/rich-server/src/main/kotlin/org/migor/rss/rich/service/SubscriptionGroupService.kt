package org.migor.rss.rich.service

import org.migor.rss.rich.dto.SubscriptionGroupDto
import org.migor.rss.rich.model.SubscriptionGroup
import org.migor.rss.rich.repository.SubscriptionGroupRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SubscriptionGroupService {

  private val log = LoggerFactory.getLogger(SubscriptionGroupService::class.simpleName)

  @Autowired
  lateinit var subscriptionGroupRepository: SubscriptionGroupRepository

  fun findAllByOwnerId(userId: String): List<SubscriptionGroupDto> {
    return subscriptionGroupRepository.findAllByOwnerIdOrderByNameAsc(userId).map { group: SubscriptionGroup ->
      group.toDto()
    }
  }

  fun findById(groupId: String): SubscriptionGroupDto {
    return subscriptionGroupRepository.findById(groupId).orElseThrow().toDto()
  }

}
