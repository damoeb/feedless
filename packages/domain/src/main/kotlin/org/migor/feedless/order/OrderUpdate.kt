package org.migor.feedless.order

data class OrderUpdate(
  val price: Double? = null,
  val isOffer: Boolean? = null,
  val isRejected: Boolean? = null
)
