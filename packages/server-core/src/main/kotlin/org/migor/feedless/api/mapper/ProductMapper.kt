package org.migor.feedless.api.mapper

import org.migor.feedless.api.toDto
import org.migor.feedless.product.PricedProduct
import org.migor.feedless.product.Product
import java.time.temporal.ChronoUnit
import org.migor.feedless.generated.types.PricedProduct as PricedProductDto
import org.migor.feedless.generated.types.Product as ProductDto
import org.migor.feedless.generated.types.RecurringPaymentInterval as RecurringPaymentIntervalDto


fun Product.toDto(): ProductDto = ProductDto(
    id = id.uuid.toString(),
    name = name,
    description = description,
    isCloud = saas,
    individual = selfHostingIndividual,
    enterprise = selfHostingEnterprise,
    other = selfHostingOther,
    partOf = partOf?.toDto(),
    featureGroupId = featureGroupId.uuid.toString(),
    prices = emptyList(),
)


fun PricedProduct.toDto(): PricedProductDto = PricedProductDto(
    id = id.uuid.toString(),
    description = "",
    inStock = inStock ?: 0,
    recurringInterval = recurringInterval.toDto(),
    price = price
)

private fun ChronoUnit.toDto(): RecurringPaymentIntervalDto {
    return when (this) {
        ChronoUnit.YEARS -> RecurringPaymentIntervalDto.yearly
        ChronoUnit.MONTHS -> RecurringPaymentIntervalDto.monthly
        else -> throw IllegalArgumentException("Invalid RecurringPaymentInterval: $this")
    }
}
