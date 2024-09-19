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
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.DeleteUserSecretInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.generated.types.UserSecret
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.useRequestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile("${AppProfiles.secrets} & ${AppLayer.api}")
@Transactional
class SecretsResolver {

  @Autowired
  private lateinit var userSecretService: UserSecretService

  @Autowired
  private lateinit var sessionService: SessionService

  @DgsData(parentType = DgsConstants.USER.TYPE_NAME, field = DgsConstants.USER.Secrets)
  suspend fun secrets(dfe: DgsDataFetchingEnvironment): List<UserSecret> = coroutineScope {
    val user: User = dfe.getSource()!!
    userSecretService.findAllByOwnerId(UUID.fromString(user.id)).map { it.toDto() }
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.CreateUserSecret)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun createUserSecret(
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): UserSecret = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    userSecretService.createUserSecret(corrId, sessionService.user(corrId)).toDto(false)
  }


  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.DeleteUserSecret)
  @PreAuthorize("hasAuthority('USER')")
  suspend fun deleteUserSecret(
    dfe: DataFetchingEnvironment,
    @InputArgument data: DeleteUserSecretInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = withContext(useRequestContext(currentCoroutineContext(), dfe)) {
    userSecretService.deleteUserSecret(
      corrId,
      sessionService.user(corrId),
      UUID.fromString(data.where.`eq`)
    )
    true
  }
}
