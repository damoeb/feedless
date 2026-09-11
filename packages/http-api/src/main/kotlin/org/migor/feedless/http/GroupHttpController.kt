package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.NotFoundException
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.http.api.GroupsApi
import org.migor.feedless.http.api.model.GroupAssignmentListResponse
import org.migor.feedless.http.api.model.GroupCreate
import org.migor.feedless.http.api.model.GroupMemberCreate
import org.migor.feedless.http.api.model.GroupMemberListResponse
import org.migor.feedless.http.mapper.HttpGroupMapper
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.migor.feedless.http.api.model.Group as HttpGroup
import org.migor.feedless.http.api.model.GroupAssignment as HttpGroupAssignment

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.user} & ${AppLayer.api}")
class GroupHttpController(
  private val groupUseCase: GroupUseCasePort,
  private val mapper: HttpGroupMapper,
) : GroupsApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listGroups(): ResponseEntity<GroupAssignmentListResponse> {
    val items = groupUseCase.listAssignments().map { mapper.toHttp(it) }
    return ResponseEntity.ok(
      GroupAssignmentListResponse(
        items = items,
        hasMore = false,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun listGroupMembers(
    groupId: java.util.UUID,
    page: Int,
    pageSize: Int,
  ): ResponseEntity<GroupMemberListResponse> {
    // listMembers already asks for one extra; inflating pageSize here too would shift every later page.
    val fetched = groupUseCase.listMembers(GroupId(groupId), page, pageSize)
    val items = fetched.take(pageSize).map { mapper.toHttpMember(it) }
    return ResponseEntity.ok(
      GroupMemberListResponse(
        items = items,
        hasMore = fetched.size > pageSize,
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun createGroup(groupCreate: GroupCreate): ResponseEntity<HttpGroup> {
    val group = groupUseCase.create(groupCreate.name)
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toHttp(group))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getGroup(groupId: java.util.UUID): ResponseEntity<HttpGroup> {
    val group = groupUseCase.findByIdForUser(GroupId(groupId))
      ?: throw NotFoundException("group $groupId not found")
    return ResponseEntity.ok(mapper.toHttp(group))
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun deleteGroup(groupId: java.util.UUID): ResponseEntity<Unit> {
    groupUseCase.delete(GroupId(groupId))
    return ResponseEntity.noContent().build()
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun addGroupMember(
    groupId: java.util.UUID,
    groupMemberCreate: GroupMemberCreate,
  ): ResponseEntity<HttpGroupAssignment> {
    val assignment = groupUseCase.addUserToGroup(
      UserId(groupMemberCreate.userId),
      GroupId(groupId),
      mapper.toDomainRole(groupMemberCreate.role),
    )
    val name = groupUseCase.findByIdForUser(GroupId(groupId))?.name ?: "-"
    return ResponseEntity.status(HttpStatus.CREATED).body(
      mapper.toHttp(
        org.migor.feedless.group.GroupAssignmentSummary(
          groupId = assignment.groupId,
          role = assignment.role,
          name = name,
        ),
      ),
    )
  }

  @PreAuthorize("@capabilityService.hasCapability('user')")
  @Throttled
  override suspend fun removeGroupMember(
    groupId: java.util.UUID,
    userId: java.util.UUID,
  ): ResponseEntity<Unit> {
    groupUseCase.removeUserFromGroup(GroupId(groupId), UserId(userId))
    return ResponseEntity.noContent().build()
  }
}
