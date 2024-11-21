package org.migor.feedless.mail

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.UnavailableException
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.migor.feedless.user.corrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.mail} & ${AppProfiles.session} & ${AppLayer.service}")
class MailAuthenticationService(
  private val tokenProvider: TokenProvider,
  private val cookieProvider: CookieProvider,
  private val oneTimePasswordDAO: OneTimePasswordDAO,
  private val userService: UserService,
  private val featureService: FeatureService,
  private val userDAO: UserDAO,
  private val oneTimePasswordService: OneTimePasswordService
) {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

  @Transactional(readOnly = true)
  suspend fun authenticateUsingMail(data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val email = data.email
    log.debug("init user session for $email")
    val corrId = coroutineContext.corrId()
    return Flux.create { emitter ->
      CoroutineScope(Dispatchers.Default).launch {
        try {
          if (featureService.isDisabled(FeatureName.canLogin, null)) {
            throw UnavailableException("login is deactivated by feature flag")
          }

          val user = resolveUserByMail(data)

          val otp = if (user == null) {
            oneTimePasswordService.createOTP()
          } else {
            oneTimePasswordService.createOTP(user, data.osInfo)
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

  private suspend fun resolveUserByMail(data: AuthViaMailInput): UserEntity? {
    return withContext(Dispatchers.IO) { userDAO.findByEmail(data.email) } ?: if (data.allowCreate) {
      createUser(data.email)
    } else {
      null
    }
  }

  private suspend fun createUser(email: String): UserEntity {
    return userService.createUser(email)
  }

  suspend fun confirmAuthCode(codeInput: ConfirmAuthCodeInput, response: HttpServletResponse) {
    val corrId = coroutineContext.corrId()
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

    response.addCookie(cookieProvider.createTokenCookie(jwt))
  }

  private fun isOtpExpired(otp: OneTimePasswordEntity) =
    otp.validUntil.isBefore(LocalDateTime.now())

}
