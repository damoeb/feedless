package org.migor.feedless.mail

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.Mother
import org.migor.feedless.mail.gateway.MailGatewayConfig
import org.migor.feedless.template.TemplateService
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.*

@SpringBootTest(
  classes = [
    MailServiceImpl::class,
    MailGatewayProperties::class,
  ]
)
@Import(MailGatewayConfig::class)
@EnableAutoConfiguration(
  exclude = [
    DataSourceAutoConfiguration::class,
    MailSenderValidatorAutoConfiguration::class,
  ]
)
@ActiveProfiles(
  "test",
  AppProfiles.mail,
  AppProfiles.properties,
)
@TestPropertySource(
  properties = [
    "app.mail.from=test@feedless.org",
    "app.mail.domain=feedless.org"
  ]
)
class MailServiceWithJavaMailTest {

  @Autowired
  lateinit var mailService: MailService

  @MockitoBean
  lateinit var javaMailSender: JavaMailSender

  @MockitoBean
  lateinit var templateService: TemplateService

  @Test
  fun `should send authentication email using JavaMail`() = runTest {
    // Given
    val user = Mother.randomUser().copy(email = "user@example.com")
    val otp = Mother.randomOneTimePassword(userId = user.id)

    val description = "Login to your account"
    val expectedHtmlContent = "<html><body>Your code is 123456</body></html>"

    val session = Session.getDefaultInstance(Properties())
    val mimeMessage = MimeMessage(session)
    `when`(javaMailSender.createMimeMessage()).thenReturn(mimeMessage)
    `when`(templateService.renderTemplate<Any>(any())).thenReturn(expectedHtmlContent)

    // When
    mailService.sendAuthCode(user, otp, description)

    // Then
    val messageCaptor = ArgumentCaptor.forClass(MimeMessage::class.java)
    verify(javaMailSender).send(messageCaptor.capture())

    val capturedMessage = messageCaptor.value
    assertThat(capturedMessage).isNotNull
    assertThat(capturedMessage.allRecipients).hasSize(1)
    assertThat(capturedMessage.allRecipients[0].toString()).isEqualTo(user.email)
    assertThat(capturedMessage.subject).isEqualTo("Confirm your login with this code")

    val content = capturedMessage.content
    assertThat(content).isInstanceOf(MimeMultipart::class.java)

    verify(templateService).renderTemplate<Any>(any())
  }
}
