package org.migor.feedless.http

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ConflictException
import org.migor.feedless.NotFoundException
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.http.mapper.HttpGroupMapper
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
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
  private lateinit var groupUseCase: GroupUseCase

  @Test
  fun `createGroup returns 201`() = runTest {
    val ownerId = UserId()
    val group = Group(name = "team", ownerId = ownerId)
    whenever(groupUseCase.create("team")).thenReturn(group)

    val mvcResult = mockMvc.post("/api/v1/groups") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"name":"team"}"""
    }.andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isCreated,
      jsonPath("$.name").value("team"),
      jsonPath("$.ownerId").value(ownerId.uuid.toString()),
    )
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

    dispatchIfAsync(
      mvcResult,
      status().isOk,
      jsonPath("$.items[0].id").value(groupId.uuid.toString()),
      jsonPath("$.items[0].name").value("team"),
      jsonPath("$.items[0].role").value("owner"),
      jsonPath("$.hasMore").value(false),
    )
  }

  @Test
  fun `getGroup returns group`() = runTest {
    val group = Group(name = "team", ownerId = UserId())
    whenever(groupUseCase.findByIdForUser(eq(group.id))).thenReturn(group)

    val mvcResult = mockMvc.get("/api/v1/groups/${group.id.uuid}").andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isOk,
      jsonPath("$.id").value(group.id.uuid.toString()),
      jsonPath("$.name").value("team"),
    )
  }

  @Test
  fun `getGroup returns 404 when missing`() = runTest {
    val groupId = GroupId()
    whenever(groupUseCase.findByIdForUser(eq(groupId))).thenReturn(null)

    val mvcResult = mockMvc.get("/api/v1/groups/${groupId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound)
  }

  @Test
  fun `getGroup returns 403 when not member`() = runTest {
    val groupId = GroupId()
    whenever(groupUseCase.findByIdForUser(eq(groupId))).thenThrow(PermissionDeniedException("not a member"))

    val mvcResult = mockMvc.get("/api/v1/groups/${groupId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isForbidden, jsonPath("$.code").value("FORBIDDEN"))
  }

  @Test
  fun `deleteGroup returns 204`() = runTest {
    val groupId = GroupId()

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isNoContent)
  }

  @Test
  fun `deleteGroup returns 404 when missing`() = runTest {
    val groupId = GroupId()
    doThrow(NotFoundException("group not found")).whenever(groupUseCase).delete(eq(groupId))

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound, jsonPath("$.code").value("NOT_FOUND"))
  }

  @Test
  fun `deleteGroup returns 409 CONFLICT when the delete would lock a member out`() = runTest {
    val groupId = GroupId()
    doThrow(ConflictException("The group still owns repositories. Delete them before deleting the group."))
      .whenever(groupUseCase).delete(eq(groupId))

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}").andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isConflict,
      jsonPath("$.code").value("CONFLICT"),
      jsonPath("$.message").value("The group still owns repositories. Delete them before deleting the group."),
    )
  }

  @Test
  fun `listGroupMembers returns members`() = runTest {
    val groupId = GroupId()
    val memberId = UserId()
    whenever(groupUseCase.listMembers(eq(groupId), eq(0), eq(20))).thenReturn(
      listOf(
        UserGroupAssignment(
          userId = memberId,
          groupId = groupId,
          role = RoleInGroup.viewer,
        ),
      ),
    )

    val mvcResult = mockMvc.get("/api/v1/groups/${groupId.uuid}/members") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isOk,
      jsonPath("$.items[0].userId").value(memberId.uuid.toString()),
      jsonPath("$.items[0].role").value("viewer"),
      jsonPath("$.hasMore").value(false),
    )
  }

  @Test
  fun `listGroupMembers returns 404 when group missing`() = runTest {
    val groupId = GroupId()
    whenever(groupUseCase.listMembers(eq(groupId), eq(0), eq(20)))
      .thenThrow(NotFoundException("group not found"))

    val mvcResult = mockMvc.get("/api/v1/groups/${groupId.uuid}/members") {
      param("page", "0")
      param("pageSize", "20")
    }.andReturn()

    dispatchIfAsync(mvcResult, status().isNotFound, jsonPath("$.code").value("NOT_FOUND"))
  }

  @Test
  fun `addGroupMember returns 201`() = runTest {
    val group = Group(name = "team", ownerId = UserId())
    val memberId = UserId()
    val assignment = UserGroupAssignment(
      userId = memberId,
      groupId = group.id,
      role = RoleInGroup.viewer,
    )
    whenever(
      groupUseCase.addUserToGroup(eq(memberId), eq(group.id), eq(RoleInGroup.viewer)),
    ).thenReturn(assignment)
    whenever(groupUseCase.findByIdForUser(eq(group.id))).thenReturn(group)

    val mvcResult = mockMvc.post("/api/v1/groups/${group.id.uuid}/members") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"userId":"${memberId.uuid}","role":"viewer"}"""
    }.andReturn()

    dispatchIfAsync(
      mvcResult,
      status().isCreated,
      jsonPath("$.id").value(group.id.uuid.toString()),
      jsonPath("$.name").value("team"),
      jsonPath("$.role").value("viewer"),
    )
  }

  @Test
  fun `addGroupMember returns 403 when not allowed`() = runTest {
    val groupId = GroupId()
    val memberId = UserId()
    doThrow(PermissionDeniedException("user is not owner of this group"))
      .whenever(groupUseCase)
      .addUserToGroup(eq(memberId), eq(groupId), eq(RoleInGroup.viewer))

    val mvcResult = mockMvc.post("/api/v1/groups/${groupId.uuid}/members") {
      contentType = MediaType.APPLICATION_JSON
      content = """{"userId":"${memberId.uuid}","role":"viewer"}"""
    }.andReturn()

    dispatchIfAsync(mvcResult, status().isForbidden, jsonPath("$.code").value("FORBIDDEN"))
  }

  @Test
  fun `removeGroupMember returns 204`() = runTest {
    val groupId = GroupId()
    val memberId = UserId()

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}/members/${memberId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isNoContent)
  }

  @Test
  fun `removeGroupMember returns 403 when not allowed`() = runTest {
    val groupId = GroupId()
    val memberId = UserId()
    doThrow(PermissionDeniedException("user is not owner of this group"))
      .whenever(groupUseCase)
      .removeUserFromGroup(eq(groupId), eq(memberId))

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}/members/${memberId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isForbidden, jsonPath("$.code").value("FORBIDDEN"))
  }

  @Test
  fun `removeGroupMember returns 409 CONFLICT when removing the last owner`() = runTest {
    val groupId = GroupId()
    val memberId = UserId()
    doThrow(ConflictException("This is the group's last owner. Add another owner before removing this one."))
      .whenever(groupUseCase)
      .removeUserFromGroup(eq(groupId), eq(memberId))

    val mvcResult = mockMvc.delete("/api/v1/groups/${groupId.uuid}/members/${memberId.uuid}").andReturn()

    dispatchIfAsync(mvcResult, status().isConflict, jsonPath("$.code").value("CONFLICT"))
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
