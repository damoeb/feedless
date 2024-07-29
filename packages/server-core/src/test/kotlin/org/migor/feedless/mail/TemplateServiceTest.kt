package org.migor.feedless.mail

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.migor.feedless.agent.AgentDAO
import org.migor.feedless.community.ScoreService
import org.migor.feedless.license.LicenseService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles(profiles = ["test"])
@MockBeans(
  value = [
    MockBean(AgentDAO::class),
    MockBean(ScoreService::class),
    MockBean(UserService::class),
    MockBean(LicenseService::class),
    MockBean(ProductService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
class TemplateServiceTest {

  @Autowired
  lateinit var templateService: TemplateService

  @BeforeEach
  fun setUp() {
  }

  @Test
  fun test_welcomeFreeMail() {
    testTemplate(WelcomeFreeMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
  }

  @Test
  fun test_welcomeWaitListMail() {
    testTemplate(WelcomeWaitListMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
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
    testTemplate(MailTrackerAuthorizedTemplate())
  }

  private fun <T> testTemplate(template: FtlTemplate<T>) {
    assertDoesNotThrow {
      templateService.renderTemplate("", template)
    }
  }
}
