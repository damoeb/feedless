package org.migor.feedless.mail

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class NativeMailServiceTest {

  @Mock
  lateinit var templateService: TemplateService

  @Mock
  lateinit var javaMailSender: JavaMailSender

  @InjectMocks
  lateinit var mailService: NativeMailService

  @Test
  fun sendAuthCode() = runTest {
    val user = mock(User::class.java)
    `when`(user.email).thenReturn("email@example.com")
    `when`(templateService.renderTemplate(any(FreemarkerTemplate::class.java))).thenReturn("rendered template")


    val otp = mock(OneTimePassword::class.java)
    `when`(otp.validUntil).thenReturn(LocalDateTime.now())
    `when`(otp.password).thenReturn(UUID.randomUUID().toString())

    mailService.sendAuthCode(user, otp, "")

    verify(javaMailSender).send(any(SimpleMailMessage::class.java))
  }
}

fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
