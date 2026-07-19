package org.migor.feedless.plan

interface PlanUseCasePort {
  suspend fun findById(id: PlanId): Plan?

  suspend fun findAllByUser(): List<Plan>
}
