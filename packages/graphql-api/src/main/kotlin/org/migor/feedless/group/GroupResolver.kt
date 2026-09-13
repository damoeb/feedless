package org.migor.feedless.group

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.user.UserId
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.springframework.context.annotation.Profile
import org.migor.feedless.generated.types.GroupAssignment as GroupAssignmentDto
import org.migor.feedless.generated.types.Role as RoleDto
import org.migor.feedless.generated.types.User as UserDto

@DgsComponent
@Profile("${AppProfiles.user} & ${AppLayer.api}")
class GroupResolver(
  private val groupRepository: GroupRepository,
  private val groupUseCase: GroupUseCase,
) {

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  suspend fun groups(dfe: DgsDataFetchingEnvironment): List<GroupAssignmentDto> =
    coroutineScope {
      val user: UserDto = dfe.getSourceOrThrow()
      groupUseCase.findAllByUserId(UserId(user.id))
        .map {
          GroupAssignmentDto(
            id = it.groupId.toString(),
            name = it.group()?.name ?: "-",
            role = it.role.toDto()
          )
        }
    }

  private suspend fun UserGroupAssignment.group(): Group? {
    return groupRepository.findById(groupId)
  }
}

private fun RoleInGroup.toDto(): RoleDto {
  return when (this) {
    RoleInGroup.editor -> RoleDto.editor
    RoleInGroup.viewer -> RoleDto.viewer
    RoleInGroup.owner -> RoleDto.owner
  }
}
