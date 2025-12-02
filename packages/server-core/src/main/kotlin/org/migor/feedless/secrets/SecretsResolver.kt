package org.migor.feedless.secrets

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.api.toDto
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.DeleteUserSecretInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.UserSecret
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserId
import org.migor.feedless.userSecret.UserSecretId
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize

@DgsComponent
@Profile("${AppProfiles.secrets} & ${AppLayer.api}")
class SecretsResolver(
  private val userSecretUseCase: UserSecretUseCase,
  private val userSecretRepository: UserSecretRepository,
  private val sessionService: SessionService,
) {

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME, field = DgsConstants.USER.Secrets)
  suspend fun secrets(dfe: DgsDataFetchingEnvironment): List<UserSecret> = coroutineScope {
    val user: User = dfe.getSourceOrThrow()
    withContext(Dispatchers.IO) {
      userSecretRepository.findAllByOwnerId(UserId(user.id)).map { it.toDto() }
    }
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateUserSecret)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun createUserSecret(
    dfe: DataFetchingEnvironment,
  ): UserSecret = coroutineScope {
    userSecretUseCase.createUserSecret(sessionService.user()).toDto(false)
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteUserSecret)
  @PreAuthorize("@capabilityService.hasCapability('user')")
  suspend fun deleteUserSecret(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEUSERSECRET_INPUT_ARGUMENT.Data) data: DeleteUserSecretInput,
  ): Boolean = coroutineScope {
    userSecretUseCase.deleteUserSecret(
      sessionService.user(),
      UserSecretId(data.where.`eq`)
    )
    true
  }
}
