package org.migor.feedless.api

import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

fun PaymentMethod.toDTO(): PaymentMethodDto {
  return when (this) {
    PaymentMethod.Bill -> PaymentMethodDto.Bill
    PaymentMethod.PayPal -> PaymentMethodDto.PayPal
    PaymentMethod.Bitcoin -> PaymentMethodDto.Bitcoin
    PaymentMethod.Ethereum -> PaymentMethodDto.Ethereum
    PaymentMethod.CreditCard -> PaymentMethodDto.CreditCard
  }
}
