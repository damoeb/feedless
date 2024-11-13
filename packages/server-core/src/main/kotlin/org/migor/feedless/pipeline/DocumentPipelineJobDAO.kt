package org.migor.feedless.pipeline

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
interface DocumentPipelineJobDAO : JpaRepository<DocumentPipelineJobEntity, UUID> {
  @Query(
    nativeQuery = true,
    value = """
      select p.* from t_pipeline_job p
      where p.terminated = false
      and p.document_id in (
        select g.document_id
        from (
            select distinct on (document_id)
                    document_id, cool_down_until
            from t_pipeline_job
            where terminated = false AND document_id IS NOT NULL
            order by document_id, sequence_id
        ) g
      where g.cool_down_until is null
         or g.cool_down_until < :now)
      order by document_id, sequence_id
      limit 100
    """
  )
  fun findAllPendingBatched(@Param("now") now: LocalDateTime): List<DocumentPipelineJobEntity>
  fun deleteAllByCreatedAtBefore(date: LocalDateTime)

  fun deleteAllByDocumentIdIn(ids: List<UUID>)

//  fun deleteAllByDocumentId(documentId: UUID)
}
