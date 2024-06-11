package org.migor.feedless.plan

import org.apache.commons.lang3.BooleanUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.Billing
import org.migor.feedless.generated.types.OrderCreateInput
import org.migor.feedless.generated.types.OrderUpdateInput
import org.migor.feedless.generated.types.OrderWhereUniqueInput
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.ProductTargetGroup
import org.migor.feedless.generated.types.UserCreateInput
import org.migor.feedless.license.LicenseService
import org.migor.feedless.generated.types.PaymentMethod as PaymentMethodDto
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.saas)
class OrderService {

  private val log = LoggerFactory.getLogger(OrderService::class.simpleName)

  @Autowired
  private lateinit var orderDAO: OrderDAO

  @Autowired
  private lateinit var sessionService: SessionService

  @Autowired
  private lateinit var licenseService: LicenseService

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var userDAO: UserDAO

  fun findAll(corrId: String, data: OrdersInput): List<OrderEntity> {
    val pageable = PageRequest.of(data.cursor?.page ?: 0, data.cursor?.pageSize ?: 10, Sort.Direction.DESC, "createdAt")
    val currentUser = sessionService.user(corrId)
    return if (currentUser.root) {
//      data.where?.id?.let {
//        billingDAO.findById(UUID.fromString(data.where?.id))
//      } ?:
      orderDAO.findAll(pageable).toList()
    } else {
      orderDAO.findAllByUserId(currentUser.id, pageable).toList()
    }
  }

  fun upsert(corrId: String, where: OrderWhereUniqueInput?, create: OrderCreateInput?, update: OrderUpdateInput?): OrderEntity {
    return where?.let {
      update(corrId, where, update!!)
    } ?: create(corrId, create!!)
  }

  private fun create(corrId: String, create: OrderCreateInput): OrderEntity {
    log.info("[$corrId] create $create]")
    val billing = OrderEntity()
    billing.isOffer = BooleanUtils.isTrue(create.isOffer)
    val productId = UUID.fromString(create.productId)
    billing.productId = productId
    billing.invoiceRecipientEmail = create.invoiceRecipientEmail.trim()
    billing.invoiceRecipientName = create.invoiceRecipientName.trim()
    billing.paymentMethod = create.paymentMethod?.fromDTO()
    billing.callbackUrl = create.callbackUrl
    billing.targetGroupIndividual = create.targetGroup === ProductTargetGroup.individual
    billing.targetGroupEnterprise = create.targetGroup === ProductTargetGroup.eneterprise
    billing.targetGroupOther = create.targetGroup === ProductTargetGroup.other

    billing.price = if(create.overwritePrice <= 0) {
      productService.resolvePriceForProduct(productId, create.user.where?.id?.let { UUID.fromString(it) })
    } else {
      create.overwritePrice
    }
    create.user.where?.let {
      billing.userId = UUID.fromString(it.id)
    } ?: run {
      billing.userId = createUser(corrId, create.user.create).id
    }

    return orderDAO.save(billing)
  }

  private fun createUser(corrId: String, create: UserCreateInput): UserEntity {
    return userDAO.findByEmail(create.email.trim())?: run {
      log.info("[$corrId] createUser $create]")
      if (BooleanUtils.isFalse(create.hasAcceptedTerms)) {
        throw IllegalArgumentException("You have to accept the terms")
      }
      val user = UserEntity()
      user.email = create.email
      user.firstName = create.firstName
      user.lastName = create.lastName
      user.hasAcceptedTerms = create.hasAcceptedTerms
      user.acceptedTermsAt = Date()

      userDAO.save(user)
    }
  }

  private fun update(corrId: String, where: OrderWhereUniqueInput, update: OrderUpdateInput): OrderEntity {
    log.info("[$corrId] update $update $where")
    if (!sessionService.user(corrId).root) {
      throw PermissionDeniedException("must be root ($corrId)")
    }
    val billing = orderDAO.findById(UUID.fromString(where.id)).orElseThrow()

    update.isRejected?.let {
      billing.isRejected = it.set
    }
    update.price?.let {
      billing.price = it.set
    }
    update.isRejected?.let {
      billing.isRejected = it.set
    }

    return orderDAO.save(billing)
  }

  @Transactional(propagation = Propagation.REQUIRED)
  fun handlePaymentCallback(corrId: String, billingId: String): OrderEntity {
    val billing = orderDAO.findById(UUID.fromString(billingId)).orElseThrow()

    billing.isPaid = true
    billing.paidAt = Date()

    val product = billing.product!!
    if (product.isCloudProduct) {
      productService.enableCloudProduct(corrId, product, billing.user)
    } else {
      billing.licenses = mutableListOf(licenseService.createLicenseForProduct(corrId, product, billing))
    }
    orderDAO.save(billing)
    // todo send email

    return billing
  }
}

private fun PaymentMethodDto.fromDTO(): PaymentMethod {
  return when(this) {
    PaymentMethodDto.Bill -> PaymentMethod.Bill
    PaymentMethodDto.CreditCard -> PaymentMethod.CreditCard
    PaymentMethodDto.Bitcoin -> PaymentMethod.Bitcoin
    PaymentMethodDto.Ethereum -> PaymentMethod.Ethereum
    PaymentMethodDto.PayPal -> PaymentMethod.PayPal
  }
}

fun OrderEntity.toDTO(): Billing {
  return Billing.newBuilder()
    .id(id.toString())
    .createdAt(createdAt.time)
    .userId(userId.toString())
    .isOffer(isOffer)
    .isPaid(isPaid)
    .paidAt(paidAt?.time)
    .paymentMethod(paymentMethod?.toDTO())
    .invoiceRecipientName(invoiceRecipientName)
    .invoiceRecipientEmail(invoiceRecipientEmail)
    .isRejected(isRejected)
    .productId(productId.toString())
    .validTo(dueTo?.time)
    .build()
}
