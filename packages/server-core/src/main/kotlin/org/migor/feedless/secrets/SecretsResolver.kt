package org.migor.feedless.secrets

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.DeleteUserSecretInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.UserSecret
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.injectCurrentUser
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.secrets} & ${AppLayer.api}")
class SecretsResolver(
  private val userSecretService: UserSecretService,
  private val sessionService: SessionService,
) {

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME, field = DgsConstants.USER.Secrets)
  suspend fun secrets(dfe: DgsDataFetchingEnvironment): List<UserSecret> = coroutineScope {
    val user: User = dfe.getSourceOrThrow()
    userSecretService.findAllByOwnerId(UserId(user.id)).map { it.toDto() }
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateUserSecret)
  @PreAuthorize("hasCapability('user')")
  suspend fun createUserSecret(
    dfe: DataFetchingEnvironment,
  ): UserSecret = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    userSecretService.createUserSecret(sessionService.user()).toDto(false)
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteUserSecret)
  @PreAuthorize("hasCapability('user')")
  suspend fun deleteUserSecret(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.DELETEUSERSECRET_INPUT_ARGUMENT.Data) data: DeleteUserSecretInput,
  ): Boolean = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    userSecretService.deleteUserSecret(
      sessionService.user(),
      UUID.fromString(data.where.`eq`)
    )
    true
  }
}
