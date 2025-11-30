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
import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordDAO
import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordEntity
import org.migor.feedless.data.jpa.user.UserDAO
import org.migor.feedless.data.jpa.user.toDomain
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.mail} & ${AppProfiles.session} & ${AppLayer.service}")
class MailAuthenticationService(
  private val jwtTokenIssuer: JwtTokenIssuer,
  private val cookieProvider: CookieProvider,
  private val oneTimePasswordDAO: OneTimePasswordDAO,
  private val userService: UserService,
  private val featureService: FeatureService,
  private val userDAO: UserDAO,
  private val mailService: MailService,
  private val oneTimePasswordService: OneTimePasswordService
) {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun authenticateUsingMail(data: AuthViaMailInput): ConfirmCode {
    val email = data.email
    log.debug("init user session for $email")
    if (featureService.isDisabled(FeatureName.canLogin, null)) {
      throw UnavailableException("login is deactivated by feature flag")
    }

    val user = resolveUserByMail(data)

    val otp = if (user == null) {
      oneTimePasswordService.createOTP()
    } else {
      val t = oneTimePasswordService.createOTP(user)
      mailService.sendAuthCode(user, t, data.osInfo)
      t
    }

    delay(1000)

    return ConfirmCode(
      length = otp.password.length,
      otpId = otp.id.toString()
    )
  }

  private suspend fun resolveUserByMail(data: AuthViaMailInput): User? {
    return withContext(Dispatchers.IO) { userDAO.findByEmail(data.email)?.toDomain() } ?: if (data.allowCreate) {
      userService.createUser(data.email)
    } else {
      null
    }
  }

  suspend fun confirmAuthCode(codeInput: ConfirmAuthCodeInput, response: HttpServletResponse): Authentication {
    delay(Random.nextLong(600, 701))
    val otpId = UUID.fromString(codeInput.otpId)
    val error = PermissionDeniedException("Please retry")
    val otp = withContext(Dispatchers.IO) {
      oneTimePasswordDAO.findById(otpId).orElseThrow { error }
    }

    if (isOtpExpired(otp)) {
      throw error
    }

    if (otp.attemptsLeft < 1) {
      throw error
    }

    otp.attemptsLeft -= 1
    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.save(otp)
    }

    if (otp.password != codeInput.code) {
      throw error
    }

    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.deleteById(otpId)
    }

    val jwt = jwtTokenIssuer.createJwtForCapabilities(listOf(UserCapability(UserId(otp.userId))))

    response.addCookie(cookieProvider.createTokenCookie(jwt))

    return Authentication(
      corrId = "",
      token = jwt.tokenValue
    )
  }

  private fun isOtpExpired(otp: OneTimePasswordEntity) =
    otp.validUntil.isBefore(LocalDateTime.now())

}
