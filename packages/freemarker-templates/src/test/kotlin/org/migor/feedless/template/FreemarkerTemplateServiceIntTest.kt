package org.migor.feedless.template

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.mail.template.FreemarkerTemplateConfig
import org.migor.feedless.mail.template.FreemarkerTemplateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [FreemarkerTemplateService::class, FreemarkerTemplateConfig::class])
@ActiveProfiles(AppProfiles.mail, AppLayer.service)
class FreemarkerTemplateServiceIntTest {

  @Autowired
  lateinit var templateService: FreemarkerTemplateService

  @Test
  fun testMailTemplateReportCreated() {
    assertThat(
      renderTemplate(
        MailTemplateReportCreated(
          ReportCreatedParams(
            language = "en",
            deactivationLink = "deactivationLink",
            reportName = "reportName",
            cronExpression = "cronExpression",
            nextScheduledAt = "nextScheduledAt",
          )
        )
      )
    )
      .isEqualTo(
        """
<!DOCTYPE html>

<html lang="en">
<head>
<meta charset="UTF-8">
<title>Scheduled Report</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.5; color: #333;">

<h2 style="color: #2c3e50;">Scheduled Reporter Created</h2>
<p>Your report has been successfully scheduled. Below are the details:</p>

<table style="border-collapse: collapse; width: 100%; max-width: 600px;">
<tr>
<td style="padding: 8px; font-weight: bold;">Report Name:</td>
<td style="padding: 8px;">reportName</td>
</tr>
<tr>
<td style="padding: 8px; font-weight: bold;">Schedule (Cron):</td>
<td style="padding: 8px;">cronExpression</td>
</tr>
<tr>
<td style="padding: 8px; font-weight: bold;">Next Execution:</td>
<td style="padding: 8px;">nextScheduledAt</td>
</tr>
</table>

<p style="margin-top: 20px;">
If you no longer wish to receive this report, you can deactivate it here:
</p>

<p>
<a href="deactivationLink"
style="display: inline-block; padding: 10px 15px; background-color: #e74c3c; color: #fff; text-decoration: none; border-radius: 4px;">
Deactivate Report
</a>
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
      codeValidUntil = "codeValidUntil",
      code = "code",
      description = "description",
      corrId = "corrId",
    )
    assertThat(renderTemplate(MailTemplateAuthCode(params))).isEqualTo(
      """<!DOCTYPE html>
  <html>
  <head>
  <meta charset="UTF-8">
  <title>Authentication Code</title>
  <style>
  body {
  font-family: Arial, sans-serif;
  background-color: #f7f7f7;
  padding: 20px;
  }

  .container {
  background-color: #ffffff;
  max-width: 400px;
  margin: auto;
  padding: 20px;
  border-radius: 5px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .code {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 20px 0;
  letter-spacing: 4px;
  }

  .footer {
  font-size: 12px;
  color: #888;
  margin-top: 20px;
  }
  </style>
  </head>
  <body>
  <div class="container">
  <h2>Your Authentication Code</h2>
  <p>Please use the code below to complete your sign-in.</p>
  <div class="code">code</div>
  <p class="footer">This code will expire after codeValidUntil.</p>
  </div>
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
