package org.migor.feedless.mail

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.template.FreemarkerTemplate
import org.migor.feedless.template.MailTemplateAuthCode
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MailServiceImplCorrIdTest {

  @Test
  fun `the auth mail quotes the current correlation id`() = runTest(RequestContext(corrId = "c1")) {
    val rendered = mutableListOf<FreemarkerTemplate<*>>()
    val templateService = object : TemplateService {
      override fun <T> renderTemplate(template: FreemarkerTemplate<T>): String {
        rendered.add(template)
        return "<p/>"
      }
    }
    val user = mock<User> { on { email } doReturn "someone@example.org" }

    MailServiceImpl(templateService, mock<MailGateway>()).sendAuthCode(user, OneTimePassword(userId = UserId()), "login")

    assertThat((rendered.single() as MailTemplateAuthCode).params.corrId).isEqualTo("c1")
  }
}
