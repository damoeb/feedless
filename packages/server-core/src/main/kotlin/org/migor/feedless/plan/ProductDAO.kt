package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface ProductDAO : JpaRepository<ProductEntity, UUID> {
  fun findByName(name: String): ProductEntity?
  fun findByPartOfAndBaseProductIsTrue(name: ProductCategory): ProductEntity?
  fun findAllByPartOfOrPartOfIsNull(category: ProductCategory): List<ProductEntity>
}
