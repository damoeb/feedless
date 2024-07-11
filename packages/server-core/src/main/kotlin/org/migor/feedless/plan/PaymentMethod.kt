package org.migor.feedless.plan

import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

enum class PaymentMethod {
  Bill,
  PayPal,
  Bitcoin,
  Ethereum,
  CreditCard
}

fun PaymentMethod.toDTO(): PaymentMethodDto {
  return when (this) {
    PaymentMethod.Bill -> PaymentMethodDto.Bill
    PaymentMethod.PayPal -> PaymentMethodDto.PayPal
    PaymentMethod.Bitcoin -> PaymentMethodDto.Bitcoin
    PaymentMethod.Ethereum -> PaymentMethodDto.Ethereum
    PaymentMethod.CreditCard -> PaymentMethodDto.CreditCard
  }
}
