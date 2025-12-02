package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.random.Random

@Service
@Profile("${AppProfiles.mail} & ${AppProfiles.session} & ${AppLayer.service}")
class MailAuthenticationService(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val cookieProvider: CookieProvider,
  private val oneTimePasswordRepository: OneTimePasswordRepository,
  private val userUseCase: UserUseCase,
  private val featureService: FeatureService,
  private val userRepository: UserRepository,
  private val mailService: MailService,
  private val oneTimePasswordService: OneTimePasswordService
) {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

  suspend fun authenticateUsingMail(data: AuthViaMailInput): ConfirmCode = withContext(Dispatchers.IO) {
    val email = data.email
    log.debug("init user session for $email")
    if (featureService.isDisabled(FeatureName.canLogin, null)) {
      throw UnavailableException("login is deactivated by feature flag")
    }

    val user = resolveUserByMail(data)

    val otp = if (user == null) {
      val anonymousUser = userRepository.findByAnonymousUser()
      oneTimePasswordRepository.save(
        OneTimePassword(
          userId = anonymousUser.id,
        )
      )
    } else {
      val t = oneTimePasswordService.createOTP(user)
      mailService.sendAuthCode(user, t, data.osInfo)
      t
    }

    delay(1000)

    ConfirmCode(
      length = otp.password.length,
      otpId = otp.id.uuid.toString()
    )
  }

  private suspend fun resolveUserByMail(data: AuthViaMailInput): User? {
    return userRepository.findByEmail(data.email) ?: if (data.allowCreate) {
      userUseCase.createUser(data.email)
    } else {
      null
    }
  }

  suspend fun confirmAuthCode(codeInput: ConfirmAuthCodeInput, response: HttpServletResponse): Authentication {
    delay(Random.nextLong(600, 701))
    val otpId = OneTimePasswordId(codeInput.otpId)
    val error = PermissionDeniedException("Please retry")
    val otp = oneTimePasswordRepository.findById(otpId)!!

    if (isOtpExpired(otp)) {
      throw error
    }

    if (otp.attemptsLeft < 1) {
      throw error
    }

    oneTimePasswordRepository.save(otp.copy(attemptsLeft = otp.attemptsLeft - 1))

    if (otp.password != codeInput.code) {
      throw error
    }

    oneTimePasswordRepository.deleteById(otpId)

    val jwt = jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(otp.userId)))

    response.addCookie(cookieProvider.createTokenCookie(jwt))

    return Authentication(
      corrId = "",
      token = jwt.tokenValue
    )
  }

  private fun isOtpExpired(otp: OneTimePassword) =
    otp.validUntil.isBefore(LocalDateTime.now())

}
