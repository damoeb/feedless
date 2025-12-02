package org.migor.feedless.secrets

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordEntity
import org.migor.feedless.data.jpa.oneTimePassword.toDomain
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.security} & ${AppLayer.service}")
class OneTimePasswordService(
  private val oneTimePasswordRepository: OneTimePasswordRepository
) {

  private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  @Transactional
  suspend fun createOTP(user: User): OneTimePassword {
    val otp = createOTP()
      .copy(
        userId = user.id
      )
    oneTimePasswordRepository.findFirstByUserIdOrderByCreatedAtDesc(user.id)?.let {
      val now = LocalDateTime.now()
      if (it.createdAt.isAfter(now.minusSeconds(30))) {
        throw ResumableHarvestException("Token cooldown", Duration.between(it.createdAt, now))
      }

      oneTimePasswordRepository.save(otp)
    }
    log.debug("sending otp '${otp.password}'")
    return otp
  }

  suspend fun createOTP(): OneTimePassword {
    val otp = OneTimePasswordEntity()
    otp.password = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase()
    otp.validUntil = LocalDateTime.now().plusMinutes(otpValidForMinutes)
    return otp.toDomain()
  }

  @Transactional
  suspend fun deleteAllByValidUntilBefore(now: LocalDateTime) {
    oneTimePasswordRepository.deleteAllByValidUntilBefore(now)
  }

}
