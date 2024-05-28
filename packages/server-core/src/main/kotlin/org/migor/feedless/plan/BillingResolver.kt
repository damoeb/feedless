package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserDAO
import org.migor.feedless.api.ApiParams
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Billing
import org.migor.feedless.generated.types.BillingCreateInput
import org.migor.feedless.generated.types.BillingUpdateInput
import org.migor.feedless.generated.types.BillingWhereUniqueInput
import org.migor.feedless.generated.types.BillingsInput
import org.migor.feedless.generated.types.Product
import org.migor.feedless.generated.types.User
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
class BillingResolver {

  private val log = LoggerFactory.getLogger(BillingResolver::class.simpleName)

  @Autowired
  lateinit var billingService: BillingService

  @Autowired
  lateinit var productDAO: ProductDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun billings(
      @RequestHeader(ApiParams.corrId) corrId: String,
      @InputArgument data: BillingsInput
  ): List<Billing> =
      coroutineScope {
          log.info("[$corrId] billings $data")
          billingService.findAll(corrId, data).map { it.toDTO() }
      }

  @DgsMutation
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun upsertBilling(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument where: BillingWhereUniqueInput,
    @InputArgument create: BillingCreateInput,
    @InputArgument update: BillingUpdateInput,
  ): Billing =
      coroutineScope {
          log.info("[$corrId] upsertBilling $where $create $update")
          billingService.upsert(corrId, where, create, update).toDTO()
      }

  @DgsData(parentType = DgsConstants.BILLING.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun product(dfe: DgsDataFetchingEnvironment): Product = coroutineScope {
      val billing: Billing = dfe.getSource()
      productDAO.findById(UUID.fromString(billing.productId)).orElseThrow().toDTO()
  }

  @DgsData(parentType = DgsConstants.BILLING.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun user(dfe: DgsDataFetchingEnvironment): User = coroutineScope {
      val billing: Billing = dfe.getSource()
    userDAO.findById(UUID.fromString(billing.userId)).orElseThrow().toDTO()
  }

}
