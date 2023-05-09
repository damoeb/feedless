package org.migor.feedless.api.auth

import org.migor.feedless.api.ApiUrls
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.repositories.OneTimePasswordDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.AuthenticationEventMessage
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.mail.MailService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
class MailAuthenticationService {
  private val log = LoggerFactory.getLogger(AuthService::class.simpleName)

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var authWebsocketRepository: AuthWebsocketRepository

  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  fun authenticateUsingMail(corrId: String, email: String): Publisher<AuthenticationEvent> {
    log.info("[${corrId}] init user session for $email")
    return Flux.create { emitter ->
      userDAO.findByEmail(email).ifPresentOrElse(
        {
          val otp = OneTimePasswordEntity()
          otp.userId = it.id
          otp.password = UUID.randomUUID().toString()
          otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
          oneTimePasswordDAO.save(otp)
          val subject = "Authorize Access"
          val text = """
          ${propertyService.apiGatewayUrl}${ApiUrls.magicMail}?i=${otp.id}&k=${otp.password}&c=${corrId}
          (Expires in $otpValidForMinutes minutes)
        """.trimIndent()
          log.debug("[${corrId}] sending otp '${otp.password}' via magic mail")
          mailService.send(it.email, subject, text)
          emitMessage(emitter, "email has been sent")

          Mono.delay(Duration.ofMinutes(otpValidForMinutes)).subscribe {
            log.info("[${corrId}] auth session timed out")
            emitter.complete()
          }
          authWebsocketRepository.store(otp, emitter)
          emitter.onDispose {
            oneTimePasswordDAO.delete(otp)
          }
        },
        {
          log.error("[${corrId}] user not found")
          emitMessage(emitter, "user not found", true)
          emitter.complete()
        }
      )
    }
  }

  fun authenticateCli(corrId: String): Publisher<AuthenticationEvent> {
    log.info("[${corrId}] init cli session")
    return Flux.create { emitter ->
        runCatching {
          val otp = OneTimePasswordEntity()
          otp.userId = userDAO.findByEmail(propertyService.anonymousEmail).orElseThrow().id
          otp.password = listOf(0,1,2,3,4,5,6,7,8,9).shuffled().take(4).joinToString("")
          otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
          oneTimePasswordDAO.save(otp)
          val text = """
            To sign in, use a web browser to open the page ${propertyService.appHost}/cli?id=${otp.id}&corrId=${corrId} and enter the code ${otp.password} to authenticate.
        """.trimMargin().trimIndent()
//           (Expires in $otpValidForMinutes minutes)
          emitMessage(emitter, text)

          Mono.delay(Duration.ofMinutes(otpValidForMinutes))
            .subscribe {
            log.info("[${corrId}] auth session timed out")
            emitter.complete()
          }
          authWebsocketRepository.store(otp, emitter)
          emitter.onDispose {
            oneTimePasswordDAO.delete(otp)
          }
        }.onFailure {
          log.error("[${corrId}] ${it.message}")
          emitMessage(emitter, "${it.message}", true)
          emitter.complete()
        }
    }
  }

//  @Transactional
  fun confirmAuthCode(codeInput: ConfirmAuthCodeInput) {
    val otpId = UUID.fromString(codeInput.otpId)
    val otp = oneTimePasswordDAO.findById(otpId)

    otp.ifPresentOrElse({
      val emitter = authWebsocketRepository.pop(it)

      if (isOtpExpired(it)) {
        emitMessage(emitter, "code expired. Please restart authentication", true)
      }
      if (it.password != codeInput.code) {
        emitMessage(emitter, "Invalid code", true)
      }

      oneTimePasswordDAO.deleteById(otpId)

      val jwt = tokenProvider.createJwtForUser(it.user!!)
      emitter.next(
        AuthenticationEvent.newBuilder()
          .authentication(
            Authentication.newBuilder()
              .token(jwt.tokenValue)
              .corrId(newCorrId())
              .build()
          )
          .build()
      )
    },
      {
        log.error("otp not found")
      })
  }


  fun authorizeViaMail(corrId: String, otpId: String, nonce: String): String {
    log.info("[${corrId}] authenticate otp from magic mail: $otpId")
    val generalErrorMsg = "Code not found or invalid"
    return oneTimePasswordDAO.findById(UUID.fromString(otpId))
      .map {
        oneTimePasswordDAO.delete(it)

        // todo validate max attempts and cooldown
        if (it.password != nonce) {
          log.error("[${corrId}] invalid")
          generalErrorMsg
        } else {
          if (isOtpExpired(it)) {
            log.error("[${corrId}] expired")
            generalErrorMsg
          } else {
            // validate otp
            val emitter = authWebsocketRepository.pop(it)

            val confirmCode = OneTimePasswordEntity()
            confirmCode.userId = it.user!!.id
            val code = newCorrId(otpConfirmCodeLength).uppercase()
            confirmCode.password = code
            confirmCode.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
            authWebsocketRepository.store(confirmCode, emitter)

            oneTimePasswordDAO.save(confirmCode)

            log.info("[${corrId}] emitting confirmation code")
            emitter.next(
              AuthenticationEvent.newBuilder()
                .confirmCode(
                  ConfirmCode.newBuilder()
                    .length(otpConfirmCodeLength)
                    .otpId(confirmCode.id.toString())
                    .build()
                )
                .build()
            )
            "Enter this code in your browser: $code"
          }
        }}.orElseGet {
        log.info("[${corrId}] otp not found")
        generalErrorMsg
      }
  }

  private fun isOtpExpired(otp: OneTimePasswordEntity) =
    otp.validUntil.before(Timestamp.valueOf(LocalDateTime.now()))

  private fun emitMessage(emitter: FluxSink<AuthenticationEvent>, message: String, isError: Boolean = false) {
    emitter.next(
      AuthenticationEvent.newBuilder()
        .message(
          AuthenticationEventMessage.newBuilder()
            .message(message)
            .isError(isError)
            .build()
        )
        .build()
    )
  }
}
