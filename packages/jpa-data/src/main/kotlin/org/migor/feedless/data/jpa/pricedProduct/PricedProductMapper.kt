package org.migor.feedless.data.jpa.pricedProduct

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.product.PricedProduct

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface PricedProductMapper {

    fun toDomain(entity: PricedProductEntity): PricedProduct
    fun toEntity(domain: PricedProduct): PricedProductEntity

    companion object {
        val INSTANCE: PricedProductMapper = Mappers.getMapper(PricedProductMapper::class.java)
    }
}
