package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.GenericFeedEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface GenericFeedDAO : JpaRepository<GenericFeedEntity, UUID> {
  fun findByNativeFeedId(id: UUID): Optional<GenericFeedEntity>
  fun findAllByWebsiteUrl(websiteUrl: String?, pageable: Pageable): List<GenericFeedEntity>
}
