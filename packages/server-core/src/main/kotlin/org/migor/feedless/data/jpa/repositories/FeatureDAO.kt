package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.FeatureEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface FeatureDAO : JpaRepository<FeatureEntity, UUID> {
  fun findByProductIdAndName(id: UUID, name: String): FeatureEntity?

  @Query(
    """
      select f FROM FeatureEntity f
      inner join ProductEntity p
      on f.productId = p.id
      where p.name=:product
  """
  )
  fun findByProductName(@Param("product") product: String): List<FeatureEntity>
}
