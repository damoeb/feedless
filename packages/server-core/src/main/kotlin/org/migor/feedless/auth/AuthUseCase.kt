package org.migor.feedless.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.session.AuthCredentialsException
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.userIdMaybe
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.user} & ${AppLayer.service}")
class AuthUseCase(
  private val userRepository: UserRepository,
  private val groupUseCasePort: GroupUseCasePort,
) : AuthUseCasePort {

  private val log = LoggerFactory.getLogger(AuthUseCase::class.simpleName)

  override suspend fun currentUser(): AuthenticatedUser = withContext(Dispatchers.IO) {
    val userId = coroutineContext.userIdMaybe()
      ?: throw AuthCredentialsException("authentication required")
    val user = userRepository.findById(userId)
      ?: throw AuthCredentialsException("authentication required")
    log.debug("currentUser userId=$userId")
    AuthenticatedUser(
      id = user.id,
      email = user.email,
      groups = groupUseCasePort.listAssignments(),
    )
  }
}
