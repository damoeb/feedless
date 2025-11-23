package org.migor.feedless.license

import org.migor.feedless.order.OrderId
import java.time.LocalDateTime

data class License(
    val id: LicenseId = LicenseId(),
    val payload: String,
    val orderId: OrderId?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

