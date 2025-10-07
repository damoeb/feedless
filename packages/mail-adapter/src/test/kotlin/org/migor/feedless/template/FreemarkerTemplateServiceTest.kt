package org.migor.feedless.template

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableMailConfiguration
import org.migor.feedless.mail.MailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(
  "test",
  AppProfiles.mail,
  AppLayer.service,
)
@MockBeans(
  MockBean(MailService::class),
  MockBean(OneTimePasswordService::class),
)
class FreemarkerTemplateServiceTest {

  @Autowired
  lateinit var templateService: FreemarkerTemplateService

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun test_welcomePaidMail() {
    testTemplate(WelcomePaidMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
  }

  @Test
  fun test_VisualDiffChangeDetectedMail() {
    val params = VisualDiffChangeDetectedParams(
      trackerTitle = "trackerTitle",
      website = "website",
      inlineImages = "inlineImages"
    )
    testTemplate(VisualDiffChangeDetectedMailTemplate(params))
  }

  @Test
  fun test_VisualDiffWelcomeMail() {
    val params = VisualDiffWelcomeParams(
      trackerTitle = "trackerTitle",
      website = "website",
      trackerInfo = "",
      activateTrackerMailsUrl = "https://foo.bar/auth",
      info = ""
    )
    testTemplate(VisualDiffWelcomeMailTemplate(params))
  }

  @Test
  fun test_MailTrackerAuthorized() {
    testTemplate(ChangeTrackerAuthorizedTemplate())
  }

  private fun <T> testTemplate(template: FtlTemplate<T>) {
    assertDoesNotThrow {
      templateService.renderTemplate(template)
    }
  }
}
