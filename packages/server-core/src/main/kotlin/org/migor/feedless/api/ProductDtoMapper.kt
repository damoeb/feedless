package org.migor.feedless.api

import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.Product
import java.time.temporal.ChronoUnit
import org.migor.feedless.generated.types.PricedProduct as PricedProductDto
import org.migor.feedless.generated.types.Product as ProductDto
import org.migor.feedless.generated.types.RecurringPaymentInterval as RecurringPaymentIntervalDto


fun PricedProduct.toDto(): PricedProductDto {
  return PricedProductDto(
    id = id.toString(),
    description = "",
    price = price,
    recurringInterval = recurringInterval.toDto(),
    inStock = inStock ?: -1,
  )
}

private fun ChronoUnit.toDto(): RecurringPaymentIntervalDto {
  return when (this) {
    ChronoUnit.YEARS -> RecurringPaymentIntervalDto.yearly
    ChronoUnit.MONTHS -> RecurringPaymentIntervalDto.monthly
    else -> throw IllegalArgumentException("Invalid RecurringPaymentInterval: $this")
  }
}


fun Product.toDTO(): ProductDto {
  return ProductDto(
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
