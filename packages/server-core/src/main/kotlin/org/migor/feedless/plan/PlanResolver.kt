package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.repository.toDto
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.ProductName
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@org.springframework.context.annotation.Profile(AppProfiles.database)
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  lateinit var planService: PlanService

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plans(@RequestHeader(ApiParams.corrId) corrId: String, @InputArgument product: ProductName): List<Plan> =
    coroutineScope {
      log.info("[$corrId] plans for $product")
      planService.findAllVisible(product.fromDto()).map { it.toDto() }
    }
}
