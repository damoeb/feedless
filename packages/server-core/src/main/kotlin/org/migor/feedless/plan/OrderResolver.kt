package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.UpsertOrderInput
import org.migor.feedless.license.LicenseDAO
import org.migor.feedless.license.LicenseEntity
import org.migor.feedless.session.useRequestContext
import org.migor.feedless.user.UserDAO
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.time.LocalDateTime
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api} & ${AppProfiles.saas}")
@Transactional
class OrderResolver {

  private val log = LoggerFactory.getLogger(OrderResolver::class.simpleName)

  @Autowired
  lateinit var orderService: OrderService

  @Autowired
  lateinit var productDAO: ProductDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var licenseDAO: LicenseDAO

  @DgsQuery
  suspend fun orders(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: OrdersInput
  ): List<Order> =
    withContext(useRequestContext(currentCoroutineContext())) {
      log.debug("[$corrId] orders $data")
      orderService.findAll(corrId, data).map { it.toDTO() }
    }

  @DgsMutation(field = DgsConstants.MUTATION.UpsertOrder)
  suspend fun upsertOrder(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpsertOrderInput,
  ): Order =
    coroutineScope {
      log.debug("[$corrId] upsertOrder $data")
      orderService.upsert(corrId, data.where, data.create, data.update).toDTO()
    }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Product)
  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
    val order: Order = dfe.getRoot()
    productDAO.findById(UUID.fromString(order.productId)).orElseThrow().toDTO()
  }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME, field = DgsConstants.ORDER.Licenses)
  suspend fun licenses(dfe: DgsDataFetchingEnvironment): List<License> = coroutineScope {
    val order: Order = dfe.getRoot()
    withContext(Dispatchers.IO) {
      licenseDAO.findAllByOrderId(UUID.fromString(order.id)).map { it.toDTO() }
    }
  }

}

private fun LicenseEntity.toDTO(): License {
  // todo decode license and fill
  return License(name = "", email = "", scope = ProductCategory.feedless, createdAt = LocalDateTime.now().toMillis(), version = 0)
}
