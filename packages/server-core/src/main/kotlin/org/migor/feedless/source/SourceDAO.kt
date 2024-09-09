package org.migor.feedless.source

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface SourceDAO : JpaRepository<SourceEntity, UUID> {

  @Query(
    """SELECT DISTINCT s FROM SourceEntity s
    LEFT JOIN FETCH s.actions
    WHERE s.repositoryId = :id"""
  )
  fun findAllByRepositoryIdOrderByCreatedAtDesc(@Param("id") id: UUID): List<SourceEntity>

  @Modifying
  @Query(
      """
      update SourceEntity C
        set C.disabled = :erroneous,
            C.lastErrorMessage = :errorMessage
      where C.id = :id
    """
  )
  fun setErrorState(
    @Param("id") id: UUID,
    @Param("erroneous") erroneous: Boolean,
    @Param("errorMessage") errorMessage: String? = null
  )

  fun existsByRepositoryIdAndDisabledTrue(id: UUID): Boolean
}
