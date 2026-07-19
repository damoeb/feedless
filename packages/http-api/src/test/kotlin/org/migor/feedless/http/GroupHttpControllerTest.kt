package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.http.mapper.HttpGroupMapper
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [GroupHttpController::class])
@AutoConfigureMockMvc(addFilters = false)
@Import(HttpGroupMapper::class, HttpApiExceptionHandler::class)
@ActiveProfiles("test", AppLayer.api, AppProfiles.user)
class GroupHttpControllerTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @MockitoBean
  private lateinit var groupUseCase: GroupUseCasePort

  @Test
  fun `createGroup returns 201`() = runTest {
    val ownerId = UserId()
    val group = Group(name = "team", ownerId = ownerId)
    whenever(groupUseCase.create("team")).thenReturn(group)

    val mvcResult = mockMvc.post("/api/v1/groups") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"name":"team"}"""
    }.andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.name").value("team"))
        .andExpect(jsonPath("$.ownerId").value(ownerId.uuid.toString()))
    } else {
      assert(mvcResult.response.status == 201)
      assert(mvcResult.response.contentAsString.contains("\"name\":\"team\""))
    }
  }

  @Test
  fun `listGroups returns assignments`() = runTest {
    val groupId = GroupId()
    whenever(groupUseCase.listAssignments()).thenReturn(
      listOf(
        GroupAssignmentSummary(
          groupId = groupId,
          role = RoleInGroup.owner,
          name = "team",
        ),
      ),
    )

    val mvcResult = mockMvc.get("/api/v1/groups").andReturn()

    if (mvcResult.request.asyncContext != null) {
      mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.items[0].id").value(groupId.uuid.toString()))
        .andExpect(jsonPath("$.items[0].name").value("team"))
        .andExpect(jsonPath("$.items[0].role").value("owner"))
        .andExpect(jsonPath("$.hasMore").value(false))
    } else {
      assert(mvcResult.response.status == 200)
      assert(mvcResult.response.contentAsString.contains(groupId.uuid.toString()))
      assert(mvcResult.response.contentAsString.contains("\"name\":\"team\""))
    }
  }
}
