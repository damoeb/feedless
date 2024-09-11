//package org.migor.feedless.mail
//
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertDoesNotThrow
//import org.migor.feedless.AppLayer
//import org.migor.feedless.AppProfiles
//import org.migor.feedless.DisableDatabaseConfiguration
//import org.migor.feedless.common.HttpService
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.boot.test.mock.mockito.MockBeans
//import org.springframework.context.annotation.Import
//import org.springframework.test.context.ActiveProfiles
//
//@SpringBootTest(
//  properties = [
//    "spring.mail.host=localhost",
//    "spring.mail.port=1234"
////    spring.mail.username=${APP_MAIL_USERNAME}
////    spring.mail.password=${APP_MAIL_PASSWORD}
//    ]
//)
//@ActiveProfiles(
//  "test",
//  AppProfiles.mail,
//  AppLayer.service,
//)
//@MockBeans(
////    MockBean(AgentDAO::class),
////    MockBean(ScoreService::class),
//    MockBean(HttpService::class),
////    MockBean(LicenseService::class),
////    MockBean(ProductService::class),
////    MockBean(KotlinJdslJpqlExecutor::class),
//)
//@Import(DisableDatabaseConfiguration::class)
//class TemplateServiceTest {
//
//  @Autowired
//  lateinit var templateService: TemplateService
//
//  @BeforeEach
//  fun setUp() {
//  }
//
//  @Test
//  fun test_welcomeFreeMail() {
//    testTemplate(WelcomeFreeMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
//  }
//
//  @Test
//  fun test_welcomeWaitListMail() {
//    testTemplate(WelcomeWaitListMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
//  }
//
//  @Test
//  fun test_welcomePaidMail() {
//    testTemplate(WelcomePaidMailTemplate(WelcomeMailParams(productName = "ThisProduct")))
//  }
//
//  @Test
//  fun test_VisualDiffChangeDetectedMail() {
//    val params = VisualDiffChangeDetectedParams(
//      trackerTitle = "trackerTitle",
//      website = "website",
//      inlineImages = "inlineImages"
//    )
//    testTemplate(VisualDiffChangeDetectedMailTemplate(params))
//  }
//
//  @Test
//  fun test_VisualDiffWelcomeMail() {
//    val params = VisualDiffWelcomeParams(
//      trackerTitle = "trackerTitle",
//      website = "website",
//      trackerInfo = "",
//      activateTrackerMailsUrl = "https://foo.bar/auth",
//      info = ""
//    )
//    testTemplate(VisualDiffWelcomeMailTemplate(params))
//  }
//
//  @Test
//  fun test_MailTrackerAuthorized() {
//    testTemplate(MailTrackerAuthorizedTemplate())
//  }
//
//  private fun <T> testTemplate(template: FtlTemplate<T>) {
//    assertDoesNotThrow {
//      templateService.renderTemplate("", template)
//    }
//  }
//}
