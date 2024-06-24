package org.migor.feedless.subscription

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.api}")
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  private lateinit var planDAO: PlanDAO


  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plan(dfe: DgsDataFetchingEnvironment, @InputArgument product: ProductCategory): Plan? = coroutineScope {
    val user: User = dfe.getSource()
    planDAO.findActiveByUserAndProduct(UUID.fromString(user.id), product.fromDto())?.toDto()
  }
}

private fun PlanEntity.toDto(): Plan {
  return Plan.newBuilder()
    .productId(productId.toString())
    .startedAt(startedAt?.time)
    .terminatedAt(terminatedAt?.time)
    .build()
}

