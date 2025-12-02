package org.migor.feedless.license

import org.migor.feedless.order.OrderId

interface LicenseRepository {
  suspend fun findAllByOrderId(orderId: OrderId): List<License>
  suspend fun save(license: License): License
}
