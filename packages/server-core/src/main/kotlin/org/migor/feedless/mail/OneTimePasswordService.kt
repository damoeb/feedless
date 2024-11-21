package org.migor.feedless.mail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.mail} & ${AppLayer.service}")
class OneTimePasswordService(
  private val mailService: MailService,
  private val oneTimePasswordDAO: OneTimePasswordDAO
) {

  private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  @Transactional
  suspend fun createOTP(user: UserEntity, description: String): OneTimePasswordEntity {
    val otp = createOTP()
    otp.userId = user.id
    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.save(otp)
    }
    log.debug("sending otp '${otp.password}'")
    mailService.sendAuthCode(user, otp, description)
    return otp
  }

  suspend fun createOTP(): OneTimePasswordEntity {
    val otp = OneTimePasswordEntity()
    otp.password = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase()
    otp.validUntil = LocalDateTime.now().plusMinutes(otpValidForMinutes)
    return otp
  }

  @Transactional
  fun deleteAllByValidUntilBefore(now: LocalDateTime) {
    oneTimePasswordDAO.deleteAllByValidUntilBefore(now)
  }

}
