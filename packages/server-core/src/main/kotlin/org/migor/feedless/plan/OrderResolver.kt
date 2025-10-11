package org.migor.feedless.plan

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
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.UpsertOrderInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.license.LicenseEntity
import org.migor.feedless.license.LicenseService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class OrderResolver {

  private val log = LoggerFactory.getLogger(OrderResolver::class.simpleName)

  @Autowired
  lateinit var orderService: OrderService

  @Autowired
  lateinit var licenseService: LicenseService

  @DgsQuery(field = DgsConstants.QUERY.Orders)
  suspend fun orders(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.ORDERS_INPUT_ARGUMENT.Data) data: OrdersInput
  ): List<Order> =
    withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
      log.debug("orders $data")
      orderService.findAll(data).map { it.toDTO() }
    }

  @DgsMutation(field = DgsConstants.MUTATION.UpsertOrder)
  suspend fun upsertOrder(
    @InputArgument(DgsConstants.MUTATION.UPSERTORDER_INPUT_ARGUMENT.Data) data: UpsertOrderInput,
  ): Order =
    coroutineScope {
      log.debug("upsertOrder $data")
      orderService.upsert(data.where, data.create, data.update).toDTO()
    }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Product)
  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
    val order: Order = dfe.getRoot()
    val dataLoader: DataLoader<String, Product> = dfe.getDataLoader<String, Product>("product")!!
    dataLoader.load(order.productId).await()
  }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Licenses)
  suspend fun licenses(dfe: DgsDataFetchingEnvironment): List<License> = coroutineScope {
    val order: Order = dfe.getRoot()
    licenseService.findAllByOrderId(OrderId(order.id)).map { it.toDTO() }
  }

}

private fun LicenseEntity.toDTO(): License {
  // todo decode license and fill
  return License(
    name = "",
    email = "",
    scope = Vertical.feedless,
    createdAt = LocalDateTime.now().toMillis(),
    version = 0
  )
}
