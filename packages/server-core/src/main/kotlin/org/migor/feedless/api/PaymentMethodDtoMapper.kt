package org.migor.feedless.api

import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

fun PaymentMethod.toDTO(): PaymentMethodDto {
  return when (this) {
    PaymentMethod.CreditCard -> PaymentMethodDto.CreditCard
  }
}
