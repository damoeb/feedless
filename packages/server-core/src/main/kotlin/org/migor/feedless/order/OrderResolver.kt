package org.migor.feedless.order

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.dataloader.DataLoader
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.api.toDTO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.UpsertOrderInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.license.LicenseService
import org.migor.feedless.product.ProductId
import org.migor.feedless.product.ProductService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.migor.feedless.generated.types.Order as OrderDto
import org.migor.feedless.generated.types.Product as ProductDto

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class OrderResolver {

    private val log = LoggerFactory.getLogger(OrderResolver::class.simpleName)

    @Autowired
    lateinit var orderService: OrderServiceImpl

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var licenseService: LicenseService

    @DgsQuery(field = DgsConstants.QUERY.Orders)
    suspend fun orders(
        dfe: DataFetchingEnvironment,
        @InputArgument(DgsConstants.QUERY.ORDERS_INPUT_ARGUMENT.Data) data: OrdersInput
    ): List<OrderDto> =
        withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
            log.debug("orders $data")
            orderService.findAll(data).map { it.toDto(productService) }
        }

    @DgsMutation(field = DgsConstants.MUTATION.UpsertOrder)
    suspend fun upsertOrder(
        @InputArgument(DgsConstants.MUTATION.UPSERTORDER_INPUT_ARGUMENT.Data) data: UpsertOrderInput,
    ): OrderDto =
        coroutineScope {
            log.debug("upsertOrder $data")
            orderService.upsert(data.where, data.create, data.update).toDto(productService)
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
        licenseService.findAllByOrderId(OrderId(order.id)).map { it.toDto() }
    }

}

private fun org.migor.feedless.license.License.toDto(): License {
    // todo decode license and fill
    return License(
        name = "",
        email = "",
        scope = Vertical.feedless,
        createdAt = LocalDateTime.now().toMillis(),
        version = 0
    )
}

internal suspend fun org.migor.feedless.order.Order.toDto(productService: ProductService): OrderDto {
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
        product = productService.findById(productId!!)!!.toDto()
    )
}
