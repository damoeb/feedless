package org.migor.feedless.order

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.dataloader.DataLoader
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PageableRequest
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.toDTO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.OrderCreateInput
import org.migor.feedless.generated.types.OrderUpdateInput
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.ProductTargetGroup
import org.migor.feedless.generated.types.UpsertOrderInput
import org.migor.feedless.generated.types.UserCreateInput
import org.migor.feedless.generated.types.UserCreateOrConnectInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.license.LicenseRepository
import org.migor.feedless.payment.PaymentMethod
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.session.createRequestContext
import org.migor.feedless.user.UserCreate
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import org.migor.feedless.generated.types.Order as OrderDto
import org.migor.feedless.generated.types.Product as ProductDto

@DgsComponent
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class OrderResolver(
  private var orderUseCase: OrderUseCase,
  private val productUseCase: ProductUseCase,
  private val userUseCase: UserUseCase,
  private val licenseRepository: LicenseRepository
) {

  private val log = LoggerFactory.getLogger(OrderResolver::class.simpleName)

  @DgsQuery(field = DgsConstants.QUERY.Orders)
  suspend fun orders(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.ORDERS_INPUT_ARGUMENT.Data) data: OrdersInput
  ): List<OrderDto> = withContext(context = createRequestContext()) {
    log.debug("orders $data")
    orderUseCase.findAll(data.cursor.toPageableRequest()).map { it.toDto(productUseCase) }
  }

  @DgsMutation(field = DgsConstants.MUTATION.UpsertOrder)
  suspend fun upsertOrder(
    @InputArgument(DgsConstants.MUTATION.UPSERTORDER_INPUT_ARGUMENT.Data) data: UpsertOrderInput,
  ): OrderDto =
    withContext(context = createRequestContext()) {
      log.debug("upsertOrder $data")
      orderUseCase.upsert(data.where?.id?.let { OrderId(it) }, data.create?.toDomain(), data.update?.toDomain())
        .toDto(productUseCase)
    }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Product)
  suspend fun product(dfe: DgsDataFetchingEnvironment): ProductDto = coroutineScope {
    val order: OrderDto = dfe.getRoot()
    val dataLoader: DataLoader<ProductId, ProductDto> = dfe.getDataLoader<ProductId, ProductDto>("product")!!
    dataLoader.load(ProductId(order.productId)).await()
  }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Licenses)
  suspend fun licenses(dfe: DgsDataFetchingEnvironment): List<License> = coroutineScope {
    val order: OrderDto = dfe.getRoot()
    licenseRepository.findAllByOrderId(OrderId(order.id)).map { toDto() }
  }

  private fun Cursor.toPageableRequest(): PageableRequest {
    return PageableRequest(pageNumber = page, pageSize = pageSize ?: 10)
  }

  private fun OrderCreateInput.toDomain(): OrderCreate {

    return OrderCreate(
      overwritePrice = overwritePrice,
      isOffer = isOffer,
      productId = productId,
      paymentMethod = PaymentMethod.CreditCard,
      targetGroup = when (targetGroup) {
        ProductTargetGroup.eneterprise -> TypeOfCustomer.eneterprise
        ProductTargetGroup.individual -> TypeOfCustomer.individual
        ProductTargetGroup.other -> TypeOfCustomer.other
      },
      invoiceRecipientName = invoiceRecipientName,
      invoiceRecipientEmail = invoiceRecipientEmail,
      userId = this.user.resolveUserId(),
    )
  }

  private fun UserCreateOrConnectInput.resolveUserId(): UserId {
    return if (connect != null) {
      UserId(connect!!.id)
    } else {
      if (create != null) {
        runBlocking { userUseCase.createUser(create!!.toDomain()).id }
      } else {
        throw IllegalArgumentException("neither connect not create for user provided")
      }
    }
  }

  private fun UserCreateInput.toDomain(): UserCreate {
    return UserCreate(
      email = email,
      firstName = firstName,
      lastName = lastName,
      country = country,
      hasAcceptedTerms = hasAcceptedTerms,
    )
  }

  private fun OrderUpdateInput.toDomain(): OrderUpdate {
    return OrderUpdate(
      price = price?.set,
      isOffer = isOffer?.set,
      isRejected = isRejected?.set,
    )
  }
}

private fun toDto(): License {
  // todo decode license and fill
  return License(
    name = "",
    email = "",
    scope = Vertical.feedless,
    createdAt = LocalDateTime.now().toMillis(),
    version = 0
  )
}

internal suspend fun org.migor.feedless.order.Order.toDto(productUseCase: ProductUseCase): OrderDto {
  return OrderDto(
    id = id.uuid.toString(),
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
    product = productUseCase.findById(productId!!)!!.toDto()
  )
}
