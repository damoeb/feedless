package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

  suspend fun authenticateUsingMail(corrId: String, data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val email = data.email
    log.info("[${corrId}] init user session for $email")
    return Flux.create { emitter ->
      CoroutineScope(Dispatchers.Default).launch {
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
          log.error("[$corrId] authenticateUsingMail failed: ${e.message}", e)
          emitter.error(e)
        }
      }
    }
  }

  private suspend fun resolveUserByMail(corrId: String, data: AuthViaMailInput): UserEntity? {
    return withContext(Dispatchers.IO) { userDAO.findByEmail(data.email) } ?: if (data.allowCreate) {
      createUser(corrId, data.email)
    } else {
      null
    }
  }

  private suspend fun createUser(corrId: String, email: String): UserEntity {
    return userService.createUser(corrId, email)
  }

  suspend fun confirmAuthCode(corrId: String, codeInput: ConfirmAuthCodeInput, response: HttpServletResponse) {
    val otpId = UUID.fromString(codeInput.otpId)
    val otp = withContext(Dispatchers.IO) {
      oneTimePasswordDAO.findById(otpId).orElseThrow()
    }

    if (isOtpExpired(otp)) {
      throw PermissionDeniedException("code expired. Please restart authentication ($corrId)")
    }
    if (otp.password != codeInput.code) {
      throw PermissionDeniedException("invalid code ($corrId)")
    }

    withContext(Dispatchers.IO) {
      oneTimePasswordDAO.deleteById(otpId)
    }

    val jwt = tokenProvider.createJwtForUser(otp.user!!)

    response.addCookie(cookieProvider.createTokenCookie(corrId, jwt))
  }

  private fun isOtpExpired(otp: OneTimePasswordEntity) =
    otp.validUntil.isBefore(LocalDateTime.now())

}
