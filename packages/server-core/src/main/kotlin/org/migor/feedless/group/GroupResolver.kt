package org.migor.feedless.group

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.GroupAssignment
import org.migor.feedless.generated.types.Role
import org.migor.feedless.generated.types.User
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.user} & ${AppLayer.api}")
class GroupResolver(
  private val groupService: GroupService
) {

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME)
  suspend fun groups(dfe: DgsDataFetchingEnvironment): List<GroupAssignment> = coroutineScope {
    val user: User = dfe.getSourceOrThrow()
    groupService.findAllByUserId(UserId(user.id))
      .map {
        GroupAssignment(
          id = it.groupId.toString(),
          name = it.group!!.name,
          role = it.role.toDto()
        )
      }
  }
}

private fun RoleInGroup.toDto(): Role {
  return when (this) {
    RoleInGroup.editor -> Role.editor
    RoleInGroup.viewer -> Role.viewer
    RoleInGroup.owner -> Role.owner
  }
}
