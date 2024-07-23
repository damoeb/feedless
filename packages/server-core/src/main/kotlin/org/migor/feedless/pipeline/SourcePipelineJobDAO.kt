package org.migor.feedless.pipeline

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface SourcePipelineJobDAO : JpaRepository<SourcePipelineJobEntity, UUID> {

  @Query(
    """
    select K from SourcePipelineJobEntity K
    inner join SourceEntity S on S.id = K.sourceId
    where S.repositoryId = :id
    ORDER BY K.createdAt DESC
  """
  )
  fun findAllByRepositoryId(@Param("id") id: UUID): List<SourcePipelineJobEntity>

  @Query(
    nativeQuery = true,
    value = """
      select p.* from t_pipeline_job p
      where p.terminated = false
      and p.source_id in (
        select g.source_id
        from (
            select distinct on (source_id)
                    source_id, cool_down_until
            from t_pipeline_job
            where terminated = false AND source_id IS NOT NULL
            order by source_id, sequence_id
        ) g
      where g.cool_down_until is null
         or g.cool_down_until < current_timestamp)
      order by source_id, sequence_id
      limit 100
    """
  )
  fun findAllPendingBatched(): List<SourcePipelineJobEntity>
  fun deleteAllByCreatedAtBefore(date: Date)
  fun existsBySourceIdAndUrl(sourceId: UUID, url: String): Boolean


}
