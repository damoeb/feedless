package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  private lateinit var planService: PlanService

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME, field = DgsConstants.USER.Plan)
  suspend fun plan(dfe: DgsDataFetchingEnvironment, @InputArgument product: Vertical): Plan? = coroutineScope {
    val user: User = dfe.getSource()!!
    planService.findActiveByUserAndProductIn(UUID.fromString(user.id), listOf(product.fromDto()))
      ?.toDto()
  }
}

private fun PlanEntity.toDto(): Plan {
  return Plan(
    productId = productId.toString(),
    startedAt = startedAt?.toMillis(),
    terminatedAt = terminatedAt?.toMillis(),
    product = product!!.toDTO()
  )
}

