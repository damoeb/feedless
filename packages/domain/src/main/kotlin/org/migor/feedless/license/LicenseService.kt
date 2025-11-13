package org.migor.feedless.license

import org.migor.feedless.Vertical
import org.migor.feedless.payment.Order
import org.migor.feedless.payment.OrderId
import org.migor.feedless.product.Product

interface LicenseService {
  fun initialize()
  suspend fun findAllByOrderId(orderId: OrderId): List<License>
  fun isTrial(): Boolean
  fun isLicenseNotNeeded(): Boolean
  fun isLicensedForProduct(product: Vertical): Boolean
  fun getBuildDate(): Long
  fun updateLicense(payload: String)
  fun getLicensePayload(): LicensePayload?
  suspend fun createLicenseForProduct(product: Product, billing: Order): License;
  fun hasValidLicenseOrLicenseNotNeeded(): Boolean
  fun getTrialUntil(): Long
}
