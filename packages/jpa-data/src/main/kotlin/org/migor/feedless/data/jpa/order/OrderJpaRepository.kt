package org.migor.feedless.data.jpa.order

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.data.jpa.repository.toPageRequest
import org.migor.feedless.order.Order
import org.migor.feedless.order.OrderId
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
@Profile("${AppProfiles.scrape} & ${AppLayer.repository}")
class OrderJpaRepository(private val orderDAO: OrderDAO) : OrderRepository {
  override fun findAllByUserId(
    userId: UserId,
    pageable: PageableRequest
  ): List<Order> {
    return orderDAO.findAllByUserId(userId.uuid, pageable.toPageRequest()).map { it.toDomain() }.toList()
  }

  override fun findAll(pageableRequest: PageableRequest): List<Order> {
    return orderDAO.findAll(pageableRequest.toPageRequest()).map { it.toDomain() }.toList()
  }

  override fun save(order: Order): Order {
    return orderDAO.save(order.toEntity()).toDomain()
  }

  override fun findById(orderId: OrderId): Order? {
    return orderDAO.findById(orderId.uuid).getOrNull()?.toDomain()
  }

}
