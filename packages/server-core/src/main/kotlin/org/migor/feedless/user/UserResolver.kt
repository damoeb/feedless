package org.migor.feedless.user

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.fromDto
import org.migor.feedless.generated.types.CreateUserInput
import org.migor.feedless.generated.types.UpdateCurrentUserInput
import org.migor.feedless.generated.types.User
import org.migor.feedless.plan.fromDto
import org.migor.feedless.session.SessionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile(AppProfiles.database)
class UserResolver {

  private val log = LoggerFactory.getLogger(UserResolver::class.simpleName)

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var currentUser: SessionService

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('ANONYMOUS')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun createUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: CreateUserInput
  ): User = coroutineScope {
    log.info("[$corrId] createUser $data")
    userService.createUser(
      corrId,
      email = data.email,
      productName = data.product.fromDto(),
      AuthSource.email,
      planName = data.plan.fromDto()
    ).toDto()
  }

  @Throttled
  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun updateCurrentUser(
    @RequestHeader(ApiParams.corrId) corrId: String,
    @InputArgument data: UpdateCurrentUserInput,
  ): Boolean = coroutineScope {
    log.info("[$corrId] updateCurrentUser ${currentUser.userId()} $data")
    userService.updateUser(corrId, currentUser.userId()!!, data)
    true
  }
}
