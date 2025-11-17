package org.migor.feedless.product

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.product.ProductEntity

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ProductMapper {

  fun toDomain(entity: ProductEntity): Product

  fun toEntity(domain: Product): ProductEntity

  companion object {
    val INSTANCE: ProductMapper = Mappers.getMapper(ProductMapper::class.java)
  }
}

