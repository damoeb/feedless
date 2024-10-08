package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppProfiles
import org.migor.feedless.mail.MailService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class OneTimePasswordService {

  private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

  @Autowired
  private lateinit var mailService: MailService

  @Autowired
  private lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  suspend fun createOTP(corrId: String, user: UserEntity, description: String): OneTimePasswordEntity {
    val otp = createOTP()
    otp.userId = user.id
    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.save(otp)
    }
    log.debug("[${corrId}] sending otp '${otp.password}'")
    mailService.sendAuthCode(corrId, user, otp, description)
    return otp
  }

  suspend fun createOTP(): OneTimePasswordEntity {
    val otp = OneTimePasswordEntity()
    otp.password = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase()
    otp.validUntil = LocalDateTime.now().plusMinutes(otpValidForMinutes)
    return otp
  }

}
