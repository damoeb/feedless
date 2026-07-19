package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.GroupId
import org.migor.feedless.http.mapper.HttpPlanMapper
import org.migor.feedless.plan.Plan
import org.migor.feedless.plan.PlanId
import org.migor.feedless.plan.PlanUseCasePort
import org.migor.feedless.product.ProductId
import org.migor.feedless.user.UserId
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(controllers = [PlanHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpPlanMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.plan)
class PlanHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var planUseCase: PlanUseCasePort

  @Test
  fun `listPlans returns items with recurringYearly false`() = runTest {
    val plan = plan()
    whenever(planUseCase.findAllByUser()).thenReturn(listOf(plan))

    val mvcResult = mockMvc.get("/api/v1/plans").andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].id").value(plan.id.uuid.toString()))
        .andExpect(jsonPath("$.items[0].productId").value(plan.productId.uuid.toString()))
        .andExpect(jsonPath("$.items[0].recurringYearly").value(false))
        .andExpect(jsonPath("$.hasMore").value(false))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(plan.id.uuid.toString()))
      assert(mvcResult.response.contentAsString.contains("\"recurringYearly\":false"))
    }
  }

  @Test
  fun `getPlan returns plan`() = runTest {
    val plan = plan()
    whenever(planUseCase.findById(eq(plan.id))).thenReturn(plan)

    val mvcResult = mockMvc.get("/api/v1/plans/${plan.id.uuid}").andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(plan.id.uuid.toString()))
        .andExpect(jsonPath("$.productId").value(plan.productId.uuid.toString()))
        .andExpect(jsonPath("$.recurringYearly").value(false))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(plan.id.uuid.toString()))
    }
  }

  @Test
  fun `getPlan returns 404 when missing`() = runTest {
    val planId = PlanId()
    whenever(planUseCase.findById(eq(planId))).thenReturn(null)

    val mvcResult = mockMvc.get("/api/v1/plans/${planId.uuid}").andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isNotFound)
    } else {
      assert(mvcResult.response.status == 404)
    }
  }

  private fun plan(
    id: PlanId = PlanId(),
    productId: ProductId = ProductId(),
  ) = Plan(
    id = id,
    userId = UserId(),
    groupId = GroupId(),
    productId = productId,
    startedAt = LocalDateTime.of(2024, 6, 1, 10, 0),
    terminatedAt = null,
  )
}
