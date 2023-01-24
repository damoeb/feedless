package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.GenericFeedEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GenericFeedDAO : CrudRepository<GenericFeedEntity, UUID> {
  fun findByManagingFeedId(id: UUID): Optional<GenericFeedEntity>
  fun findAllByWebsiteUrl(websiteUrl: String?, pageable: Pageable): Page<GenericFeedEntity>
}
