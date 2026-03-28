package org.migor.feedless.payment

import java.util.*

/**
 * Implemented in server-core: completes auction-alert signup after Stripe Checkout (subscription + trial).
 */
fun interface PendingCheckoutFinalizer {
  suspend fun finalizePendingCheckout(pendingId: UUID)
}
