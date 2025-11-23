package org.migor.feedless.invoice

import org.migor.feedless.order.OrderId
import java.time.LocalDateTime
import java.util.*

data class Invoice(
    val id: InvoiceId = InvoiceId(UUID.randomUUID()),
    val price: Double,
    val isCanceled: Boolean,
    val dueTo: LocalDateTime?,
    val paidAt: LocalDateTime?,
    val orderId: OrderId? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

