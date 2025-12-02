package org.migor.feedless.order

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.UserCreateInput
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

@Service
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class OrderUseCaseImpl(
  private val orderRepository: OrderRepository,
  private val sessionService: SessionService,
  private val productUseCase: ProductUseCase,
  private val userRepository: UserRepository
) : OrderUseCase {

  private val log = LoggerFactory.getLogger(OrderUseCaseImpl::class.simpleName)

  override suspend fun findAll(cursor: PageableRequest): List<Order> {
    val currentUser = sessionService.user()
    return withContext(Dispatchers.IO) {
      if (currentUser.admin) {
//      data.where?.id?.let {
//        orderDAO.findById(UUID.fromString(data.where?.id))
//      } ?:
        orderRepository.findAll(cursor).toList()
      } else {
        orderRepository.findAllByUserId(currentUser.id, cursor).toList()
      }
    }
  }

  override suspend fun upsert(
    orderId: OrderId?,
    create: OrderCreate?,
    update: OrderUpdate?
  ): Order {
    return orderId?.let {
      update(orderId, update!!)
    } ?: create(create!!)
  }

  private suspend fun create(create: OrderCreate): Order {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] create $create]")
    // todo validate capabilities

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

  private suspend fun createUser(create: UserCreateInput): User {
    val corrId = coroutineContext.corrId()
    return withContext(Dispatchers.IO) {
      userRepository.findByEmail(create.email.trim()) ?: run {
        log.info("[$corrId] createUser $create]")
        if (BooleanUtils.isFalse(create.hasAcceptedTerms)) {
          throw IllegalArgumentException("You have to accept the terms")
        }
        userRepository.save(
          User(
            email = create.email,
            firstName = create.firstName,
            lastName = create.lastName,
            hasAcceptedTerms = create.hasAcceptedTerms,
            acceptedTermsAt = LocalDateTime.now(),
            lastLogin = LocalDateTime.now(),
          )
        )
      }
    }
  }

  private suspend fun update(orderId: OrderId, update: OrderUpdate): Order {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] update $update $orderId")
    if (!sessionService.user().admin) {
      throw PermissionDeniedException("must be root ($corrId)")
    }

    return withContext(Dispatchers.IO) {
      val order = orderRepository.findById(orderId)!!

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

private fun PaymentMethodDto.fromDTO(): PaymentMethod {
  return when (this) {
    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
  }
}
