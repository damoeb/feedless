package org.migor.feedless.data.jpa.order

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.order.Order

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface OrderMapper {

  fun toDomain(entity: OrderEntity): Order
  fun toEntity(domain: Order): OrderEntity

  companion object {
    val INSTANCE: OrderMapper = Mappers.getMapper(OrderMapper::class.java)
  }
}


