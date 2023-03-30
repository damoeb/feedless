package org.migor.rich.rss.service

import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.auth.AuthService
import org.migor.rich.rss.auth.AuthWebsocketRepository
import org.migor.rich.rss.auth.TokenProvider
import org.migor.rich.rss.data.jpa.models.OneTimePasswordEntity
import org.migor.rich.rss.data.jpa.repositories.OneTimePasswordDAO
import org.migor.rich.rss.data.jpa.repositories.UserDAO
import org.migor.rich.rss.generated.types.Authentication
import org.migor.rich.rss.generated.types.AuthenticationEvent
import org.migor.rich.rss.generated.types.AuthenticationEventMessage
import org.migor.rich.rss.generated.types.ConfirmAuthCodeInput
import org.migor.rich.rss.generated.types.ConfirmCode
import org.migor.rich.rss.mail.MailService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import kotlin.properties.Delegates

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

  @Value("\${auth.token.anonymous.validForDays}")
  lateinit var tokenAnonymousValidForDays: String

  @Value("\${default.auth.token.anonymous.validForDays}")
  lateinit var defaultTokenAnonymousValidForDays: String

  private var tokenAnonymousValidFor: Long by Delegates.notNull()

  private val maxAgeWebTokenMin: Long = 10

  private val attrAuthorities = "authorities"
  private val otpValidForMinutes: Long = 5
  private val otpConfirmCodeLength: Int = 5

  fun initiateMailAuthentication(corrId: String, email: String): Publisher<AuthenticationEvent> {
    log.info("[${corrId}] init user session for $email")
    return Flux.create { emitter ->
      userDAO.findByEmail(email).ifPresentOrElse(
        {
          val otp = OneTimePasswordEntity()
          otp.user = it
          otp.password = UUID.randomUUID().toString()
          otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
          oneTimePasswordDAO.save(otp)
          authWebsocketRepository.store(otp, emitter)
          val subject = "Authorize Access"
          val text = """
          ${propertyService.publicUrl}${ApiUrls.magicMail}?i=${otp.id}&k=${otp.password}&c=${corrId}
          (Expires in $otpValidForMinutes minutes)
        """.trimIndent()
          log.debug("[${corrId}] sending otp '${otp.password}' via magic mail")
          mailService.send(it.email, subject, text)

          emitMessage(emitter, "email has been sent")
        },
        {
          log.error("[${corrId}] user not found")
          emitMessage(emitter, "user not found", true)
        }
      )
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
      if (it.password !== codeInput.code) {
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
            confirmCode.user = it.user
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
