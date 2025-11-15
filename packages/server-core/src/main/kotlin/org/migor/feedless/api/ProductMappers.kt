package org.migor.feedless.api

import org.migor.feedless.data.jpa.pricedProduct.PricedProductEntity
import org.migor.feedless.data.jpa.product.ProductEntity
import org.migor.feedless.generated.types.PricedProduct
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.RecurringPaymentInterval
import java.time.temporal.ChronoUnit


fun PricedProductEntity.toDto(): PricedProduct {
  return PricedProduct(
    id = id.toString(),
    description = this.unit,
    price = price,
    recurringInterval = recurringInterval.toDto(),
    inStock = inStock ?: -1,
  )
}

private fun ChronoUnit.toDto(): RecurringPaymentInterval {
  return when (this) {
    ChronoUnit.YEARS -> RecurringPaymentInterval.yearly
    ChronoUnit.MONTHS -> RecurringPaymentInterval.monthly
    else -> throw IllegalArgumentException("Invalid RecurringPaymentInterval: $this")
  }
}


fun ProductEntity.toDTO(): Product {
  return Product(
    id = id.toString(),
    name = name,
    description = description,
    isCloud = saas,
    individual = selfHostingIndividual,
    enterprise = selfHostingEnterprise,
    other = selfHostingOther,
    partOf = partOf?.toDto(),
    featureGroupId = featureGroupId.toString(),
    prices = emptyList(),
  )
}
