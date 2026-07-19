package org.migor.feedless.http.mapper

import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupAssignmentSummary
import org.migor.feedless.userGroup.RoleInGroup
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Group as HttpGroup
import org.migor.feedless.http.api.model.GroupAssignment as HttpGroupAssignment
import org.migor.feedless.http.api.model.GroupAssignment.Role as HttpGroupRole

@Component
class HttpGroupMapper {

  fun toHttp(group: Group): HttpGroup =
    HttpGroup(
      id = group.id.uuid,
      name = group.name,
      ownerId = group.ownerId.uuid,
    )

  fun toHttp(assignment: GroupAssignmentSummary): HttpGroupAssignment =
    HttpGroupAssignment(
      id = assignment.groupId.uuid,
      role = assignment.role.toHttp(),
      name = assignment.name,
    )

  fun toDomainRole(role: HttpGroupRole): RoleInGroup =
    RoleInGroup.valueOf(role.name)

  fun toDomainRole(role: org.migor.feedless.http.api.model.GroupMemberCreate.Role): RoleInGroup =
    RoleInGroup.valueOf(role.name)

  private fun RoleInGroup.toHttp(): HttpGroupRole =
    HttpGroupRole.valueOf(name)
}
