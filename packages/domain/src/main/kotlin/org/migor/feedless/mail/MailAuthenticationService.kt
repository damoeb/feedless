package org.migor.feedless.mail

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.session.actingGroupOf
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
@Profile("${AppProfiles.mail} & ${AppProfiles.session} & ${AppLayer.service}")
class MailAuthenticationService(
  private val tokenIssuer: TokenIssuer,
  private val oneTimePasswordRepository: OneTimePasswordRepository,
  private val userUseCase: UserUseCase,
  private val featureService: FeatureService,
  private val userRepository: UserRepository,
  private val mailService: MailService,
  private val oneTimePasswordService: OneTimePasswordService,
  private val userGroupAssignmentRepository: UserGroupAssignmentRepository,
) {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

  suspend fun authenticateUsingMail(email: String, allowCreate: Boolean, osInfo: String): OtpChallenge =
    withContext(Dispatchers.IO) {
      log.debug("init user session for $email")
      if (featureService.isDisabled(FeatureName.canLogin, null)) {
        throw UnavailableException("login is deactivated by feature flag")
      }

      val user = resolveUserByMail(email, allowCreate)

      val otp = if (user == null) {
        val anonymousUser = userRepository.findByAnonymousUser()
        oneTimePasswordRepository.save(
          OneTimePassword(
            userId = anonymousUser.id,
          )
        )
      } else {
        val t = oneTimePasswordService.createOTP(user)
        mailService.sendAuthCode(user, t, osInfo)
        t
      }

      delay(1000)

      OtpChallenge(
        length = otp.password.length,
        otpId = otp.id
      )
    }

  private suspend fun resolveUserByMail(email: String, allowCreate: Boolean): User? {
    return userRepository.findByEmail(email) ?: if (allowCreate) {
      userUseCase.createUser(email)
    } else {
      null
    }
  }

  suspend fun confirmAuthCode(otpId: OneTimePasswordId, code: String): AuthToken {
    delay(Random.nextLong(600, 701))
    val error = PermissionDeniedException("Please retry")
    val otp = oneTimePasswordRepository.findById(otpId)!!

    if (isOtpExpired(otp)) {
      throw error
    }

    if (otp.attemptsLeft < 1) {
      throw error
    }

    oneTimePasswordRepository.save(otp.copy(attemptsLeft = otp.attemptsLeft - 1))

    if (otp.password != code) {
      throw error
    }

    val actingGroup = userGroupAssignmentRepository.actingGroupOf(otp.userId)

    oneTimePasswordRepository.deleteById(otpId)

    return tokenIssuer.issueTokenForCapabilities(listOf(UserCapability(otp.userId), GroupCapability(actingGroup)))
  }

  private fun isOtpExpired(otp: OneTimePassword) =
    otp.validUntil.isBefore(LocalDateTime.now())

}
