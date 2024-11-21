package org.migor.feedless.actions

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
interface ScrapeActionDAO : JpaRepository<ScrapeActionEntity, UUID> {
  fun findAllBySourceId(id: UUID): List<ScrapeActionEntity>
}
