package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.mapper.toDto
import org.migor.feedless.capability.CapabilityService
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Cursor
import org.migor.feedless.product.ProductService
import org.migor.feedless.user.UserId
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.migor.feedless.generated.types.Plan as PlanDto

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  private lateinit var planService: PlanService

  @Autowired
  private lateinit var productService: ProductService

  @Autowired
  private lateinit var capabilityService: CapabilityService

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @DgsQuery(field = DgsConstants.QUERY.Plans)
  suspend fun plan(
    dfe: DgsDataFetchingEnvironment,
    @InputArgument(DgsConstants.QUERY.PLANS_INPUT_ARGUMENT.Cursor) cursor: Cursor
  ): List<PlanDto> = coroutineScope {
    planService.findAllByUser(userId()).map { it.toDto(productService) }
  }

  private fun userId(): UserId {
    return capabilityService.getCapability(UserCapability.ID)?.let { UserCapability.resolve(it) }!!
  }
}

internal suspend fun Plan.toDto(productService: ProductService): PlanDto {
  return PlanDto(
    id = id.toString(),
    productId = productId.toString(),
    startedAt = startedAt?.toMillis(),
    terminatedAt = terminatedAt?.toMillis(),
    product = productService.findById(productId)!!.toDto(),
    recurringYearly = false
  )
}

