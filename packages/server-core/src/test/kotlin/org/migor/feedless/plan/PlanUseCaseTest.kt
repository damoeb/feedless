package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.any
import org.migor.feedless.session.RequestContext
import org.migor.feedless.user.UserId
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PlanUseCaseTest {

  private lateinit var planRepository: PlanRepository
  private lateinit var planUseCase: PlanUseCase
  private lateinit var userId: UserId

  @BeforeEach
  fun before() {
    planRepository = mock(PlanRepository::class.java)
    planUseCase = PlanUseCase(planRepository)
    userId = UserId()
  }

  @Test
  fun `findById throws for non-owners`() = runTest(context = RequestContext(userId = userId)) {
    val plan = mock(Plan::class.java)
    `when`(plan.userId).thenReturn(UserId())
    `when`(planRepository.findById(any(PlanId::class.java))).thenReturn(plan)

    assertThrows<PermissionDeniedException> {
      planUseCase.findById(PlanId())
    }

  }

  @Test
  fun `findById returns plan to owner`() = runTest(context = RequestContext(userId = userId)) {
    val plan = mock(Plan::class.java)
    `when`(plan.userId).thenReturn(userId)
    `when`(planRepository.findById(any(PlanId::class.java))).thenReturn(plan)

    val actualPlan = planUseCase.findById(PlanId())

    assertThat(actualPlan).isEqualTo(plan)
  }
}
