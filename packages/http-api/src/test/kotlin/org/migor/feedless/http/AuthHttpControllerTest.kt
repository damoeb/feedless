package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.auth.AuthUseCasePort
import org.migor.feedless.auth.AuthenticatedUser
import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.group.GroupId
import org.migor.feedless.http.mapper.HttpAuthMapper
import org.migor.feedless.http.mapper.HttpGroupMapper
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
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

/**
 * [AutoConfigureMockMvc] disables servlet filters (`addFilters = false`), so [HttpApiJwtFilter]
 * (which lives in `server-core` and is not on this module's test classpath anyway) never runs here.
 * The 401 case below is therefore exercised purely through [AuthUseCasePort] throwing
 * [AuthCredentialsException], which [HttpApiExceptionHandler] maps to 401 — it does NOT assert
 * anything about filter-level/whitelist behavior. Filter- and whitelist-level auth enforcement is
 * covered separately by `HttpApiJwtFilterTest` and `SecurityConfigIntTest` in `server-core`.
 */
@WebMvcTest(controllers = [AuthHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpAuthMapper::class, HttpGroupMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.user)
class AuthHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var authUseCasePort: AuthUseCasePort

  @Test
  fun `getAuthenticatedUser returns 200 with id, email and groups`() = runTest {
    val userId = UserId()
    val groupId = GroupId()
    whenever(authUseCasePort.currentUser()).thenReturn(
      AuthenticatedUser(
        id = userId,
        email = "user@example.com",
        groups = listOf(
          GroupAssignmentSummary(groupId = groupId, role = RoleInGroup.owner, name = "team"),
        ),
      ),
    )

    val mvcResult = mockMvc.get("/api/v1/user").andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isOk,
      jsonPath("$.id").value(userId.uuid.toString()),
      jsonPath("$.email").value("user@example.com"),
      jsonPath("$.groups[0].id").value(groupId.uuid.toString()),
      jsonPath("$.groups[0].name").value("team"),
      jsonPath("$.groups[0].role").value("owner"),
    )
  }

  @Test
  fun `getAuthenticatedUser returns 401 when unauthenticated`() = runTest {
    whenever(authUseCasePort.currentUser()).thenThrow(AuthCredentialsException("authentication required"))

    val mvcResult = mockMvc.get("/api/v1/user").andReturn()

    dispatchIfAsync(mvcResult, status().isUnauthorized, jsonPath("$.code").value("UNAUTHORIZED"))
  }

  private fun dispatchIfAsync(
    mvcResult: org.springframework.test.web.servlet.MvcResult,
    expectedStatus: org.springframework.test.web.servlet.ResultMatcher,
    vararg extraMatchers: org.springframework.test.web.servlet.ResultMatcher,
  ) {
    if (mvcResult.request.asyncContext != null) {
      var actions = mockMvc.perform(asyncDispatch(mvcResult)).andExpect(expectedStatus)
      extraMatchers.forEach { actions = actions.andExpect(it) }
    } else {
      expectedStatus.match(mvcResult)
    }
  }
}
