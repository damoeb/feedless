package org.migor.feedless.plan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.toDTO
import org.migor.feedless.data.jpa.order.OrderDAO
import org.migor.feedless.data.jpa.order.OrderEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.UserEntity
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.OrderCreateInput
import org.migor.feedless.generated.types.OrderUpdateInput
import org.migor.feedless.generated.types.OrderWhereUniqueInput
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.ProductTargetGroup
import org.migor.feedless.generated.types.UserCreateInput
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserId
import org.migor.feedless.user.corrId
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.service}")
class OrderService {

  private val log = LoggerFactory.getLogger(OrderService::class.simpleName)

  @Autowired
  private lateinit var orderDAO: OrderDAO

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Transactional(readOnly = true)
  suspend fun findAll(data: OrdersInput): List<OrderEntity> {
    val pageable = PageRequest.of(data.cursor.page, data.cursor.pageSize ?: 10, Sort.Direction.DESC, "createdAt")
    val currentUser = sessionService.user()
    return withContext(Dispatchers.IO) {
      if (currentUser.admin) {
//      data.where?.id?.let {
//        orderDAO.findById(UUID.fromString(data.where?.id))
//      } ?:
        orderDAO.findAll(pageable).toList()
      } else {
        orderDAO.findAllByUserId(currentUser.id, pageable).toList()
      }
    }
  }

  @Transactional
  suspend fun upsert(
    where: OrderWhereUniqueInput?,
    create: OrderCreateInput?,
    update: OrderUpdateInput?
  ): OrderEntity {
    return where?.let {
      update(where, update!!)
    } ?: create(create!!)
  }

  private suspend fun create(create: OrderCreateInput): OrderEntity {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] create $create]")
    val order = OrderEntity()
    order.isOffer = BooleanUtils.isTrue(create.isOffer)
    val productId = ProductId(UUID.fromString(create.productId))
    order.productId = productId.value
    order.invoiceRecipientEmail = create.invoiceRecipientEmail.trim()
    order.invoiceRecipientName = create.invoiceRecipientName.trim()
    order.paymentMethod = create.paymentMethod.fromDTO()
    order.callbackUrl = create.callbackUrl
    order.targetGroupIndividual = create.targetGroup === ProductTargetGroup.individual
    order.targetGroupEnterprise = create.targetGroup === ProductTargetGroup.eneterprise
    order.targetGroupOther = create.targetGroup === ProductTargetGroup.other

    order.price = if (create.overwritePrice <= 0) {
      productService.resolvePriceForProduct(productId, create.user.connect?.id?.let { UserId(UUID.fromString(it)) })
    } else {
      create.overwritePrice
    }
    create.user.connect?.let {
      order.userId = UUID.fromString(it.id)
    } ?: create.user.create?.let { userCreateInput ->
      order.userId = createUser(userCreateInput).id
    } ?: throw IllegalArgumentException("Neither connect or create is present")
    return withContext(Dispatchers.IO) {
      orderDAO.save(order)
    }
  }

  private suspend fun createUser(create: UserCreateInput): UserEntity {
    val corrId = coroutineContext.corrId()
    return withContext(Dispatchers.IO) {
      userDAO.findByEmail(create.email.trim()) ?: run {
        log.info("[$corrId] createUser $create]")
        if (BooleanUtils.isFalse(create.hasAcceptedTerms)) {
          throw IllegalArgumentException("You have to accept the terms")
        }
        val user = UserEntity()
        user.email = create.email
        user.firstName = create.firstName
        user.lastName = create.lastName
        user.hasAcceptedTerms = create.hasAcceptedTerms
        user.acceptedTermsAt = LocalDateTime.now()

        userDAO.save(user)
      }
    }
  }

  private suspend fun update(where: OrderWhereUniqueInput, update: OrderUpdateInput): OrderEntity {
    val corrId = coroutineContext.corrId()
    log.info("[$corrId] update $update $where")
    if (!sessionService.user().admin) {
      throw PermissionDeniedException("must be root ($corrId)")
    }

    return withContext(Dispatchers.IO) {
      val order = orderDAO.findById(UUID.fromString(where.id)).orElseThrow()

      update.isRejected?.let {
        order.isOfferRejected = it.set
      }
      update.price?.let {
        order.price = it.set
      }
      update.isRejected?.let {
        order.isOfferRejected = it.set
      }

      orderDAO.save(order)
    }
  }
}

private fun PaymentMethodDto.fromDTO(): PaymentMethod {
  return when (this) {
    PaymentMethodDto.Bill -> PaymentMethod.Bill
    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
    PaymentMethodDto.Bitcoin -> PaymentMethod.Bitcoin
    PaymentMethodDto.Ethereum -> PaymentMethod.Ethereum
    PaymentMethodDto.PayPal -> PaymentMethod.PayPal
  }
}

fun OrderEntity.toDTO(): Order {
  return Order(
    id = id.toString(),
    createdAt = createdAt.toMillis(),
    userId = userId.toString(),
    productId = productId.toString(),

    isOffer = isOffer,
    paymentDueTo = dueTo?.toMillis(),
    isPaid = isPaid,
    isOfferRejected = isOfferRejected,

    paidAt = paidAt?.toMillis(),
    paymentMethod = paymentMethod?.toDTO(),
    invoiceRecipientName = invoiceRecipientName,
    invoiceRecipientEmail = invoiceRecipientEmail,
    price = price,
    product = product!!.toDTO(),

    )
}
