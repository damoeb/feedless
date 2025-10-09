package org.migor.feedless.template

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [FreemarkerTemplateService::class, FreemarkerTemplateConfig::class])
@ActiveProfiles(AppProfiles.mail, AppLayer.service)
class FreemarkerTemplateServiceTest {

  @Autowired
  lateinit var templateService: FreemarkerTemplateService

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
