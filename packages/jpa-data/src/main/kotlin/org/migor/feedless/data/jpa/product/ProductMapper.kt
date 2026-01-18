package org.migor.feedless.data.jpa.product

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.product.Product

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface ProductMapper {

  fun toDomain(entity: ProductEntity): Product

  @Mapping(target = "featureGroup", ignore = true)
  fun toEntity(domain: Product): ProductEntity

  companion object {
    val INSTANCE: ProductMapper = Mappers.getMapper(ProductMapper::class.java)
  }
}
