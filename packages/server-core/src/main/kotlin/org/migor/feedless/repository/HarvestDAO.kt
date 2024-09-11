package org.migor.feedless.repository

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.repository} & ${AppLayer.repository}")
interface HarvestDAO : JpaRepository<HarvestEntity, UUID> {
  fun findAllByRepositoryId(id: UUID, pageable: PageRequest): List<HarvestEntity>

  @Modifying
  @Query("""
    WITH ranked_entities AS (
    SELECT
        id,
        repository_id,
        ROW_NUMBER() OVER (PARTITION BY repository_id ORDER BY created_at DESC) AS row_num
    FROM
        t_harvest
)
DELETE FROM t_harvest WHERE EXISTS(
    SELECT 1 FROM ranked_entities
    where row_num > 4 and t_harvest.id = ranked_entities.id
)
  """, nativeQuery = true)
  fun deleteAllTailingByRepositoryId()
}
