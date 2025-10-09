package org.migor.feedless.template

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
  fun testTemplateWelcomePaidMail() {
    assertThat(renderTemplate(MailTemplateWelcomePaid(WelcomeMailParams(productName = "ThisProduct"))))
      .isEqualTo(
        """
<!DOCTYPE html>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  </head>
  <body>
    <p>Welcome to ThisProduct</p>
    <p>paid plan</p>
    <p>Regards,</p>
    <p>
      <em>ThisProduct Team</em> <br />
    </p>
  </body>
</html>
        """.trimAllIndents()
      )
  }

  @Test
  fun testTemplateVisualDiffChangeDetectedMail() {
    val params = VisualDiffChangeDetectedParams(
      trackerTitle = "trackerTitle",
      website = "website",
      inlineImages = "inlineImages"
    )
    assertThat(renderTemplate(MailTemplateVisualDiffChange(params))).isEqualTo(
      """
        <!DOCTYPE html>
  <html>
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <style>
  html, body {
  font-family: "Arial";
  }
  </style>
  </head>
  <body>
  <p>Hi,</p>
  <p>your tracker trackerTitle noticed a change on site website
  <div>inlineImages</div>
  </body>
  </html>
      """.trimAllIndents()
    )
  }

  @Test
  fun testTemplateVisualDiffWelcomeMail() {
    val params = VisualDiffWelcomeParams(
      trackerTitle = "trackerTitle",
      website = "website",
      trackerInfo = "",
      activateTrackerMailsUrl = "https://foo.bar/auth",
      info = ""
    )
    assertThat(renderTemplate(MailTemplateVisualDiffWelcome(params))).isEqualTo(
      """
      <!DOCTYPE html>
  <html>
  <head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <style>
  html, body {
  font-family: "Arial";
  }
  </style>
  </head>
  <body>
  <p>Hi,</p>
  <p>you created a page tracker trackerTitle for page website</p>
  <p>in order to authorize the tracking emails, <a href="https://foo.bar/auth" rel="link">click here</a> </p>

  <p></p>

  </body>
  </html>
    """.trimAllIndents()
    )
  }

  @Test
  fun testTemplateAuthCodeMail() {
    val params = AuthCodeMailParams(
      domain = "domain",
      codeValidUntil = "codeValidUntil",
      code = "code",
      description = "description",
      corrId = "corrId",
    )
    assertThat(renderTemplate(MailTemplateAuthCode(params))).isEqualTo(
      """<!DOCTYPE html>
  <html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <style>
      html, body {
        font-family: "Arial";
      }
    </style>
  </head>
  <body>
  <p>Hi,</p>
  <p>you request access to domain. Please enter the following code (valid until codeValidUntil minutes).</p>

  <div style="margin: 15px; font-size: 16px;">
    code
  </div>

  <div>(description, CorrelationId: corrId)</div>
  </body>
  </html>""".trimAllIndents()
    )
  }

  @Test
  fun testTemplateMailTrackerAuthorized() {
    assertThat(renderTemplate(MailTemplateChangeTrackerAuthorized())).isEqualTo(
      """
      <!DOCTYPE html>
  <html lang="en">
  <head>
  <meta charset="UTF-8">
  <title>Title</title>
  <style>
  html, body {
  font-family: "Arial";
  height: 100%;
  width: 100%;
  margin: 0;
  }
  </style>
  </head>
  <body style="display: flex; align-items: center; text-align: center">
  <div style="margin: auto">
  Great, your Change Tracker is now authorized and will send you emails.
  </div>

  </body>
  </html>
    """.trimAllIndents()
    )
  }

  private fun <T> renderTemplate(template: FreemarkerTemplate<T>): String {
    return templateService.renderTemplate(template).trimAllIndents()
  }
}

private fun String.trimAllIndents(): String {
  return this.lines().joinToString("\n") { it.trimIndent() }.trimIndent()
}
