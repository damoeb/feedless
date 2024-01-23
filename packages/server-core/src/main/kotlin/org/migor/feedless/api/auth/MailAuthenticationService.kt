package org.migor.feedless.api.auth

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.OneTimePasswordEntity
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.repositories.OneTimePasswordDAO
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.AuthenticationEventMessage
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.generated.types.Product
import org.migor.feedless.mail.MailService
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

data class Email(val subject: String, val text: String, val from: String)

@Service
@Profile(AppProfiles.database)
class MailAuthenticationService {
  private val log = LoggerFactory.getLogger(MailAuthenticationService::class.simpleName)

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

  fun authenticateUsingMail(corrId: String, data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val email = data.email
    log.info("[${corrId}] init user session for $email")
    return Flux.create { emitter -> run {
      val user = resolveUserByMail(email)

      val otp = OneTimePasswordEntity()
      otp.userId = user.id
      otp.password = newCorrId(otpConfirmCodeLength).uppercase()
      otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(otpValidForMinutes))
      oneTimePasswordDAO.save(otp)

      log.debug("[${corrId}] sending otp '${otp.password}'")
      mailService.send(user.email, getEmailForProduct(corrId, data, otp))
      emitMessage(emitter, "email has been sent")

      Mono.delay(Duration.ofMinutes(otpValidForMinutes)).subscribe {
        log.debug("[${corrId}] auth session timed out")
        emitter.complete()
      }
      authWebsocketRepository.store(otp, emitter)
      emitter.onDispose {
        oneTimePasswordDAO.delete(otp)
      }
    }
    }
  }

  private fun getEmailForProduct(corrId: String, data: AuthViaMailInput, otp: OneTimePasswordEntity): Email {
    val domain = getDomain(data.product)
    val subject = "$domain: Access Code"
    val text = """
Hi,
you request access to $domain. Please enter the following code (valid for $otpValidForMinutes minutes).


${otp.password}


(Browser: ${data.osInfo}, CorrelationId: ${corrId})

""".trimIndent()
    return Email(subject, text, "no-reply@$domain")
  }

  private fun getDomain(product: Product): String {
    return when(product) {
      Product.visualDiff -> "visualdiff.com"
      Product.pageChangeTracker -> "pagechangetracker.com"
      Product.reader -> throw IllegalArgumentException("not supported")
      Product.rssBuilder -> "feedguru.com"
      else -> "feedless.org"
    }
  }

  private fun resolveUserByMail(email: String): UserEntity {
    return userDAO.findByEmail(email) ?: createUser(email)
  }

  private fun createUser(email: String): UserEntity {
    TODO("Not yet implemented")
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
