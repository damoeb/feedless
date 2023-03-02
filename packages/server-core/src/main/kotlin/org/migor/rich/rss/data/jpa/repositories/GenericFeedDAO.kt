package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.GenericFeedEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GenericFeedDAO : JpaRepository<GenericFeedEntity, UUID> {
  fun findByManagingFeedId(id: UUID): Optional<GenericFeedEntity>
  fun findAllByWebsiteUrl(websiteUrl: String?, pageable: Pageable): List<GenericFeedEntity>
}
