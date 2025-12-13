package org.migor.feedless.data.jpa.license

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

  override fun findAllByOrderId(orderId: OrderId): List<License> {
    return licenseDAO.findAllByOrderId(orderId.uuid).map { it.toDomain() }
  }

  override fun save(license: License): License {
    return licenseDAO.save(license.toEntity()).toDomain()
  }

}
