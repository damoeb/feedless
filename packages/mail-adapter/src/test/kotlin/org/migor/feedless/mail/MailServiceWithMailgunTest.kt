package org.migor.feedless.mail

import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.Message
import com.mailgun.model.message.MessageResponse
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.Mother
import org.migor.feedless.mail.gateway.MailGunGateway
import org.migor.feedless.template.TemplateService
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
  classes = [
    MailServiceImpl::class,
    MailGatewayProperties::class,
  ]
)
@Import(MailServiceWithMailgunTest.MailgunTestConfig::class)
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
class MailServiceWithMailgunTest {

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var mailGunGateway: MailGunGateway

  @MockitoBean
  lateinit var mailgunMessagesApi: MailgunMessagesApi

  @MockitoBean
  lateinit var templateService: TemplateService

  @TestConfiguration
  class MailgunTestConfig {
    @Bean
    fun mailGunGateway(
      mailgunMessagesApi: MailgunMessagesApi,
      mailGatewayProperties: MailGatewayProperties
    ): MailGunGateway {
      return MailGunGateway(mailgunMessagesApi, mailGatewayProperties)
    }
  }

  @Test
  fun `should send authentication email using Mailgun`() = runTest {
    // Given
    val user = Mother.randomUser().copy(email = "user@example.com")
    val otp = Mother.randomOneTimePassword(userId = user.id)

    val description = "Login to your account"
    val expectedHtmlContent = "<html><body>Your code is 123456</body></html>"

    val messageResponse = MessageResponse.builder()
      .message("Queued. Thank you.")
      .id("<20231113123456.1.ABCD@feedless.org>")
      .build()

    `when`(mailgunMessagesApi.sendMessage(eq("feedless.org"), any()))
      .thenReturn(messageResponse)
    `when`(templateService.renderTemplate<Any>(any())).thenReturn(expectedHtmlContent)

    // When
    mailService.sendAuthCode(user, otp, description)

    // Then
    val messageCaptor = ArgumentCaptor.forClass(Message::class.java)
    verify(mailgunMessagesApi).sendMessage(eq("feedless.org"), messageCaptor.capture())

    val capturedMessage = messageCaptor.value
    assertThat(capturedMessage).isNotNull
    assertThat(capturedMessage.to).contains(user.email)
    assertThat(capturedMessage.subject).isEqualTo("Confirm your login with this code")
    assertThat(capturedMessage.html).isEqualTo(expectedHtmlContent)
    assertThat(capturedMessage.from).isEqualTo("test@feedless.org")
  }
}
