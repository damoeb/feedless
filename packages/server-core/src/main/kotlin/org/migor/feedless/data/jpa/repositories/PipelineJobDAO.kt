package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.PipelineJobEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface PipelineJobDAO : JpaRepository<PipelineJobEntity, UUID> {
  @Query(
    nativeQuery = true,
    value = """
      select p.* from t_pipeline_job p
      where terminated = false
      and webdocument_id in (
        select g.webdocument_id
        from (
            select distinct on (webdocument_id)
                    webdocument_id, cool_down_until
            from t_pipeline_job
            where terminated = false
            order by webdocument_id, sequence_id
            limit 20
        ) g
      where g.cool_down_until is null
         or g.cool_down_until < current_timestamp)
      order by webdocument_id, sequence_id
    """
  )
  fun findAllPendingBatched(): List<PipelineJobEntity>

}
