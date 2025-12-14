package org.migor.feedless.order

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.product.ProductId
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.corrId
import org.migor.feedless.user.isAdmin
import org.migor.feedless.user.userId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class OrderUseCaseImpl(
  private val orderRepository: OrderRepository,
//  private val userRepository: UserRepository,
  private val orderGuard: OrderGuard,
  private val userGuard: UserGuard,
) : OrderUseCase {

  private val log = LoggerFactory.getLogger(OrderUseCaseImpl::class.simpleName)

  override suspend fun findAll(cursor: PageableRequest): List<Order> {

    userGuard.requireRead(currentCoroutineContext().userId())

    return withContext(Dispatchers.IO) {
      if (coroutineContext.isAdmin()) {
//      data.where?.id?.let {
//        orderDAO.findById(UUID.fromString(data.where?.id))
//      } ?:
        orderRepository.findAll(cursor).toList()
      } else {
        orderRepository.findAllByUserId(coroutineContext.userId(), cursor).toList()
      }
    }
  }

  override suspend fun upsert(
    orderId: OrderId?,
    create: OrderCreate?,
    update: OrderUpdate?
  ): Order {
    userGuard.requireRead(currentCoroutineContext().userId())

    return orderId?.let {
      update(orderId, update!!)
    } ?: create(create!!)
  }

  private suspend fun create(create: OrderCreate): Order {
    val corrId = currentCoroutineContext().corrId()
    log.info("[$corrId] create $create]")

    userGuard.requireRead(currentCoroutineContext().userId())

    val productId = ProductId(create.productId)

//    val userId = create.user.connect?.let {
//      UserId(it.id)
//    } ?: create.user.create?.let { userCreateInput ->
//      createUser(userCreateInput).id
//    } ?: throw IllegalArgumentException("Neither connect or create is present")

    val order = Order(
      userId = create.userId,
      isOffer = BooleanUtils.isTrue(create.isOffer),
      productId = productId,
      invoiceRecipientEmail = create.invoiceRecipientEmail.trim(),
      invoiceRecipientName = create.invoiceRecipientName.trim(),
      paymentMethod = create.paymentMethod,
      callbackUrl = "", // create.callbackUrl
      targetGroupIndividual = create.targetGroup === TypeOfCustomer.individual,
      targetGroupEnterprise = create.targetGroup === TypeOfCustomer.eneterprise,
      targetGroupOther = create.targetGroup === TypeOfCustomer.other,
      price = if (create.overwritePrice <= 0) {
        throw IllegalArgumentException("Cannot overwrite order with non-overwritePrice")
      } else {
        create.overwritePrice
      }
    )

    return withContext(Dispatchers.IO) {
      orderRepository.save(order)
    }
  }

  private suspend fun update(orderId: OrderId, update: OrderUpdate): Order {
    val corrId = currentCoroutineContext().corrId()
    log.info("[$corrId] update $update $orderId")

    val order = orderGuard.requireWrite(orderId)

    return withContext(Dispatchers.IO) {

      orderRepository.save(
        order.copy(
          isOfferRejected = update.isRejected ?: order.isOfferRejected,
          price = update.price ?: order.price,
          isOffer = update.isOffer ?: order.isOffer,
        )
      )
    }
  }

  override fun findById(orderID: OrderId): Order {
    return orderRepository.findById(orderID)!!
  }
}

//private fun PaymentMethodDto.fromDTO(): PaymentMethod {
//  return when (this) {
//    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
//  }
//}
