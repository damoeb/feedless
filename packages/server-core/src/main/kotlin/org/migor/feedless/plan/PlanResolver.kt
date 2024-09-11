package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Plan
import org.migor.feedless.generated.types.ProductCategory
import org.migor.feedless.generated.types.User
import org.migor.feedless.util.toMillis
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@DgsComponent
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PlanResolver {

  private val log = LoggerFactory.getLogger(PlanResolver::class.simpleName)

  @Autowired
  private lateinit var planDAO: PlanDAO


  @DgsData(parentType = DgsConstants.USER.TYPE_NAME, field = DgsConstants.USER.Plan)
  @Transactional
  suspend fun plan(dfe: DgsDataFetchingEnvironment, @InputArgument product: ProductCategory): Plan? = coroutineScope {
      val user: User = dfe.getSource()!!
      withContext(Dispatchers.IO) {
          planDAO.findActiveByUserAndProductIn(UUID.fromString(user.id), listOf(product.fromDto()), LocalDateTime.now())
              ?.toDto()
      }
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

