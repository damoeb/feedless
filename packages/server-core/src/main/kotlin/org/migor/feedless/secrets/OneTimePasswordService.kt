package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime


@Service
@Profile("${AppProfiles.security} & ${AppLayer.service}")
class OneTimePasswordService(
  private val oneTimePasswordRepository: OneTimePasswordRepository
) {

  private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

  suspend fun createOTP(user: User): OneTimePassword = withContext(Dispatchers.IO) {
    val otp = OneTimePassword(
      userId = user.id,
      attemptsLeft = 3
    )
    oneTimePasswordRepository.findFirstByUserIdOrderByCreatedAtDesc(user.id)?.let {
      val now = LocalDateTime.now()
      if (it.createdAt.isAfter(now.minusSeconds(30))) {
        throw ResumableHarvestException("Token cooldown", Duration.between(it.createdAt, now))
      }

      oneTimePasswordRepository.save(otp)
    }
    otp
  }

  fun deleteAllByValidUntilBefore(now: LocalDateTime) {
    oneTimePasswordRepository.deleteAllByValidUntilBefore(now)
  }

}
