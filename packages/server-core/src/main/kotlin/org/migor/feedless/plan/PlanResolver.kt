package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.toDTO
import org.migor.feedless.data.jpa.plan.PlanEntity
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.user.userId
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  private lateinit var planService: PlanService

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @DgsQuery(field = DgsConstants.QUERY.Plans)
  suspend fun plan(
    dfe: DgsDataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.PLANS_INPUT_ARGUMENT.Cursor) cursor: Cursor
  ): List<Plan> = withContext(
    injectCurrentUser(currentCoroutineContext(), dfe)
  ) {
    planService.findAllByUser(coroutineContext.userId()).map { it.toDto() }
  }
}

private fun PlanEntity.toDto(): Plan {
  return Plan(
    id = id.toString(),
    productId = productId.toString(),
    startedAt = startedAt?.toMillis(),
    terminatedAt = terminatedAt?.toMillis(),
    product = product!!.toDTO(),
    recurringYearly = false
  )
}

