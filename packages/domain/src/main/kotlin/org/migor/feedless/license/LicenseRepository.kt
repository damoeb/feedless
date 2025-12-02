package org.migor.feedless.license

import org.migor.feedless.order.OrderId

interface LicenseRepository {
  fun findAllByOrderId(orderId: OrderId): List<License>
  fun save(license: License): License
}
