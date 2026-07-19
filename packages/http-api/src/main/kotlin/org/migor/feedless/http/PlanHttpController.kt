package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.http.api.PlansApi
import org.migor.feedless.http.api.model.PlanListResponse
import org.migor.feedless.http.mapper.HttpPlanMapper
import org.migor.feedless.plan.PlanId
import org.migor.feedless.plan.PlanUseCasePort
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Plan as HttpPlan

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.plan} & ${AppLayer.api}")
class PlanHttpController(
  private val planUseCase: PlanUseCasePort,
  private val mapper: HttpPlanMapper,
) : PlansApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listPlans(): ResponseEntity<PlanListResponse> {
    val items = planUseCase.findAllByUser().map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      PlanListResponse(
        items = items,
        hasMore = false,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getPlan(planId: java.util.UUID): ResponseEntity<HttpPlan> {
    val plan = planUseCase.findById(PlanId(planId))
      ?: return ResponseEntity.notFound().build()
    return ResponseEntity.ok(mapper.toHttp(plan))
  }
}
