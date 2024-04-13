package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.repositories.OneTimePasswordDAO
import org.migor.feedless.mail.MailService
import org.migor.feedless.user.UserEntity
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.LocalDateTime


@Service
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class OneTimePasswordService {

  private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  fun createOTP(corrId: String, user: UserEntity, description: String): OneTimePasswordEntity {
    val otp = createOTP()
    otp.userId = user.id
    oneTimePasswordDAO.save(otp)
    log.debug("[${corrId}] sending otp '${otp.password}'")
    mailService.sendAuthCode(corrId, user, otp, description)
    return otp
  }

  fun createOTP(): OneTimePasswordEntity {
    val otp = OneTimePasswordEntity()
    otp.password = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase()
    otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
    return otp
  }

}
