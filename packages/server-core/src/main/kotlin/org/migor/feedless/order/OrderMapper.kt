package org.migor.feedless.order

import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.mapstruct.factory.Mappers
import org.migor.feedless.data.jpa.IdMappers
import org.migor.feedless.data.jpa.order.OrderEntity
import org.migor.feedless.user.UserId
import java.util.*

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = [IdMappers::class])
interface OrderMapper {

    fun toDomain(entity: OrderEntity): Order

    fun toEntity(domain: Order): OrderEntity

    fun mapBillingId(entity: OrderEntity): String {
        // Use callback URL or id as billing ID if not available
        return entity.callbackUrl.ifEmpty { entity.id.toString() }
    }

    @org.mapstruct.Named("uuidToOrderId")
    fun uuidToOrderId(uuid: UUID): OrderId = OrderId(uuid)

    @org.mapstruct.Named("orderIdToUuid")
    fun orderIdToUuid(id: OrderId): UUID = id.uuid

    @org.mapstruct.Named("uuidToUserId")
    fun uuidToUserId(uuid: UUID): UserId = UserId(uuid)

    @org.mapstruct.Named("userIdToUuid")
    fun userIdToUuid(id: UserId): UUID = id.uuid

    companion object {
        val INSTANCE: OrderMapper = Mappers.getMapper(OrderMapper::class.java)
    }
}

