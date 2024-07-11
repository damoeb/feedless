package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.License
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.UpsertOrderInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.license.LicenseDAO
import org.migor.feedless.license.LicenseEntity
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.toDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api} & ${AppProfiles.saas}")
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
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun orders(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: OrdersInput
  ): List<Order> =
    coroutineScope {
      log.info("[$corrId] orders $data")
      orderService.findAll(corrId, data).map { it.toDTO() }
    }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun upsertOrder(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpsertOrderInput,
  ): Order =
    coroutineScope {
      log.info("[$corrId] upsertOrder $data")
      orderService.upsert(corrId, data.where, data.create, data.update).toDTO()
    }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
    val order: Order = dfe.getSource()
    productDAO.findById(UUID.fromString(order.productId)).orElseThrow().toDTO()
  }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun user(dfe: DgsDataFetchingEnvironment): User = coroutineScope {
    val order: Order = dfe.getSource()
    userDAO.findById(UUID.fromString(order.userId)).orElseThrow().toDTO()
  }

  @DgsData(parentType = DgsConstants.ORDER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun licenses(dfe: DgsDataFetchingEnvironment): List<License> = coroutineScope {
    val order: Order = dfe.getSource()
    licenseDAO.findAllByOrderId(UUID.fromString(order.id)).map { it.toDTO() }
  }

}

private fun LicenseEntity.toDTO(): License {
  // todo decode license and fill
  return License(name = "", email = "", scope = ProductCategory.feedless, createdAt = Date().time, version = 0)
}
