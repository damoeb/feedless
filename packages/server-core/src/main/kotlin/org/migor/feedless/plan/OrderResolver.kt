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
import org.migor.feedless.generated.types.Order
import org.migor.feedless.generated.types.OrderCreateInput
import org.migor.feedless.generated.types.OrderUpdateInput
import org.migor.feedless.generated.types.OrderWhereUniqueInput
import org.migor.feedless.generated.types.OrdersInput
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.User
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
    @InputArgument where: OrderWhereUniqueInput,
    @InputArgument create: OrderCreateInput,
    @InputArgument update: OrderUpdateInput,
  ): Order =
    coroutineScope {
      log.info("[$corrId] upsertOrder $where $create $update")
      orderService.upsert(corrId, where, create, update).toDTO()
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

}
