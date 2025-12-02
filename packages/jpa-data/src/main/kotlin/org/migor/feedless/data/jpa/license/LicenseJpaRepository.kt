package org.migor.feedless.data.jpa.license

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.license.License
import org.migor.feedless.license.LicenseRepository
import org.migor.feedless.order.OrderId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("${AppProfiles.license} & ${AppLayer.repository}")
class LicenseJpaRepository(private val licenseDAO: LicenseDAO) : LicenseRepository {

  override suspend fun findAllByOrderId(orderId: OrderId): List<License> {
    return withContext(Dispatchers.IO) {
      licenseDAO.findAllByOrderId(orderId.uuid).map { it.toDomain() }
    }
  }

  override suspend fun save(license: License): License {
    return withContext(Dispatchers.IO) {
      licenseDAO.save(license.toEntity()).toDomain()
    }
  }

}
