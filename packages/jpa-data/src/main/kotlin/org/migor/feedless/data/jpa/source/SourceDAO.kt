package org.migor.feedless.data.jpa.source

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.source} & ${AppLayer.repository}")
interface SourceDAO : JpaRepository<SourceEntity, UUID>, KotlinJdslJpqlExecutor {

//  @Query(
//    """SELECT DISTINCT s FROM SourceEntity s
//    LEFT JOIN FETCH s.actions
//    WHERE s.repositoryId = :id
//    ORDER BY s.title"""
//  )
//  fun findAllByRepositoryId(@Param("id") id: UUID, pageable: Pageable): Page<SourceEntity>

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

  fun countByRepositoryIdAndLastRecordsRetrieved(repositoryId: UUID, count: Int): Int

  @Query(
    """SELECT s FROM SourceEntity s
    LEFT JOIN FETCH s.actions
    WHERE s.id = :id"""
  )
  fun findByIdWithActions(@Param("id") sourceId: UUID): SourceEntity?
  fun countByRepositoryId(id: UUID): Long

  @Query(
    """SELECT s FROM SourceEntity s
    LEFT JOIN FETCH s.actions
    WHERE s.id in (:ids)"""
  )
  fun findAllWithActionsByIdIn(@Param("ids") ids: List<UUID>): List<SourceEntity>

  fun findAllByRepositoryIdAndIdIn(repositoryId: UUID, sourceIds: List<UUID>): List<SourceEntity>

}
