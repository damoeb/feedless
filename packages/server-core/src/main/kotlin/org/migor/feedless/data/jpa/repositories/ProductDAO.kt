package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.ProductEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface ProductDAO : JpaRepository<ProductEntity, UUID> {
  fun findByNameAndOwnerId(name: String, id: UUID): ProductEntity?

}
