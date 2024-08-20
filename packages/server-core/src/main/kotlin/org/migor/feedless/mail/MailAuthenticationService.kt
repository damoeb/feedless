package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.plan.FeatureName
import org.migor.feedless.plan.FeatureService
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.migor.feedless.secrets.OneTimePasswordEntity
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class MailAuthenticationService {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var cookieProvider: CookieProvider

  @Autowired
  private lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  private lateinit var userService: UserService

  @Autowired
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var oneTimePasswordService: OneTimePasswordService

  fun authenticateUsingMail(corrId: String, data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val email = data.email
    log.info("[${corrId}] init user session for $email")
    return Flux.create { emitter ->
      run {
        try {
          if (featureService.isDisabled(FeatureName.canLogin, null)) {
            throw UnavailableException("login is deactivated by feature flag")
          }

          val user = resolveUserByMail(corrId, data)

          val otp = if (user == null) {
            oneTimePasswordService.createOTP()
          } else {
            oneTimePasswordService.createOTP(corrId, user, data.osInfo)
          }

          Mono.delay(Duration.ofSeconds(2)).subscribe {
            emitter.next(
              AuthenticationEvent(
                confirmCode = ConfirmCode(
                  length = otp.password.length,
                  otpId = otp.id.toString()
                )
              )
            )
            emitter.complete()
          }
          emitter.onDispose { log.debug("[$corrId] disconnected") }

        } catch (e: Exception) {
          log.error("[$corrId] authenticateUsingMail failed: ${e.message}")
          emitter.error(e)
        }
      }
    }
  }

  private fun resolveUserByMail(corrId: String, data: AuthViaMailInput): UserEntity? {
    return userDAO.findByEmail(data.email) ?: if (data.allowCreate) {
      createUser(corrId, data.email)
    } else {
      null
    }
  }

  private fun createUser(corrId: String, email: String): UserEntity {
    return userService.createUser(corrId, email)
  }

  fun confirmAuthCode(corrId: String, codeInput: ConfirmAuthCodeInput, response: HttpServletResponse) {
    val otpId = UUID.fromString(codeInput.otpId)
    val otp = oneTimePasswordDAO.findById(otpId)

    otp.ifPresentOrElse({
      if (isOtpExpired(it)) {
        throw PermissionDeniedException("code expired. Please restart authentication ($corrId)")
      }
      if (it.password != codeInput.code) {
        throw PermissionDeniedException("invalid code ($corrId)")
      }

      oneTimePasswordDAO.deleteById(otpId)

      val jwt = tokenProvider.createJwtForUser(it.user!!)

      response.addCookie(cookieProvider.createTokenCookie(corrId, jwt))
    },
      {
        log.error("confirmAuthCode failed: otp not found ($corrId)")
      })
  }

  private fun isOtpExpired(otp: OneTimePasswordEntity) =
    otp.validUntil.before(Timestamp.valueOf(LocalDateTime.now()))

}
