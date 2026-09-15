package org.migor.feedless.user

import org.migor.feedless.product.ProductId

/** null = leave unchanged. */
data class UserUpdate(
  val email: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val country: String? = null,
  val plan: ProductId? = null,
  val acceptedTermsAndServices: Boolean? = null,
  /** true = schedule purge in 30 days, false = unset. */
  val schedulePurge: Boolean? = null,
)
